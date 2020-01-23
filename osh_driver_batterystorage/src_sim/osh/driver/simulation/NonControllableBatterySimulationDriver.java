package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.driver.simulation.batterylogic.SimpleBatteryLogic;
import osh.driver.simulation.batterystorage.SimpleBatteryStorageModel;
import osh.driver.simulation.inverter.SimpleInverterModel;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.BatteryStorageOX;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.SubjectAction;

import java.util.UUID;

/**
 * @author Jan Mueller
 */
public class NonControllableBatterySimulationDriver extends DeviceSimulationDriver {

    //	// Battery parameters

    private final int STEP_SIZE = 1;

    private SimpleInverterModel inverterModel;
    private SimpleBatteryStorageModel batteryModel;
    private SimpleBatteryLogic batteryLogic;

    private int initialStateOfCharge;

    private int batteryMinChargingState;
    private int batteryMaxChargingState;
    private int batteryMinDischargePower;
    private int batteryMaxDischargePower;
    private int batteryMinChargePower;
    private int batteryMaxChargePower;
    private int standingLoss;

    private int inverterMinPower;
    private int inverterMaxPower;
    private int inverterMinComplexPower;
    private int inverterMaxComplexPower;

    private long newIppAfter;
    private int triggerIppIfDeltaSoCBigger;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public NonControllableBatterySimulationDriver(IOSH osh, UUID deviceID,
                                                  OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // Battery parameters
        this.batteryMinChargingState = Integer.parseInt(driverConfig.getParameter("minChargingState"));
        this.batteryMaxChargingState = Integer.parseInt(driverConfig.getParameter("maxChargingState"));
        this.batteryMinDischargePower = Integer.parseInt(driverConfig.getParameter("minDischargingPower"));
        this.batteryMaxDischargePower = Integer.parseInt(driverConfig.getParameter("maxDischargingPower"));
        this.batteryMinChargePower = Integer.parseInt(driverConfig.getParameter("minChargingPower"));
        this.batteryMaxChargePower = Integer.parseInt(driverConfig.getParameter("maxChargingPower"));

        //FIXME (also in standing loss calculation)
        this.standingLoss = 0;

        //Inverter parameters
        this.inverterMinPower = Integer.parseInt(driverConfig.getParameter("minInverterPower"));
        this.inverterMaxPower = Integer.parseInt(driverConfig.getParameter("maxInverterPower"));
        this.inverterMinComplexPower = this.inverterMinPower;
        this.inverterMaxComplexPower = this.inverterMaxPower;

        try {
            this.newIppAfter = Long.parseLong(this.getDriverConfig().getParameter("newIppAfter"));
        } catch (Exception e) {
            this.newIppAfter = 3600; // 1 hour
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        try {
            this.triggerIppIfDeltaSoCBigger = Integer.parseInt(this.getDriverConfig().getParameter("triggerIppIfDeltaSoCBigger"));
        } catch (Exception e) {
            this.triggerIppIfDeltaSoCBigger = 100;
            this.getGlobalLogger().logWarning("Can't get triggerIppIfDeltaSoCBigger, using the default value: " + this.triggerIppIfDeltaSoCBigger);
        }

        this.initialStateOfCharge = 0;

        this.batteryModel = new SimpleBatteryStorageModel(
                this.standingLoss,
                this.batteryMinChargingState,
                this.batteryMaxChargingState,
                this.batteryMinChargePower,
                this.batteryMaxChargePower,
                this.batteryMinDischargePower,
                this.batteryMaxDischargePower,
                this.initialStateOfCharge
        );

        this.inverterModel = new SimpleInverterModel(
                this.inverterMinComplexPower,
                this.inverterMaxComplexPower,
                this.inverterMinPower,
                this.inverterMaxPower
        );

        this.batteryLogic = new SimpleBatteryLogic();
    }

    @Override
    public void onNextTimeTick() {
        long now = this.getTimeDriver().getUnixTime();

        int currentPowerAtGridConnection = 0;
        if (this.ancillaryMeterState != null) {
            currentPowerAtGridConnection = (int) this.ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
        }

        this.batteryLogic.doStupidBMS(
                currentPowerAtGridConnection,
                this.batteryModel,
                this.inverterModel,
                this.STEP_SIZE,
                0, //int OptimizedmaxChargePower,
                0, //int OptimizedminChargePower,
                0, //int OptimizedmaxDisChargePower,
                0  //int OptimizedminDisChargePower
        );


        // set state of driver
        this.setPower(Commodity.ACTIVEPOWER, this.inverterModel.getActivePower());
        this.setPower(Commodity.REACTIVEPOWER, this.inverterModel.getReactivePower());

        // send OC
        BatteryStorageOX ox = new BatteryStorageOX(
                this.getUUID(),
                now,
                this.inverterModel.getActivePower(),
                this.inverterModel.getReactivePower(),
                this.batteryModel.getStateOfCharge(),
                this.batteryModel.getStateOfHealth(),
                this.standingLoss,
                this.batteryMinChargingState,
                this.batteryMaxChargingState,
                this.batteryMinChargePower,
                this.batteryMaxChargePower,
                this.batteryMinDischargePower,
                this.batteryMaxDischargePower,
                this.inverterMinComplexPower,
                this.inverterMaxComplexPower,
                this.inverterMinPower,
                this.inverterMaxPower,
                0,
                this.newIppAfter,
                this.triggerIppIfDeltaSoCBigger,
                this.compressionType,
                this.compressionValue);

        this.notifyObserver(ox);
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }

}
