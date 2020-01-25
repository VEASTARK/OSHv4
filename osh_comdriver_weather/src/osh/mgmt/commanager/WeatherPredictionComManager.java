package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;

import java.util.UUID;


/**
 * @author Jan Mueller
 */
public class WeatherPredictionComManager extends ComManager {

    CurrentWeatherDetails currentWeatherDetails;
    WeatherPredictionDetails weatherPredictionDetails;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public WeatherPredictionComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        // NOTHING
    }

}
