package osh.comdriver.details;

import osh.datatypes.registry.StateExchange;
import osh.openweathermap.prediction.PredictedWeatherMap;
import osh.utils.DeepCopy;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * http://openweathermap.org/forecast5
 *
 * @author Jan Mueller
 */

public class WeatherPredictionDetails extends StateExchange {

    /**
     * SERIAL
     */
    private static final long serialVersionUID = -2129466555073434421L;

    private final PredictedWeatherMap openWeatherMap;

    /**
     * CONSTRUCTOR
     */
    public WeatherPredictionDetails(UUID sender, ZonedDateTime timestamp, PredictedWeatherMap temperatureForecast) {
        super(sender, timestamp);

        this.openWeatherMap = (PredictedWeatherMap) DeepCopy.copy(temperatureForecast);
    }

    public PredictedWeatherMap getTemperatureForecastList() {
        return this.openWeatherMap;
    }

}
