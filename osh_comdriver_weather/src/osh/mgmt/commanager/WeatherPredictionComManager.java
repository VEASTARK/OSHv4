package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.UUID;


/**
 * @author Jan Mueller
 */
public class WeatherPredictionComManager extends ComManager implements IEventTypeReceiver {

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
        this.getTimer().registerComponent(this, 1);
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

    }


    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(
            Class<T> type, T event) {
        // NOTHING
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        // NOTHING
    }

}
