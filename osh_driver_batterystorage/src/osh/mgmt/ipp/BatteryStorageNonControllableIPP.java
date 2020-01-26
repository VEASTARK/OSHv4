package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.simulation.batterylogic.SimpleBatteryLogic;
import osh.driver.simulation.batterystorage.SimpleBatteryStorageModel;
import osh.driver.simulation.inverter.SimpleInverterModel;

import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Sebastian Kramer, Jan Mueller
 */
@SuppressWarnings("unused")
public class BatteryStorageNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = 1063856793735351671L;
    private final double batteryInitialStateOfCharge;
    private final double batteryInitialStateOfHealth;
    private final int batteryStandingLoss;
    private final int batteryMinChargingState;
    private final int batteryMaxChargingState;
    private final int batteryMinChargePower;
    private final int batteryMinDischargePower;
    private final int batteryMaxChargePower;
    private final int inverterMinComplexPower;
    private final int inverterMaxComplexPower;
    private final int inverterMaxPower;
    private final int inverterMinPower;
    private final int batteryMaxDischargePower;
    private SimpleInverterModel inverterModel;
    private SimpleBatteryStorageModel batteryModel;
    private SimpleBatteryLogic batteryLogic;


    /**
     * CONSTRUCTOR
     */
    public BatteryStorageNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            long now,
            double batteryInitialStateOfCharge,
            double batteryInitialStateOfHealth,
            int batteryStandingLoss,
            int batteryMinChargingState,
            int batteryMaxChargingState,
            int batteryMinChargePower,
            int batteryMaxChargePower,
            int batteryMinDischargePower,
            int batteryMaxDischargePower,
            int inverterMinComplexPower,
            int inverterMaxComplexPower,
            int inverterMinPower,
            int inverterMaxPower,
            LoadProfileCompressionTypes compressionType,
            int compressionValue
    ) {

        super(
                deviceId,
                logger,
                false, //does not cause scheduling
                true, //needs ancillary meter state as Input State
                false, //reacts to input states
                false, //is not static
                now,
                DeviceTypes.BATTERYSTORAGE,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);

        this.batteryInitialStateOfCharge = batteryInitialStateOfCharge;
        this.batteryInitialStateOfHealth = batteryInitialStateOfHealth;
        this.batteryStandingLoss = batteryStandingLoss;
        this.batteryMinChargingState = batteryMinChargingState;
        this.batteryMaxChargingState = batteryMaxChargingState;
        this.batteryMinChargePower = batteryMinChargePower;
        this.batteryMaxChargePower = batteryMaxChargePower;
        this.batteryMinDischargePower = batteryMinDischargePower;
        this.batteryMaxDischargePower = batteryMaxDischargePower;
        this.inverterMinComplexPower = inverterMinComplexPower;
        this.inverterMaxComplexPower = inverterMaxComplexPower;
        this.inverterMinPower = inverterMinPower;
        this.inverterMaxPower = inverterMaxPower;
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected BatteryStorageNonControllableIPP() {
        super();
        this.batteryInitialStateOfHealth = 0;
        this.batteryInitialStateOfCharge = 0;
        this.batteryStandingLoss = 0;
        this.batteryMinChargingState = 0;
        this.batteryMaxChargingState = 0;
        this.batteryMinChargePower = 0;
        this.batteryMinDischargePower = 0;
        this.batteryMaxChargePower = 0;
        this.inverterMinComplexPower = 0;
        this.inverterMaxComplexPower = 0;
        this.inverterMaxPower = 0;
        this.inverterMinPower = 0;
        this.batteryMaxDischargePower = 0;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        //  better not...new IPP instead
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        this.inverterModel = new SimpleInverterModel(
                this.inverterMinComplexPower,
                this.inverterMaxComplexPower,
                this.inverterMinPower,
                this.inverterMaxPower);

        this.batteryModel = new SimpleBatteryStorageModel(
                this.batteryStandingLoss,
                this.batteryMinChargingState,
                this.batteryMaxChargingState,
                this.batteryMinChargePower,
                this.batteryMaxChargePower,
                this.batteryMinDischargePower,
                this.batteryMaxDischargePower,
                this.batteryInitialStateOfCharge);

        this.batteryLogic = new SimpleBatteryLogic();
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // get information about available power
            int availablePower = 0;
            if (this.ancillaryMeterState != null) {
                availablePower = (int) this.ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
            }

            // get SOC of Battery
//			double stateOfCharge = this.batteryModel.getStateOfCharge();

            // update state
            this.batteryLogic.doStupidBMS(
                    availablePower,
                    this.batteryModel,
                    this.inverterModel,
                    this.getStepSize(),
                    0,
                    0,
                    0,
                    0
            );

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setPower(
                    Commodity.ACTIVEPOWER, this.inverterModel.getActivePower());
            this.internalInterdependentOutputStates.setPower(
                    Commodity.REACTIVEPOWER, this.inverterModel.getReactivePower());
            this.setOutputStates(this.internalInterdependentOutputStates);

            if (this.getLoadProfile() != null) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), this.inverterModel.getActivePower());
                this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(),
                        this.inverterModel.getActivePower());
            }
        } else {
            this.getGlobalLogger().logDebug("interdependentInputStates == null");
        }

        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            if (this.getLoadProfile().getEndingTimeOfProfile() > 0) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), 0);
            }
            return new Schedule(this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] BatteryStorageNonControllableIPP "
                + "activePower="
                + (this.inverterModel != null ? this.inverterModel.getActivePower() : "N/A")
                + " initialStateOfCharge="
                + this.batteryInitialStateOfCharge;
    }
}