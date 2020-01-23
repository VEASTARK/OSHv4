package osh.busdriver.mielegateway;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import osh.busdriver.mielegateway.data.MieleApplianceRawDataREST;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataREST;
import osh.busdriver.mielegateway.data.MieleDeviceList;
import osh.core.logging.IGlobalLogger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Handling the connection to one miele gateway
 *
 * @author Kaibin Bao, Ingo Mauser
 */
@SuppressWarnings("deprecation")
public class MieleGatewayRESTDispatcher implements Runnable {

    final String homeBusUrl;
    private final IGlobalLogger logger;
    private final HttpClient httpclient;
    private final HttpContext httpcontext;
    // (Miele) UID (sic!) -> device state map
    private final HashMap<Integer, MieleDeviceHomeBusDataREST> deviceData;
    private final ReentrantReadWriteLock deviceDataLock = new ReentrantReadWriteLock();


    /**
     * CONSTRUCTOR
     *
     * @param gatewayHostAndPort miele gateway host and port address
     * @param username           miele gateway access username
     * @param password           miele gateway access password
     * @param logger             OSH logger for this dispatcher
     */
    public MieleGatewayRESTDispatcher(
            String gatewayHostAndPort,
            String username,
            String password,
            IGlobalLogger logger) {
        super();

        this.homeBusUrl = "http://" + gatewayHostAndPort + "/homebus/?language=en";

        CredentialsProvider httpCredentialsProvider = new BasicCredentialsProvider();
        httpCredentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));
        this.httpcontext = new BasicHttpContext();
        this.httpcontext.setAttribute(ClientContext.CREDS_PROVIDER, httpCredentialsProvider);

        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        ClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);

        this.httpclient = new DefaultHttpClient(cm);
        this.httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); // Default to HTTP 1.1 (connection persistence)
        this.httpclient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
        this.httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 1000);
        this.httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 1000);

        this.logger = logger;

        //this.parser = new MieleGatewayParser("http://" + device + "/homebus");
        this.deviceData = new HashMap<>();

        new Thread(this, "MieleGatewayDispatcher for " + gatewayHostAndPort).start();
    }

    /**
     * Collect information about Miele device and provide it
     * (to MieleGatewayDriver)
     *
     * @param id Miele UID (sic!)
     * @return Miele device data corresponding to the provided uid
     */
    public MieleDeviceHomeBusDataREST getDeviceData(Integer id) {
        MieleDeviceHomeBusDataREST dev;

        this.deviceDataLock.readLock().lock();
        try {
            return this.deviceData.get(id);
        } finally {
            this.deviceDataLock.readLock().unlock();
        }
    }

    /**
     * Collect all information about Miele devices and provide it
     * (to MieleGatewayDriver)
     *
     * @return a collection of all known device data
     */
    public Collection<MieleDeviceHomeBusDataREST> getDeviceData() {

        this.deviceDataLock.readLock().lock();
        try {
            return new ArrayList<>(this.deviceData.values());
        } finally {
            this.deviceDataLock.readLock().unlock();
        }
    }

    public void sendCommand(String url) {
        HttpGet httpget = new HttpGet(url);
        try {
            HttpResponse response = this.httpclient.execute(httpget, this.httpcontext);
            if (response.getStatusLine().getStatusCode() != 200) {
                this.logger.logWarning("miele@home bus driver: error sending command " + url);
            }
            EntityUtils.consume(response.getEntity());
        } catch (IOException e1) {
            httpget.abort();
            this.logger.logWarning("miele@home bus driver: error sending command " + url, e1);
        }
    }

    @Override
    public void run() {
        MieleDeviceList deviceList = new MieleDeviceList();
        JAXBContext context;

        // initialize empty device list
        deviceList.setDevices(Collections.emptyList());

        try {
            context = JAXBContext.newInstance(MieleDeviceList.class);
        } catch (JAXBException e1) {
            this.logger.logError("unable to initialize XML marshaller", e1);
            return;
        }

        while (true) {
            // fetch device list
            try {
                HttpGet httpget = new HttpGet(this.homeBusUrl);
                HttpResponse response = this.httpclient.execute(httpget, this.httpcontext);
                HttpEntity entity = response.getEntity();

                if (entity != null) {

                    // Process the XML
                    try (InputStream inputStream = entity.getContent()) {
                        // Create the EclipseLink JAXB (MOXy) Unmarshaller
                        Map<String, Object> jaxbProperties = new HashMap<>(2);
//			            jaxbProperties.put(JAXBContextProperties.MEDIA_TYPE, "application/xml");
//			            jaxbProperties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
                        JAXBContext jc = JAXBContext.newInstance(new Class[]{MieleDeviceList.class},
                                jaxbProperties);
                        Unmarshaller unmarshaller = jc.createUnmarshaller();

//						Unmarshaller unmarshaller = context.createUnmarshaller();
                        deviceList = (MieleDeviceList) unmarshaller.unmarshal(inputStream);

                        //DEBUG
//						StringReader reader = new StringReader("s");
//						deviceList = (MieleDeviceList) unmarshaller.unmarshal(reader);
                        //DEBUG END

                        if (this.logger == null) {
                            System.out.println(deviceList);
                        }
                    } catch (JAXBException e) {
                        if (this.logger != null) {
                            this.logger.logError("failed to unmarshal miele homebus xml", e);
                        } else {
                            e.printStackTrace();
                        }
                        deviceList.setDevices(Collections.emptyList()); // set empty list
                    } finally {
                        if (deviceList == null || deviceList.getDevices() == null || deviceList.getDevices().isEmpty()) {
                            deviceList = new MieleDeviceList();
                            if (this.logger != null) {
                                this.logger.logWarning("no miele devices in list");
                            } else {
                                System.out.println("no miele devices in list");
                            }
                        }
                        if (deviceList.getDevices() == null) {
                            deviceList.setDevices(Collections.emptyList()); // set empty list
                        }
                    }
                }
            } catch (IOException e1) {
                deviceList.setDevices(Collections.emptyList()); // set empty list
                if (this.logger != null) {
                    this.logger.logWarning("miele@home bus driver: failed to fetch device list; " + e1.getMessage());
                    this.logger.logInfo("miele@home bus driver: failed to fetch device list", e1);
                } else {
                    e1.printStackTrace();
                }
            }

            // fetch device details
            for (MieleDeviceHomeBusDataREST dev : deviceList.getDevices()) {
                try {
                    HttpGet httpget = new HttpGet(dev.getDetailsUrl());
                    HttpResponse response = this.httpclient.execute(httpget, this.httpcontext);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {

                        // Process the XML
                        try (InputStream inputStream = entity.getContent()) {
                            Unmarshaller unmarshaller = context.createUnmarshaller();
                            MieleApplianceRawDataREST deviceDetails = (MieleApplianceRawDataREST) unmarshaller.unmarshal(inputStream);
                            dev.setDeviceDetails(deviceDetails);
                        } catch (JAXBException e) {
                            if (this.logger != null) {
                                this.logger.logError("failed to unmarshal miele homebus detail xml", e);
                            }
                        }
                    }
                } catch (IOException e2) {
                    // ignore
                }
            }

            // store device state
            this.deviceDataLock.writeLock().lock();
            try {
                this.deviceData.clear();
                for (MieleDeviceHomeBusDataREST dev : deviceList.getDevices()) {
                    this.deviceData.put(dev.getUid(), dev);
                }
            } finally {
                this.deviceDataLock.writeLock().unlock();
            }

            this.notifyAll();

            // wait a second till next state fetch
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e3) {
                if (this.logger != null) {
                    this.logger.logError("sleep interrupted - miele@home bus driver dies right now...");
                }
                break;
            }
        }
    }
}
