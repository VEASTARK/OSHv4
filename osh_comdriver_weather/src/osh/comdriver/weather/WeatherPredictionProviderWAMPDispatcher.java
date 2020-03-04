package osh.comdriver.weather;


import com.fasterxml.jackson.databind.ObjectMapper;
import osh.comdriver.WeatherProviderWAMPDriver;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.core.logging.IGlobalLogger;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handling the connection to openweathermap via wamp
 *
 * @author Jan Mueller
 */
public class WeatherPredictionProviderWAMPDispatcher {

    static final int TIMER_INTERVAL = 1000; // 1s
    // Scheduler
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IGlobalLogger globalLogger;
    private final WeatherProviderWAMPDriver comDriver;
    private final ObjectMapper mapper;
    CurrentWeatherDetails weatherDetails;
    Subscription onDataSubscription;
    Scheduler rxScheduler = Schedulers.from(this.executor);
    private final String url = "ws://wamp-router:8080/ws";
    private final String realm = "eshl";
    //	private String realm = "realm1";
    private final String wampTopic = "eshl.openweathermap.v1.readout.currentweather";
    private WampClient client;
    private long lastLog;

    /**
     * CONSTRUCTOR
     *
     * @param logger
     * @param comDriver
     */
    public WeatherPredictionProviderWAMPDispatcher(IGlobalLogger logger, WeatherProviderWAMPDriver comDriver) {
        super();
        this.mapper = new ObjectMapper();
        this.globalLogger = logger;
        this.comDriver = comDriver;

        this.subscribeForWampUpdates();
    }

    /**
     * Collect information about predicted weather and provide it WeatherProviderWAMPDriver.java
     */
    public void subscribeForWampUpdates() {
        //TODO: read weather prediction from WAMP and provide it
    }
}
