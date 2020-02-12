package osh.comdriver.details;

import osh.datatypes.registry.StateExchange;
import osh.openweathermap.current.CurrentWeatherMap;
import osh.utils.DeepCopy;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * http://openweathermap.org/current
 *
 * @author Jan Mueller
 */

public class CurrentWeatherDetails extends StateExchange {

    private final CurrentWeatherMap currentWeatherMap;

    /**
     * CONSTRUCTOR
     */
    public CurrentWeatherDetails(UUID sender, ZonedDateTime timestamp, CurrentWeatherMap currentWeatherMap) {
        super(sender, timestamp);

        this.currentWeatherMap = (CurrentWeatherMap) DeepCopy.copy(currentWeatherMap);
    }

    public CurrentWeatherMap getTemperatureForecastList() {
        return this.currentWeatherMap;
    }

}
