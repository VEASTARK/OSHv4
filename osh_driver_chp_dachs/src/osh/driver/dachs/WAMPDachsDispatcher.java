package osh.driver.dachs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.WAMPDachsChpDriver;
import rx.Scheduler;
import rx.Subscription;
import rx.schedulers.Schedulers;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Ingo Mauser, Jan Mueller
 */
public class WAMPDachsDispatcher {

    private final IGlobalLogger globalLogger;
    private final WAMPDachsChpDriver dachsDriver;

    private final String url = "ws://wamp-router:8080/ws";
    private final String realm = "eshl";
    private final String wampTopic = "eshl.dachs.v2.readout.information";

    private WampClient client;

    // Scheduler
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler rxScheduler = Schedulers.from(this.executor);

    private final DachsDriverDetails dachsDetails;
    private final ReentrantLock dachsDetailsLock = new ReentrantLock();
    private Subscription onDataSubscription;

    private final ObjectMapper mapper;


    /**
     * CONSTRUCTOR
     */
    public WAMPDachsDispatcher(
            IGlobalLogger globalLogger,
            WAMPDachsChpDriver dachsDriver) {

        this.mapper = new ObjectMapper();
        this.globalLogger = globalLogger;
        this.dachsDriver = dachsDriver;

        // get information from DACHS via WAMP and save into dachsDetails
        this.subscribeForWampUpdates();

        this.dachsDetails = new DachsDriverDetails(
                dachsDriver.getDeviceID(),
                dachsDriver.getTimer().getUnixTime());
    }


    public void sendPowerRequest(boolean setOn) {
        if (this.globalLogger != null) {
            this.globalLogger.logDebug("doPowerRequest()");
        }
//		List <NameValuePair> datalist = new ArrayList <NameValuePair>(); 
        String jsonInString;
        if (setOn) {
            jsonInString = "{\"Stromf_Ew.Anforderung_GLT.bAktiv\" : \"1\", \"Stromf_Ew.Anforderung_GLT.bAnzahlModule\" : \"1\"}";
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "1"));
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAnzahlModule", "1"));
        } else {
            jsonInString = "{\"Stromf_Ew.Anforderung_GLT.bAktiv\" : \"0\", \"Stromf_Ew.Anforderung_GLT.bAnzahlModule\" : \"1\"}";
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAktiv", "0"));
//			datalist.add(new BasicNameValuePair("Stromf_Ew.Anforderung_GLT.bAnzahlModule", "1"));
        }

//		try {
//			jsonInString = mapper.writeValueAsString(datalist);
//		} catch (JsonProcessingException e) {
//			e.printStackTrace();
//		}

        this.client.call("eshl.dachs.v1.request.powerrequest", jsonInString)
                .observeOn(this.rxScheduler)
                .subscribe(this.globalLogger::logInfo, err -> this.globalLogger.logError("sending command failed", err));
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
                                this.onDataSubscription = this.client.makeSubscription(this.wampTopic)
                                        .observeOn(this.rxScheduler)
                                        .subscribe(
                                                ev -> {
                                                    if (ev.arguments() == null || ev.arguments().size() < 1)
                                                        return; // error

                                                    JsonNode eventNode = ev.arguments().get(0);
                                                    if (eventNode.isNull()) return;

                                                    try {
                                                        Map<String, String> map =
                                                                this.mapper.convertValue(eventNode, new TypeReference<>() {
                                                                });

                                                        this.dachsDetailsLock.lock();
                                                        this.dachsDetails.setValues(map);
                                                        this.dachsDriver.processDachsDetails(this.dachsDetails);
                                                        this.notifyAll();
                                                        this.dachsDetailsLock.lock();
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


    public DachsDriverDetails getDachsDetails() {
        return this.dachsDetails;
    }

}
