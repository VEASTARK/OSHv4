package osh.busdriver.mielegateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataJSON;
import osh.core.logging.IGlobalLogger;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handling the connection to one miele gateway
 *
 * @author Kaibin Bao, Ingo Mauser
 */
public class MieleGatewayWAMPDispatcher {

    // Scheduler
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Scheduler rxScheduler = Schedulers.from(this.executor);
    private final IGlobalLogger logger;
    private final String url = "ws://wamp-router:8080/ws";
    private final String realm = "eshl";
    private final ObjectMapper mapper;
    // (Miele) UID (sic!) -> device state map
    private final Map<Integer, MieleDeviceHomeBusDataJSON> deviceData;
    private final ReentrantReadWriteLock deviceDataLock = new ReentrantReadWriteLock();
    Subscription onDataSubscription;
    private WampClient client;

    /**
     * CONSTRUCTOR
     *
     * @param logger OSH logger for this dispatcher
     */
    public MieleGatewayWAMPDispatcher(IGlobalLogger logger) {
        super();

        this.logger = logger;

        this.deviceData = new HashMap<>();

        this.mapper = new ObjectMapper();

        this.subscribeForWampUpdates();
    }

    /**
     * Collect information about Miele device and provide it (to
     * MieleGatewayDriver)
     *
     * @param id Miele UID (sic!)
     * @return Miele device data corresponding to the provided uid
     */
    public MieleDeviceHomeBusDataJSON getDeviceData(Integer id) {
        MieleDeviceHomeBusDataJSON dev;

        this.deviceDataLock.readLock().lock();
        dev = this.deviceData.get(id);
        this.deviceDataLock.readLock().unlock();

        return dev;
    }

    /**
     * Collect all information about Miele devices and provide it (to
     * MieleGatewayDriver)
     *
     * @return a collection of all known device data
     */
    public Collection<MieleDeviceHomeBusDataJSON> getDeviceData() {

        this.deviceDataLock.readLock().lock();
        ArrayList<MieleDeviceHomeBusDataJSON> devices = new ArrayList<>(this.deviceData.values());
        this.deviceDataLock.readLock().unlock();

        return devices;
    }

    public void sendCommand(String command, int uid) {
        this.client.call("eshl.miele.v1.homebus." + command, uid)
                .observeOn(this.rxScheduler)
                .subscribe(this.logger::logInfo,
                        err -> this.logger.logError("sending command failed", err));
    }

    public void sendStartTimes(long startTime, int uid) {
        HashMap<Integer, Long> publishMap = new HashMap<>();
        publishMap.put(uid, startTime);
        this.client.publish("eshl.schedules", publishMap, new TypeReference<Map<Integer, Long>>() {
        })
                .observeOn(this.rxScheduler)
                .subscribe(this.logger::logInfo,
                        err -> this.logger.logError("publishing startTime failed", err));
    }

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
                            this.logger.logInfo("Session status changed to " + state);
                            if (state instanceof WampClient.ConnectedState) {
                                // SUBSCRIBE to a topic and receive events
                                this.onDataSubscription = this.client.makeSubscription("eshl.miele.v1.homebus")
                                        .observeOn(this.rxScheduler)
                                        .subscribe(
                                                ev -> {
                                                    if (ev.arguments() == null || ev.arguments().size() < 1)
                                                        return; // error

                                                    JsonNode eventNode = ev.arguments().get(0);
                                                    if (eventNode.isNull()) return;

                                                    try {
                                                        Map<Integer, MieleDeviceHomeBusDataJSON> map =
                                                                this.mapper.convertValue(eventNode, new TypeReference<>() {
                                                                });

                                                        this.deviceDataLock.writeLock().lock();
                                                        this.deviceData.clear();
                                                        this.deviceData.putAll(map);
                                                        this.deviceDataLock.writeLock().unlock();

                                                        this.notifyAll();
                                                    } catch (IllegalArgumentException e) {
                                                        // error
                                                    }
                                                },
                                                e -> this.logger.logError("failed to subscribe to topic", e),
                                                () -> this.logger.logInfo("subscription ended"));
                            } else if (state instanceof WampClient.DisconnectedState) {
                                if (this.onDataSubscription != null)
                                    this.onDataSubscription.unsubscribe();
                                this.onDataSubscription = null;
                            }
                        },
                        t -> this.logger.logError("Session ended with error ", t),
                        () -> this.logger.logInfo("Session ended normally"));

        this.client.open();
    }

    public String convertStreamToString(InputStream is) throws IOException {
        // To convert the InputStream to String we use the
        // Reader.read(char[] buffer) method. We iterate until the
        // Reader return -1 which means there's no more data to
        // read. We use the StringWriter class to produce the string.
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try (is) {
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            }
            return writer.toString();
        }
        return "";
    }
}
