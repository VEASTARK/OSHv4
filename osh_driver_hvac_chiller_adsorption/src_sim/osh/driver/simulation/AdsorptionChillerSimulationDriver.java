package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.chiller.AdsorptionChillerModel;
import osh.driver.simulation.spacecooling.HollOutdoorTemperatures;
import osh.driver.simulation.spacecooling.OutdoorTemperatures;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.esc.LimitedCommodityStateMap;
import osh.hal.exchange.ChillerControllerExchange;
import osh.hal.exchange.ChillerObserverExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.SubjectAction;

import java.util.UUID;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class AdsorptionChillerSimulationDriver
        extends DeviceSimulationDriver {

    //Constants
    /**
     * [W]
     */
    private final int typicalStandbyEnergyConsumption = 10;
    /**
     * [W]
     */
    private final int typicalRunningActivePowerConsumption = 420;
    /**
     * [degree Celsius]
     */
    private final double minTemperatureIN = 60.0;
    /**
     * [degree Celsius]
     */
    private final double maxTemperatureIN = 80.0;

    //GET SIMULATED OUTDOOR TEMPERATURES
//	HollOutdoorTemperatureAugust2015 outdoorTemperature = new HollOutdoorTemperatureAugust2015();
    final OutdoorTemperatures outdoorTemperature = new HollOutdoorTemperatures(this.getGlobalLogger());

    //Variables
    private boolean runningRequestFromController;
    private boolean running;
    /**
     * [0..1]
     */
    private double currentCop = 1; // just default...
    /**
     * [W]
     */
    private int currentCoolingPower; // just default...
    private int currentHotWaterPower;
    /**
     * RANGE: [22-37]!
     */
    private double currentOutdoorTemperature = 35.0;

    // received from other devices...
    private double observedWaterTemperature = Double.MIN_VALUE;

    /**
     * CONSTRUCTOR
     */
    public AdsorptionChillerSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // NOTHING
    }

    @Override
    public void onNextTimeTick() {

        // simulate device
        if (this.runningRequestFromController) {
            // check whether to switch on or off
            if (this.observedWaterTemperature <= this.maxTemperatureIN
                    && this.observedWaterTemperature >= this.minTemperatureIN) {
                if (!this.running) {
                    this.doSwitchOnLogic();
                }
            } else {
                if (this.running) {
                    this.doSwitchOffLogic();
                }
            }
        } else {
            if (this.running) {
                this.doSwitchOffLogic();
            }
        }

        this.doLogic();

        // notify Observer about current status
        ChillerObserverExchange ox = new ChillerObserverExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentEpochSecond(),
                this.running,
                this.outdoorTemperature);
        ox.setColdWaterPower(this.currentCoolingPower);
        ox.setHotWaterPower(this.currentHotWaterPower);
        ox.setActivePower(this.running ? this.typicalRunningActivePowerConsumption : this.typicalStandbyEnergyConsumption);
//		ox.setReactivePower(reactivePower);
        this.notifyObserver(ox);
    }


    private void doLogic() {

        if (this.running) {
            // get outdoor temperature
            this.currentOutdoorTemperature = this.outdoorTemperature.getTemperature(this.getTimeDriver().getCurrentEpochSecond());

            // CALCULATE COP AND DYNAMIC COOLING POWER
            this.currentCoolingPower = AdsorptionChillerModel.chilledWaterPower(this.observedWaterTemperature, this.currentOutdoorTemperature);
            this.currentCop = AdsorptionChillerModel.cop(this.observedWaterTemperature, this.currentOutdoorTemperature);
            this.currentHotWaterPower = (int) -Math.round(this.currentCoolingPower / this.currentCop);

            //DEBUG
//			getGlobalLogger().logDebug("currentOutdoorTemperature: " + currentOutdoorTemperature);
//			getGlobalLogger().logDebug("observedWaterTemperature: " + observedWaterTemperature);
//			getGlobalLogger().logDebug("currentCoolingPower: " + currentCoolingPower);
//			getGlobalLogger().logDebug("currentCop: " + c  urrentCop);
            if (this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.HOUR)) {
                this.getGlobalLogger().logDebug(
                        "outdoorTemperature: " + this.currentOutdoorTemperature
                                + " | hotwaterdemand: " + (int) ((-1) * (this.currentCoolingPower / this.currentCop))
                                + " | coldwaterdemand: " + this.currentCoolingPower
                                + " | cop: " + this.currentCop);
            }

            // it is on...
            this.setPower(Commodity.ACTIVEPOWER, this.typicalRunningActivePowerConsumption);
        } else {
            // reset values
            this.currentCoolingPower = 0;
            this.currentCop = 1;
            this.currentHotWaterPower = 0;

            // it is off...
            this.setPower(Commodity.ACTIVEPOWER, this.typicalStandbyEnergyConsumption);
        }
        this.setPower(Commodity.COLDWATERPOWER, this.currentCoolingPower);
        this.setPower(Commodity.HEATINGHOTWATERPOWER, this.currentHotWaterPower);
    }


    private void doSwitchOnLogic() {
        if (this.running) {
            //should not happen
            this.getGlobalLogger().logError("BAD!");
        } else {
            this.running = true;
        }
    }


    private void doSwitchOffLogic() {
        if (this.running) {
            this.running = false;
        } else {
            //should not happen
            this.getGlobalLogger().logError("BAD!");
        }
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest)
            throws HALException {
        super.onControllerRequest(controllerRequest);

        ChillerControllerExchange cx = (ChillerControllerExchange) controllerRequest;
        boolean stop = cx.isStopGenerationFlag();
        boolean cr = cx.isCoolingRequest();

        if ((cr) && !stop) {
            this.runningRequestFromController = true;
            //getGlobalLogger().logDebug("cooling request from controller");
        } else if (stop) {
            this.runningRequestFromController = false;
            //getGlobalLogger().logDebug("got stop request");
        } else {
            this.runningRequestFromController = false;
            //getGlobalLogger().logDebug("no cooling request from controller");
        }
    }

    @Override
    public void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
            AncillaryMeterState ancillaryMeterState) {

        super.setCommodityInputStates(inputStates, ancillaryMeterState);
        if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
            this.observedWaterTemperature = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
        }
    }


    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
