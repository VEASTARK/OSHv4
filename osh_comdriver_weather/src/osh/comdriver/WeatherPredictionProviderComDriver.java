package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.comdriver.details.WeatherPredictionDetails;
import osh.comdriver.weather.CurrentWeatherRequestThread;
import osh.comdriver.weather.WeatherPredictionRequestThread;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.exceptions.HALException;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

import java.util.UUID;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class WeatherPredictionProviderComDriver extends CALComDriver implements IEventTypeReceiver, IHasState {

    // Current weather
    // http://api.openweathermap.org/data/2.5/weather?id=ID&APPID=APPID

    // Forecast
    // http://api.openweathermap.org/data/2.5/forecast/city?id=ID&APPID=APPID

    private final String urlToCurrentWeather = "http://api.openweathermap.org/data/2.5/weather?id=2892794&APPID=";
    private final String urlToWeatherPrediction = "http://api.openweathermap.org/data/2.5/forecast/city?id=2892794&APPID=";
    private final String apiKey = "API_KEY";

//	private WeatherPredictionDetails weatherdetails = null;

    private WeatherPredictionRequestThread reqPredictionRunnable;
    private Thread reqPredictionThread;

    private CurrentWeatherRequestThread reqCurrentRunnable;
    private Thread reqCurrentThread;
    private UUID loggerUUID;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public WeatherPredictionProviderComDriver(IOSH osh, UUID deviceID, OSHParameterCollection driverConfig) throws HALException {
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

        this.getTimer().registerComponent(this, 3600);

        // init weather prediction request thread
        this.reqPredictionRunnable = new WeatherPredictionRequestThread(this.getGlobalLogger(), this, this.urlToWeatherPrediction,
                this.apiKey);
        this.reqPredictionThread = new Thread(this.reqPredictionRunnable, "DachsInformationRequestThread");
        this.reqPredictionThread.start();

        // init current weather request thread
        this.reqCurrentRunnable = new CurrentWeatherRequestThread(this.getGlobalLogger(), this, this.urlToCurrentWeather,
                this.apiKey);
        this.reqCurrentThread = new Thread(this.reqCurrentRunnable, "DachsInformationRequestThread");
        this.reqCurrentThread.start();

    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // still alive message
        if (this.getTimer().getUnixTime() % 60 == 0) {
            this.getGlobalLogger().logDebug("onNextTimePeriod() (getTimer().getUnixTime() % 60 == 0) - I'm still alive");
        }

        // re-init request thread if dead
        if (this.reqPredictionThread == null || !this.reqPredictionThread.isAlive()) {
            this.getGlobalLogger().logError("Restart WeatherPredictionRequestThread");

            this.reqPredictionRunnable = new WeatherPredictionRequestThread(this.getGlobalLogger(), this,
                    this.urlToWeatherPrediction, this.apiKey);
            this.reqPredictionThread = new Thread(this.reqPredictionRunnable, "PredictionThread");
            this.reqPredictionThread.start();
        }
        if (this.reqCurrentThread == null || !this.reqCurrentThread.isAlive()) {
            this.getGlobalLogger().logError("Restart CurrentWeatherRequestThread");

            this.reqCurrentRunnable = new CurrentWeatherRequestThread(this.getGlobalLogger(), this, this.urlToCurrentWeather,
                    this.apiKey);
            this.reqCurrentThread = new Thread(this.reqCurrentRunnable, "CurrentThread");
            this.reqCurrentThread.start();
        }

    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        this.reqPredictionRunnable.shutdown();
        this.reqCurrentRunnable.shutdown();
    }


    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
        //NOTHING
    }

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

    public void receiveCurrentDetails(CurrentWeatherDetails currentWeatherDetails) {
        synchronized (currentWeatherDetails) {
            //TODO: this was changed from driverRegistry to ComRegistry please fix all other classes depending on recieveing this on the driverRegistry (via DataBroker)
            // set raw details
            this.getComRegistry().publish(CurrentWeatherDetails.class, currentWeatherDetails);
            this.getGlobalLogger().logDebug("set new state" + currentWeatherDetails);
        }
    }

    public void receivePredictionDetails(WeatherPredictionDetails weatherDetails) {
        synchronized (weatherDetails) {
            //TODO: see above
            // set raw details
            this.getComRegistry().publish(WeatherPredictionDetails.class, weatherDetails);
            this.getGlobalLogger().logDebug("set new state" + weatherDetails);
        }
    }

}
