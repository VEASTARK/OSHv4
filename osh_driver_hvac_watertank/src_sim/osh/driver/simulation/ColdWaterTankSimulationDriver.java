package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.thermal.FactorisedBasicWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.ColdWaterTankObserverExchange;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class
ColdWaterTankSimulationDriver extends WaterTankSimulationDriver {

    private Duration newIppAfter;
    private double triggerIppIfDeltaTempBigger;
    private double rescheduleIfViolatedTemperature;
    private Duration rescheduleIfViolatedDuration;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public ColdWaterTankSimulationDriver(IOSH osh, UUID deviceID,
                                         OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // tank capacity in liters
        double tankCapacity;
        try {
            tankCapacity = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.tankCapacity));
        } catch (Exception e) {
            tankCapacity = 3000.0;
            this.getGlobalLogger().logWarning("Can't get tankCapacity, using the default value: " + tankCapacity);
        }

        double tankDiameter;
        try {
            tankDiameter = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.tankDiameter));
        } catch (Exception e) {
            tankDiameter = 1.0;
            this.getGlobalLogger().logWarning("Can't get tankDiameter, using the default value: " + tankDiameter);
        }

        double initialTemperature;
        try {
            initialTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.initialTemperature));
        } catch (Exception e) {
            initialTemperature = 14.0;
            this.getGlobalLogger().logWarning("Can't get initialTemperature, using the default value: " + initialTemperature);
        }

        double ambientTemperature;
        try {
            ambientTemperature = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.ambientTemperature));
        } catch (Exception e) {
            ambientTemperature = 20.0;
            this.getGlobalLogger().logWarning("Can't get ambientTemperature, using the default value: " + ambientTemperature);
        }

        double standingHeatLossFactor;
        try {
            standingHeatLossFactor = Double.parseDouble(driverConfig.getParameter(ParameterConstants.WaterTank.standingHeatLossFactor));
        } catch (Exception e) {
            standingHeatLossFactor = 1.0;
            this.getGlobalLogger().logWarning("Can't get standingHeatLossFactor, using the default value: " + standingHeatLossFactor);
        }

        try {
            this.newIppAfter =
                    Duration.ofSeconds(Long.parseLong(this.getDriverConfig().getParameter(ParameterConstants.IPP.newIPPAfter)));
        } catch (Exception e) {
            this.newIppAfter = Duration.ofHours(1);
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        try {
            this.rescheduleIfViolatedTemperature =
                    Double.parseDouble(driverConfig.getParameter(ParameterConstants.IPP.rescheduleIfViolatedTemperature));
        } catch (Exception e) {
            this.rescheduleIfViolatedTemperature = 2.5;
            this.getGlobalLogger().logWarning("Can't get rescheduleIfViolatedTemperature, using the default value: " + this.rescheduleIfViolatedDuration);
        }

        try {
            this.rescheduleIfViolatedDuration =
                    Duration.ofSeconds(Integer.parseInt(driverConfig.getParameter(ParameterConstants.IPP.rescheduleIfViolatedDuration)));
        } catch (Exception e) {
            //TODO: 2 minutes as deault is too low but will be kept for backwards-compatibility, change to 10 as soon
            // as the next update that breaks backwards-compatibility
            this.rescheduleIfViolatedDuration = Duration.ofMinutes(2);
            this.getGlobalLogger().logWarning("Can't get rescheduleIfViolatedDuration, using the default value: " + this.rescheduleIfViolatedDuration);
        }

        try {
            this.triggerIppIfDeltaTempBigger =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.IPP.triggerIppIfDeltaTemp));
        } catch (Exception e) {
            this.triggerIppIfDeltaTempBigger = 0.1;
            this.getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
        }

        this.waterTank = new FactorisedBasicWaterTank(
                tankCapacity,
                tankDiameter,
                initialTemperature,
                ambientTemperature,
                standingHeatLossFactor);
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();

        IPPSchedulingExchange _ise = new IPPSchedulingExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        _ise.setNewIppAfter(this.newIppAfter);
        _ise.setTriggerIfDeltaX(this.triggerIppIfDeltaTempBigger);
        this.notifyObserver(_ise);
    }

    @Override
    public void onNextTimeTick() {

        // reduce be standing loss
        this.waterTank.reduceByStandingHeatLoss(1);
        double demand = 0, supply = 0;

        // add/remove energy from tank...
//		if (thermalInputState != null) {
        if (this.commodityInputStates.containsCommodity(Commodity.COLDWATERPOWER)) {
            double power = this.commodityInputStates.getPower(Commodity.COLDWATERPOWER);
            double[] addThermal = this.commodityInputStates.getAdditionalThermal(Commodity.COLDWATERPOWER);
            if (power != 0) {
//				// (-1) because of energy calculation tank is based on heat not coolness...
//				waterTank.addPowerOverTime( (-1) * power, 1, reflowTemperature, massFlow);
//				waterTank.addPowerOverTime( power, 1, reflowTemperature, massFlow);
                this.waterTank.addPowerOverTime(power, 1, addThermal[0], addThermal[1]);
            }
//			thermalInputState = null;
            if (power < 0) {
                demand += power;
            } else if (power > 0) {
                supply += power;
            }
        }

        // communicate to visualization / GUI
        // -> is done via Observer -> OCRegistry -> GuiComMgr -> GuiComDriver

        // communicate state to observer
        ColdWaterTankObserverExchange observerExchange =
                new ColdWaterTankObserverExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        this.waterTank.getCurrentWaterTemperature(),
                        this.waterTank.getTankCapacity(),
                        this.waterTank.getTankDiameter(),
                        this.waterTank.getAmbientTemperature(),
                        this.waterTank.getStandingHeatLossFactor(),
                        this.rescheduleIfViolatedTemperature,
                        this.rescheduleIfViolatedDuration,
                        demand,
                        supply);
        this.notifyObserver(observerExchange);
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
