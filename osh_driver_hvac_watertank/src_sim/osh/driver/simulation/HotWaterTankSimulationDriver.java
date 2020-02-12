package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class HotWaterTankSimulationDriver extends WaterTankSimulationDriver {

//	private SimpleHotWaterTank waterTank;

    private Duration newIppAfter;
    private double triggerIppIfDeltaTempBigger;

    private boolean log;
    private double temperatureLogging;
    private double demandLogging;
    private double supplyLogging;
    private double temperatureLoggingCounter;

    /**
     * CONSTRUCTOR
     */
    public HotWaterTankSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(
                osh,
                deviceID,
                driverConfig);

        // tank capacity in liters
        double tankCapacity;
        try {
            tankCapacity = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.tankCapacity));
        } catch (Exception e) {
            tankCapacity = 750;
            this.getGlobalLogger().logWarning("Can't get tankCapacity, using the default value: " + tankCapacity);
        }

        double tankDiameter;
        try {
            tankDiameter = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.tankDiameter));
        } catch (Exception e) {
            tankDiameter = 0.5;
            this.getGlobalLogger().logWarning("Can't get tankDiameter, using the default value: " + tankDiameter);
        }

        double initialTemperature;
        try {
            initialTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.initialTemperature));
        } catch (Exception e) {
            initialTemperature = 70.0;
            this.getGlobalLogger().logWarning("Can't get initialTemperature, using the default value: " + initialTemperature);
        }

        double ambientTemperature;
        try {
            ambientTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.ambientTemperature));
        } catch (Exception e) {
            ambientTemperature = 20.0;
            this.getGlobalLogger().logWarning("Can't get ambientTemperature, using the default value: " + ambientTemperature);
        }

        try {
            this.newIppAfter =
                    Duration.ofSeconds(Long.parseLong(this.getDriverConfig().getParameter(ParameterConstants.IPP.newIPPAfter)));
        } catch (Exception e) {
            this.newIppAfter = Duration.ofHours(1);
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        try {
            this.triggerIppIfDeltaTempBigger =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.IPP.triggerIppIfDeltaTemp));
        } catch (Exception e) {
            this.triggerIppIfDeltaTempBigger = 0.5;
            this.getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
        }

        this.waterTank = new SimpleHotWaterTank(
                tankCapacity,
                tankDiameter,
                initialTemperature,
                ambientTemperature);
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();

        IPPSchedulingExchange _ise = new IPPSchedulingExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        _ise.setNewIppAfter(this.newIppAfter);
        _ise.setTriggerIfDeltaX(this.triggerIppIfDeltaTempBigger);
        this.notifyObserver(_ise);

        this.log = DatabaseLoggerThread.isLogWaterTank();
    }

    @Override
    public void onSystemShutdown() {
        if (this.log) {
            this.temperatureLogging /= this.temperatureLoggingCounter;
            this.demandLogging /= 3600000.0;
            this.supplyLogging /= 3600000.0;

            DatabaseLoggerThread.enqueueWaterTank(this.temperatureLogging, this.demandLogging, this.supplyLogging);
        }
    }


    @Override
    public void onNextTimeTick() {

        // reduce be standing loss
        this.waterTank.reduceByStandingHeatLoss(1);
        double waterDemand = 0;
        double waterSupply = 0;


        if (this.commodityInputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
            double power = (-1) * this.commodityInputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
            double[] addThermal = this.commodityInputStates.getAdditionalThermal(Commodity.HEATINGHOTWATERPOWER);
            if (power != 0) {
                this.waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
            }
            if (power < 0) {
                waterDemand += power;
            } else if (power > 0) {
                waterSupply += power;
            }
        }

        if (this.commodityInputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
            double power = (-1) * this.commodityInputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);
            double[] addThermal = this.commodityInputStates.getAdditionalThermal(Commodity.DOMESTICHOTWATERPOWER);
            if (power != 0) {
                this.waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
            }
            if (power < 0) {
                waterDemand += power;
            } else if (power > 0) {
                waterSupply += power;
            }
        }

        if (this.log) {
            this.temperatureLogging += this.waterTank.getCurrentWaterTemperature();
            this.demandLogging += waterDemand;
            this.supplyLogging += waterSupply;
            this.temperatureLoggingCounter++;
        }

        // communicate to visualization / GUI
        // -> is done via Observer -> OCRegistry -> GuiComMgr -> GuiComDriver

        // communicate state to observer
        HotWaterTankObserverExchange observerExchange =
                new HotWaterTankObserverExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        this.waterTank.getCurrentWaterTemperature(),
                        this.waterTank.getTankCapacity(),
                        this.waterTank.getTankDiameter(),
                        this.waterTank.getAmbientTemperature(),
                        waterDemand,
                        waterSupply);
        this.notifyObserver(observerExchange);
    }


    @Override
    public LimitedCommodityStateMap getCommodityOutputStates() {
        LimitedCommodityStateMap map =
                new LimitedCommodityStateMap(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
        map.setTemperature(Commodity.HEATINGHOTWATERPOWER, this.waterTank.getCurrentWaterTemperature());
        return map;
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
