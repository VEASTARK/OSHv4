package osh.comdriver.wamp;

import com.fasterxml.jackson.core.type.TypeReference;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import rx.Scheduler;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Sebastian Kramer
 */
public class SignalsWAMPDispatcher {

    // Scheduler
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    final Scheduler rxScheduler = Schedulers.from(this.executor);
    private final IGlobalLogger logger;
    //	private String url = "ws://wamp-router:8080/ws";
//	private String realm = "eshl";
    private final String url = "ws://localhost:8080/ws";
    private final String realm = "realm1";
    private WampClient client;


    /**
     * CONSTRUCTOR
     *
     * @param logger
     * @throws MalformedURLException
     */
    public SignalsWAMPDispatcher(IGlobalLogger logger) {
        super();

        this.logger = logger;

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

        this.client.open();
    }

    public void sendEPS(Map<AncillaryCommodity, PriceSignal> priceSignals) {
        this.client.publish("eshl.signals.eps", priceSignals, new TypeReference<Map<AncillaryCommodity, PriceSignal>>() {
        })
                .observeOn(this.rxScheduler)
                .subscribe(this.logger::logInfo, err -> this.logger.logError("publishing eps failed", err));
    }

    public void sendPLS(Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignal) {
        this.client.publish("eshl.signals.pls", powerLimitSignal, new TypeReference<Map<AncillaryCommodity, PowerLimitSignal>>() {
        })
                .observeOn(this.rxScheduler)
                .subscribe(this.logger::logInfo, err -> this.logger.logError("publishing pls failed", err));
    }


}
