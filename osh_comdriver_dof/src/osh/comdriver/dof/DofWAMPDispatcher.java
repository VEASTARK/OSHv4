package osh.comdriver.dof;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class DofWAMPDispatcher {

    // Scheduler
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Scheduler rxScheduler = Schedulers.from(this.executor);
    private final IGlobalLogger logger;
//	private String realm = "realm1";
    private final String url = "ws://wamp-router:8080/ws";
    private final String realm = "eshl";
    private final ObjectMapper mapper;
    // (Miele) UID (sic!) -> device dof
    private final Map<Integer, Integer> deviceData;
    private final ReentrantReadWriteLock deviceLock = new ReentrantReadWriteLock();
    Subscription onDataSubscription;
    private WampClient client;

    /**
     * CONSTRUCTOR
     *
     * @param logger
     */
    public DofWAMPDispatcher(IGlobalLogger logger) {
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
     * @return the current dof of the device with the provided id
     */
    public Integer getDeviceDof(Integer id) {
        this.deviceLock.readLock().lock();
        Integer dof = this.deviceData.get(id);
        this.deviceLock.readLock().unlock();
        return dof;
    }

    /**
     * Collect all information about Miele devices and provide it (to
     * MieleGatewayDriver)
     *
     * @return all currently known device dof information
     */
    public Map<Integer, Integer> getDeviceMap() {
        this.deviceLock.readLock().lock();
        HashMap<Integer, Integer> devices = new HashMap<>(this.deviceData);
        this.deviceLock.readLock().unlock();
        return devices;
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
                            System.out.println("Session status changed to " + state);
                            if (state instanceof WampClient.ConnectedState) {
                                // SUBSCRIBE to a topic and receive events
                                this.onDataSubscription = this.client.makeSubscription("eshl.preferences.v1.dof")
                                        .observeOn(this.rxScheduler)
                                        .subscribe(
                                                ev -> {
                                                    if (ev.arguments() == null || ev.arguments().size() < 1)
                                                        return; // error

                                                    JsonNode eventNode = ev.arguments().get(0);
                                                    if (eventNode.isNull()) return;

                                                    try {
                                                        Map<Integer, Integer> map =
                                                                this.mapper.convertValue(eventNode, new TypeReference<>() {
                                                                });

                                                        this.deviceLock.writeLock().lock();
                                                        this.deviceData.clear();
                                                        this.deviceData.putAll(map);
                                                        this.notifyAll();
                                                        this.deviceLock.writeLock().unlock();

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
