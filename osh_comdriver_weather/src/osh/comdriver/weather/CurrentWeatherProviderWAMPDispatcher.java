package osh.comdriver.weather;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import osh.comdriver.WeatherProviderWAMPDriver;
import osh.comdriver.details.CurrentWeatherDetails;
import osh.core.logging.IGlobalLogger;
import osh.openweathermap.current.CurrentWeatherMap;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handling the connection to openweathermap via wamp
 *
 * @author Jan Mueller
 */
public class CurrentWeatherProviderWAMPDispatcher {

    static final int TIMER_INTERVAL = 1000; // 1s
    // Scheduler
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Scheduler rxScheduler = Schedulers.from(this.executor);
    private final IGlobalLogger globalLogger;
    private final WeatherProviderWAMPDriver comDriver;
    private final String url = "ws://wamp-router:8080/ws";
    private final String realm = "eshl";
    //	private String realm = "realm1";
    private final String wampTopic = "eshl.openweathermap.v1.readout.currentweather";
    private final ObjectMapper mapper;
    private final Lock weatherWriteLock = new ReentrantReadWriteLock().writeLock();
    CurrentWeatherDetails currentWeatherDetails;
    Subscription onDataSubscription;
    private WampClient client;
    private long lastLog;

    /**
     * CONSTRUCTOR
     *
     * @param logger
     * @param comDriver
     */
    public CurrentWeatherProviderWAMPDispatcher(IGlobalLogger logger, WeatherProviderWAMPDriver comDriver) {
        super();
        this.mapper = new ObjectMapper();
        this.globalLogger = logger;
        this.comDriver = comDriver;

        this.subscribeForWampUpdates();
    }

    /**
     * Collect information about current weather and provide it WeatherProviderWAMPDriver.java
     */
    public void subscribeForWampUpdates() {
        WampClientBuilder builder = new WampClientBuilder();
        try {
            IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
            builder.withConnectorProvider(connectorProvider)
                    .withUri(this.url)
                    .withRealm(this.realm)
                    .withInfiniteReconnects()
                    .withReconnectInterval(3, TimeUnit.SECONDS);

            this.client = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Subscribe on the clients status updates
        this.client.statusChanged()
                .observeOn(this.rxScheduler)
                .subscribe(
                        state -> {
                            System.out.println("Session status changed to " + state);
                            if (state instanceof WampClient.ConnectedState) {
                                // SUBSCRIBE to a topic and receive events
                                this.onDataSubscription = this.client.makeSubscription(this.wampTopic)
                                        .observeOn(this.rxScheduler)
                                        .subscribe(
                                                ev -> {
                                                    if (ev.arguments() == null || ev.arguments().size() < 1)
                                                        return; // error

                                                    JsonNode eventNode = ev.arguments().get(0);
                                                    if (eventNode.isNull()) return;

                                                    try {
                                                        CurrentWeatherMap currentWeatherData = this.mapper.convertValue(eventNode, CurrentWeatherMap.class);

                                                        this.weatherWriteLock.lock();
                                                        try {
                                                            this.currentWeatherDetails = new CurrentWeatherDetails(
                                                                    this.comDriver.getUUID(),
                                                                    this.comDriver.getTimeDriver().getCurrentEpochSecond(),
                                                                    currentWeatherData);
                                                            this.comDriver.receiveCurrentDetails(this.currentWeatherDetails);

                                                            this.lastLog = this.comDriver.getTimeDriver().getCurrentEpochSecond();
                                                        } finally {
                                                            this.weatherWriteLock.unlock();
                                                        }

                                                    } catch (IllegalArgumentException e) {
                                                        // error
                                                    }
                                                },
                                                e -> this.globalLogger.logError("failed to subscribe to topic", e),
                                                () -> this.globalLogger.logInfo("subscription ended"));
                            } else if (state instanceof WampClient.DisconnectedState) {
                                if (this.onDataSubscription != null)
                                    this.onDataSubscription.unsubscribe();
                                this.onDataSubscription = null;
                            }
                        },
                        t -> this.globalLogger.logError("Session ended with error ", t),
                        () -> this.globalLogger.logInfo("Session ended normally"));

        this.client.open();
    }
}
