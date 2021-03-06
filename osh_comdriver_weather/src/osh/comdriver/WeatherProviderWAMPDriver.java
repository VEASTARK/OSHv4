package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.comdriver.weather.CurrentWeatherProviderWAMPDispatcher;
import osh.comdriver.weather.WeatherPredictionProviderWAMPDispatcher;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;

import java.util.UUID;

/**
 * @author Jan Mueller
 */
public class WeatherProviderWAMPDriver extends CALComDriver implements Runnable {

    private WeatherPredictionProviderWAMPDispatcher weatherPredictionProviderWAMPDispatcher;


    private CurrentWeatherProviderWAMPDispatcher currentWeatherProviderWAMPDispatcher;
    private Thread reqCurrentThread;
    private final UUID loggerUUID;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public WeatherProviderWAMPDriver(IOSH osh, UUID deviceID, OSHParameterCollection driverConfig) throws HALException {
        super(osh, deviceID, driverConfig);

        String loggerUUID = driverConfig.getParameter("loggeruuid");
        if (loggerUUID == null) {
            throw new HALException("Need config parameter loggeruuid");
        }
        this.loggerUUID = UUID.fromString(loggerUUID);

    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.HOUR);

        this.currentWeatherProviderWAMPDispatcher = new CurrentWeatherProviderWAMPDispatcher(this.getGlobalLogger(), this);
        this.weatherPredictionProviderWAMPDispatcher = new WeatherPredictionProviderWAMPDispatcher(this.getGlobalLogger(), this);

        new Thread(this, "pull proxy of WeatherProviderDriver to WAMP").start();

    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        // still alive message
        if (exchange.getTimeEvents().contains(TimeSubscribeEnum.HOUR)) {
            this.getGlobalLogger().logDebug("hour gone - I'm still alive");
        }

    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.currentWeatherProviderWAMPDispatcher) {
                try { // wait for new data
                    this.currentWeatherProviderWAMPDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }

                // long timestamp = getTimer().getUnixTime();

                // if ( currentWeatherProviderWAMPDispatcher. ().isEmpty() ) {
                // // an error has occurred
                // }
            }
            synchronized (this.weatherPredictionProviderWAMPDispatcher) {
                try { // wait for new data
                    this.weatherPredictionProviderWAMPDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }

//				long timestamp = getTimer().getUnixTime();
                // if ( currentWeatherProviderWAMPDispatcher. ().isEmpty() ) {
                // // an error has occurred
                // }
            }
        }
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

    public void receiveCurrentDetails(CurrentWeatherDetails currentWeatherDetails) {
        synchronized (currentWeatherDetails) {
            //TODO: this was changed from driverRegistry to ComRegistry please fix all other classes depending on receiving this on the driverRegistry (via DataBroker)
            // set raw details
            this.getComRegistry().publish(CurrentWeatherDetails.class, this, currentWeatherDetails);
            this.getGlobalLogger().logDebug("set new state" + currentWeatherDetails);
        }
    }

    public void receivePredictionDetails(WeatherPredictionDetails weatherDetails) {
        synchronized (weatherDetails) {
            //TODO: see above
            // set raw details
            this.getComRegistry().publish(WeatherPredictionDetails.class, this, weatherDetails);
            this.getGlobalLogger().logDebug("set new state" + weatherDetails);
        }
    }

}
