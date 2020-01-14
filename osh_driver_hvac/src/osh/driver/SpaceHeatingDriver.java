package osh.driver;

import osh.comdriver.details.WeatherPredictionDetails;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.driver.model.BuildingThermalModel;
import osh.driver.model.ESHLThermalModel;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceHeatingPredictionObserverExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.physics.TemperatureUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class SpaceHeatingDriver
        extends HALDeviceDriver
        implements IEventTypeReceiver, IHasState {

    private final BuildingThermalModel model;
    private WeatherPredictionDetails weatherPredictionDetails;
    //private TemperaturePrediction temperaturePrediction;
    private final Map<Long, Double> predictedHeatConsumptionMap;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;
    private UUID weatherPredictionProviderUUID;


    public SpaceHeatingDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        try {
            this.compressionType = LoadProfileCompressionTypes.valueOf(this.getDriverConfig().getParameter("compressionType"));
        } catch (Exception e) {
            this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
            this.getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
        }

        try {
            this.compressionValue = Integer.parseInt(this.getDriverConfig().getParameter("compressionValue"));
        } catch (Exception e) {
            this.compressionValue = 100;
            this.getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
        }

        try {
            this.weatherPredictionProviderUUID = UUID.fromString(this.getDriverConfig().getParameter("weahterPredictionProviderUUID"));
        } catch (Exception e) {
            this.weatherPredictionProviderUUID = UUID.fromString("00000000-0000-2200-0000-7265ab8ef219");
            this.getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.weatherPredictionProviderUUID);
        }


        this.model = new ESHLThermalModel();
        this.predictedHeatConsumptionMap = new HashMap<>();
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getDriverRegistry().registerStateChangeListener(WeatherPredictionDetails.class, this);

        StaticCompressionExchange observerExchange =
                new StaticCompressionExchange(this.getDeviceID(), this.getTimer().getUnixTime(), this.compressionType, this.compressionValue);

        this.notifyObserver(observerExchange);
    }


    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        //NOTHING
    }


    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
        if (event instanceof StateChangedExchange && ((StateChangedExchange) event).getStatefulEntity().equals(this.weatherPredictionProviderUUID)) {
            StateChangedExchange exsc = (StateChangedExchange) event;
            boolean updateOx = false;

            if (exsc.getType().equals(WeatherPredictionDetails.class)) {
                this.weatherPredictionDetails = this.getDriverRegistry().getState(WeatherPredictionDetails.class, exsc.getStatefulEntity());

                for (int index = 0; index < this.weatherPredictionDetails.getTemperatureForecastList().getList().size(); index++) {
                    double temperaturePrediction = TemperatureUtil.convertKelvinToCelsius(
                            this.weatherPredictionDetails.getTemperatureForecastList().getList().get(index).getMain().getTemp());
                    long timeOfPrediction = this.weatherPredictionDetails.getTemperatureForecastList().getList().get(index).getDt();
                    this.predictedHeatConsumptionMap.put(timeOfPrediction, this.model.calculateHeatingDemand(temperaturePrediction));
                }
                updateOx = true;
            }

            if (updateOx) {
                SpaceHeatingPredictionObserverExchange observerExchange =
                        new SpaceHeatingPredictionObserverExchange(
                                this.getDeviceID(),
                                this.getTimer().getUnixTime(),
                                this.predictedHeatConsumptionMap);
                this.notifyObserver(observerExchange);
            }
        }
    }


    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

}
