package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.gasboiler.GasBoilerModel;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.GasBoilerObserverExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class GasBoilerSimulationDriver extends DeviceSimulationDriver {

    private double minTemperature;
    private double maxTemperature;
    private int maxHotWaterPower;
    private int maxGasPower;

    private int typicalActivePowerOn;
    private int typicalActivePowerOff;
    private int typicalReactivePowerOn;
    private int typicalReactivePowerOff;

    private final GasBoilerModel model;

    private Duration newIppAfter;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public GasBoilerSimulationDriver(IOSH osh, UUID deviceID,
                                     OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        try {
            this.minTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.GasBoiler.hotWaterStorageMinTemp));
        } catch (Exception e) {
            this.minTemperature = 60;
            this.getGlobalLogger().logWarning("Can't get minTemperature, using the default value: " + this.minTemperature);
        }

        try {
            this.maxTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.GasBoiler.hotWaterStorageMaxTemp));
        } catch (Exception e) {
            this.maxTemperature = 80;
            this.getGlobalLogger().logWarning("Can't get maxTemperature, using the default value: " + this.maxTemperature);
        }

        try {
            this.maxHotWaterPower = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.maxHotWaterPower));
        } catch (Exception e) {
            this.maxHotWaterPower = 15000;
            this.getGlobalLogger().logWarning("Can't get maxHotWaterPower, using the default value: " + this.maxHotWaterPower);
        }

        try {
            this.maxGasPower = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.maxGasPower));
        } catch (Exception e) {
            this.maxGasPower = 15000;
            this.getGlobalLogger().logWarning("Can't get maxGasPower, using the default value: " + this.maxGasPower);
        }

        try {
            this.typicalActivePowerOn = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.activePowerOn));
        } catch (Exception e) {
            this.typicalActivePowerOn = 100;
            this.getGlobalLogger().logWarning("Can't get typicalActivePowerOn, using the default value: " + this.typicalActivePowerOn);
        }

        try {
            this.typicalActivePowerOff = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.activePowerOff));
        } catch (Exception e) {
            this.typicalActivePowerOff = 0;
            this.getGlobalLogger().logWarning("Can't get typicalActivePowerOff, using the default value: " + this.typicalActivePowerOff);
        }

        try {
            this.typicalReactivePowerOn = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.reactivePowerOn));
        } catch (Exception e) {
            this.typicalReactivePowerOn = 0;
            this.getGlobalLogger().logWarning("Can't get typicalReactivePowerOn, using the default value: " + this.typicalReactivePowerOn);
        }

        try {
            this.typicalReactivePowerOff = Integer.parseInt(driverConfig.getParameter(ParameterConstants.GasBoiler.reactivePowerOff));
        } catch (Exception e) {
            this.typicalReactivePowerOff = 0;
            this.getGlobalLogger().logWarning("Can't get typicalReactivePowerOff, using the default value: " + this.typicalReactivePowerOff);
        }

        try {
            this.newIppAfter =
                    Duration.ofSeconds(Integer.parseInt(driverConfig.getParameter(ParameterConstants.IPP.newIPPAfter)));
        } catch (Exception e) {
            this.newIppAfter = Duration.ofHours(1); //1 hour
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        this.model = new GasBoilerModel(this.maxHotWaterPower, this.maxGasPower, this.typicalActivePowerOn, this.typicalActivePowerOff,
                this.typicalReactivePowerOn, this.typicalReactivePowerOff, false);
    }

//	Nothing to do for now
//	@Override
//	public void onSimulationIsUp() throws SimulationSubjectException {
//		super.onSimulationIsUp();
//	}

    @Override
    public void onNextTimeTick() {
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        // LOGIC
        double waterTemperature = this.commodityInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

        if (waterTemperature < this.minTemperature) {
            this.model.switchOn();
        } else if (waterTemperature > this.maxTemperature) {
            this.model.switchOff();
        }

        int activePower = this.model.getActivePower();
        int reactivePower = this.model.getReactivePower();
        int gasPower = this.model.getGasPower();
        int hotWaterPower = -this.model.getHotWaterPower();

        this.setPower(Commodity.ACTIVEPOWER, activePower);
        this.setPower(Commodity.REACTIVEPOWER, reactivePower);
        this.setPower(Commodity.NATURALGASPOWER, gasPower);
        this.setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);

        GasBoilerObserverExchange ox = new GasBoilerObserverExchange(
                this.getUUID(),
                now,
                this.minTemperature,
                this.maxTemperature,
                waterTemperature,
                this.model.isOn(),
                activePower,
                reactivePower,
                gasPower,
                hotWaterPower,
                this.maxHotWaterPower,
                this.maxGasPower,
                this.typicalActivePowerOn,
                this.typicalActivePowerOff,
                this.typicalReactivePowerOn,
                this.typicalReactivePowerOff,
                this.newIppAfter);
        this.notifyObserver(ox);
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
