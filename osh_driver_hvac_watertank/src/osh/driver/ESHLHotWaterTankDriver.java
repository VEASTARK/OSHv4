package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.driver.thermal.SimpleHotWaterTank;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.HotWaterTankObserverExchange;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.UUID;


/**
 * @author Ingo Mauser, Jan Mueller
 */
public class ESHLHotWaterTankDriver extends WaterTankDriver {

    private final SimpleHotWaterTank waterTank;

    private Duration newIppAfter;
    private double triggerIppIfDeltaTempBigger;

//	private TemperatureDetails currentTemperatureDetails = null;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public ESHLHotWaterTankDriver(IOSH osh, UUID deviceID,
                                  OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

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
            this.newIppAfter = Duration.ofHours(1); // 1 hour
            this.getGlobalLogger().logWarning("Can't get newIppAfter, using the default value: " + this.newIppAfter);
        }

        try {
            this.triggerIppIfDeltaTempBigger =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.IPP.triggerIppIfDeltaTemp));
        } catch (Exception e) {
            this.triggerIppIfDeltaTempBigger = 0.25;
            this.getGlobalLogger().logWarning("Can't get triggerIppIfDeltaTempBigger, using the default value: " + this.triggerIppIfDeltaTempBigger);
        }

        this.waterTank = new SimpleHotWaterTank(
                tankCapacity,
                tankDiameter,
                initialTemperature,
                ambientTemperature);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        StaticCompressionExchange _stat =
                new StaticCompressionExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        _stat.setCompressionType(this.compressionType);
        _stat.setCompressionValue(this.compressionValue);
        this.notifyObserver(_stat);

        IPPSchedulingExchange _ise = new IPPSchedulingExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        _ise.setNewIppAfter(this.newIppAfter);
        _ise.setTriggerIfDeltaX(this.triggerIppIfDeltaTempBigger);
        this.notifyObserver(_ise);

        HotWaterTankObserverExchange observerExchange =
                new HotWaterTankObserverExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentTime(),
                        this.waterTank.getCurrentWaterTemperature(),
                        this.waterTank.getTankCapacity(),
                        this.waterTank.getTankDiameter(),
                        this.waterTank.getAmbientTemperature(),
                        0,
                        0);
        this.notifyObserver(observerExchange);
    }


    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        if (exchange instanceof TemperatureDetails) {
            TemperatureDetails currentTemperatureDetails = (TemperatureDetails) exchange;
            this.waterTank.setCurrentWaterTemperature(currentTemperatureDetails.getTemperature());

            HotWaterTankObserverExchange observerExchange =
                    new HotWaterTankObserverExchange(
                            this.getUUID(),
                            this.getTimeDriver().getCurrentTime(),
                            this.waterTank.getCurrentWaterTemperature(),
                            this.waterTank.getTankCapacity(),
                            this.waterTank.getTankDiameter(),
                            this.waterTank.getAmbientTemperature(),
                            0,
                            0);
            this.notifyObserver(observerExchange);
        }
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        //NOTHING
    }
}
