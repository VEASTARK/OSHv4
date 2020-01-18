package osh.comdriver.details;

import osh.datatypes.registry.StateExchange;
import osh.openweathermap.current.CurrentWeatherMap;
import osh.utils.DeepCopy;

import java.util.UUID;

/**
 * http://openweathermap.org/current
 *
 * @author Jan Mueller
 */

public class CurrentWeatherDetails extends StateExchange {

    /**
     * SERIAL
     */
    private static final long serialVersionUID = -2129466555073434421L;

    private final CurrentWeatherMap currentWeatherMap;

    /**
     * CONSTRUCTOR
     */
    public CurrentWeatherDetails(UUID sender, long timestamp, CurrentWeatherMap currentWeatherMap) {
        super(sender, timestamp);

        this.currentWeatherMap = (CurrentWeatherMap) DeepCopy.copy(currentWeatherMap);
    }

    public CurrentWeatherMap getTemperatureForecastList() {
        return this.currentWeatherMap;
    }

}
