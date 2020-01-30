package osh.busdriver;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import osh.busdriver.mielegateway.MieleGatewayRESTDispatcher;
import osh.busdriver.mielegateway.data.MieleApplianceRawDataREST;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataREST;
import osh.busdriver.mielegateway.data.MieleDuration;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails.ConnectionStatus;
import osh.datatypes.registry.details.common.StartTimeDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.en50523.EN50523DeviceState;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.uuid.UUIDGenerationHelperMiele;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BusDriver for Miele Homebus Gateway at KIT
 *
 * @author Kaibin Bao, Ingo Mauser
 */
//FIXME: Error handling, if device is (temporarily not found by Miele GW)
public class MieleGatewayRESTBusDriver extends HALBusDriver implements Runnable, IDataRegistryListener {

    private final String mieleGatewayHost;
    private MieleGatewayRESTDispatcher mieleGatewayDispatcher;
    private InetAddress mieleGatewayAddr;

    private String mieleGatewayUsername;
    private String mieleGatewayPassword;

    private Map<UUID, Map<String, String>> deviceProperties;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws UnknownHostException
     */
    public MieleGatewayRESTBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws UnknownHostException {
        super(osh, deviceID, driverConfig);

        this.mieleGatewayHost = driverConfig.getParameter("mielegatewayhost");
        this.mieleGatewayAddr = InetAddress.getByName(this.mieleGatewayHost);

        this.mieleGatewayUsername = driverConfig.getParameter("mielegatewayusername");
        this.mieleGatewayPassword = driverConfig.getParameter("mielegatewaypassword");

        this.deviceProperties = new HashMap<>();
    }

    static private StartTimeDetails createStartTimeDetails(
            final UUID devUUID,
            ZonedDateTime timestamp,
            MieleDeviceHomeBusDataREST dev) {

        StartTimeDetails startDetails = new StartTimeDetails(devUUID, timestamp);
        startDetails.setStartTime(-1);

        if (dev.getDeviceDetails() != null) {
            MieleDuration mieleStartTime = dev.getDeviceDetails().getStartTime();

            if (mieleStartTime != null) {
                int startTime = mieleStartTime.duration();

                if (startTime >= 0) {
                    ZonedDateTime now = ZonedDateTime.now();
                    ZonedDateTime start = ZonedDateTime.now();

                    start.withMinute(startTime % 60);
                    start.withHour(startTime / 60);

                    if (start.isBefore(now)) {
                        start.plusDays(1);
                    }

                    startDetails.setStartTime(start.toEpochSecond());
                }
            }
        }

        return startDetails;
    }

    static private GenericApplianceDriverDetails createApplianceDetails(
            UUID uuid,
            ZonedDateTime timestamp,
            MieleDeviceHomeBusDataREST dev) throws OSHException {

        GenericApplianceDriverDetails details = new GenericApplianceDriverDetails(uuid, timestamp);
        MieleApplianceRawDataREST devDetails = dev.getDeviceDetails();

        if (devDetails == null) {
            throw new OSHException("can't get device details");
        }

        if (devDetails.getStartCommandUrl() == null && dev.getState() == EN50523DeviceState.PROGRAMMED) {
            details.setState(EN50523DeviceState.STANDBY);
        } else {
            details.setState(dev.getState());
        }

        return details;
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // create dispatcher for
        // connection to Miele gateway
        this.mieleGatewayDispatcher = new MieleGatewayRESTDispatcher(
                this.mieleGatewayHost,
                this.mieleGatewayUsername,
                this.mieleGatewayPassword,
                this.getGlobalLogger());

        new Thread(this, "push proxy of Miele bus driver " + this.mieleGatewayHost).start();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.mieleGatewayDispatcher) {
                try { // wait for new data
                    this.mieleGatewayDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }

                ZonedDateTime timestamp = this.getTimeDriver().getCurrentTime();

                if (this.mieleGatewayDispatcher.getDeviceData().isEmpty()) { // an error has occurred
                    for (UUID uuid : this.deviceProperties.keySet()) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().publish(BusDeviceStatusDetails.class, bs);
                    }
                }

                for (MieleDeviceHomeBusDataREST dev : this.mieleGatewayDispatcher.getDeviceData()) {
                    // build UUID
                    long uuidHigh = UUIDGenerationHelperMiele.getMieleUUIDHigherPart(dev.getUid());
                    long uuidLow;
                    try {
                        uuidLow = UUIDGenerationHelperMiele.getHomeApplianceUUIDLowerPart((short) dev.getDeviceClass(), this.mieleGatewayAddr);
                    } catch (Exception e) {
                        this.getGlobalLogger().logError("should not happen: UUID generation failed", e);
                        continue;
                    }
                    final UUID devUUID = new UUID(uuidHigh, uuidLow);


                    // register UUID as command receiver to the registry
                    if (!this.deviceProperties.containsKey(devUUID)) { // device already known?
                        // register device
                        this.getDriverRegistry().subscribe(StartDeviceRequest.class, devUUID, this);
                        this.getDriverRegistry().subscribe(StopDeviceRequest.class, devUUID, this);
                        this.getDriverRegistry().subscribe(SwitchRequest.class, devUUID, this);
                        this.deviceProperties.put(devUUID, new HashMap<>());
                    }

                    // check if all data is available
                    if (dev.getDeviceDetails() == null) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().publish(BusDeviceStatusDetails.class, bs);
                        continue;
                    } else {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ATTACHED);
                        this.getDriverRegistry().publish(BusDeviceStatusDetails.class, bs);
                    }

                    // create program details
                    GenericApplianceProgramDriverDetails programDetails = new GenericApplianceProgramDriverDetails(devUUID, timestamp);
                    programDetails.setLoadProfiles(null);
                    programDetails.setProgramName(dev.getDeviceDetails().getProgramName());
                    programDetails.setPhaseName(dev.getDeviceDetails().getPhaseName());

                    // create Miele specific details
                    // duration
                    MieleApplianceDriverDetails mieleDetails = new MieleApplianceDriverDetails(devUUID, timestamp);
                    if (dev.getDuration() != null)
                        mieleDetails.setExpectedProgramDuration(dev.getDuration().duration() * 60);
                    else
                        mieleDetails.setExpectedProgramDuration(-1);

                    // remaining time
                    if (dev.getRemainingTime() != null)
                        mieleDetails.setProgramRemainingTime(dev.getRemainingTime().duration() * 60);
                    else
                        mieleDetails.setProgramRemainingTime(-1);

                    // start time
                    if (dev.getStartTime() != null) {
                        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

                        ZonedDateTime time = now.withHour(dev.getStartTime().hour()).withMinute(dev.getStartTime().minute()).withSecond(0);

                        if (time.isBefore(now))
                            time = time.plusDays(1);

                        mieleDetails.setStartTime(time.toEpochSecond());
                    } else
                        mieleDetails.setStartTime(-1);


                    // set state of the UUID
                    try {
                        this.getDriverRegistry().publish(
                                GenericApplianceDriverDetails.class,
                                createApplianceDetails(devUUID, timestamp, dev));
                        this.getDriverRegistry().publish(
                                StartTimeDetails.class,
                                createStartTimeDetails(devUUID, timestamp, dev));
                        this.getDriverRegistry().publish(
                                GenericApplianceProgramDriverDetails.class,
                                programDetails);
                        this.getDriverRegistry().publish(
                                MieleApplianceDriverDetails.class,
                                mieleDetails);

                    } catch (OSHException e1) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().publish(BusDeviceStatusDetails.class, bs);
                        this.getGlobalLogger().logError(e1);
                    }

                    // extract additional information for invoking commands
                    String detailsUrl = dev.getDetailsUrl();
                    // extract type and id from details url
                    if (detailsUrl != null) {
                        Map<String, String> devProps = this.deviceProperties.get(devUUID);
                        try {
                            URIBuilder uri = new URIBuilder(detailsUrl);
                            for (NameValuePair pair : uri.getQueryParams()) {
                                if ("type".equals(pair.getName()) || "id".equals(pair.getName())) {
                                    devProps.put(pair.getName(), pair.getValue());
                                }
                            }
                        } catch (URISyntaxException e) {
                            // nop. shit happens.
                            this.getGlobalLogger().logError("should not happen", e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        // NOTHING
    }



    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof CommandExchange) {
            UUID devUUID = ((CommandExchange) exchange).getReceiver();
            Map<String, String> devProps = this.deviceProperties.get(devUUID);

            if (devProps != null) { // known device?
                if (devProps.containsKey("type") && devProps.containsKey("id")) {
                    URIBuilder builder = new URIBuilder();
                    if (exchange instanceof StartDeviceRequest) {
                        builder
                                .setScheme("http")
                                .setHost(this.mieleGatewayHost)
                                .setPath("/homebus/device")
                                .setParameter("type", devProps.get("type"))
                                .setParameter("id", devProps.get("id"))
                                .setParameter("action", "start");
                        //						.setParameter("actionId", "start")
                        //						.setParameter("argumentCount", "0");
                    } else {
                        if (exchange instanceof StopDeviceRequest) {
                            builder
                                    .setScheme("http")
                                    .setHost(this.mieleGatewayHost)
                                    .setPath("/homebus/device")
                                    .setParameter("type", devProps.get("type"))
                                    .setParameter("id", devProps.get("id"))
                                    .setParameter("action", "stop");
                        } else {
                            if (exchange instanceof SwitchRequest) {
                                builder
                                        .setScheme("http")
                                        .setHost(this.mieleGatewayHost)
                                        .setPath("/homebus/device")
                                        .setParameter("type", devProps.get("type"))
                                        .setParameter("id", devProps.get("id"))
                                        .setParameter("action", ((SwitchRequest) exchange).isTurnOn() ? "switchOn" : "switchOff");
                            } else {
                                return;
                            }
                        }
                    }

                    try {
                        this.mieleGatewayDispatcher.sendCommand(builder.build().toString());
                    } catch (URISyntaxException e) {
                        this.getGlobalLogger().logWarning("miele gateway disconnected?", e);
                    }
                }
            }
        }
    }

    /* CURRENTLY NOT IN USE (BUT KEEP IT!)
    private final int MIELE_GW_UID_HOMEBUS = 0x48425553; // "HBUS" for HOMEBUS
    private final short MIELE_GW_APPLIANCE_TYPE = 0x4757; // "GW"
    public UUID getUUID() {
        long uuidHigh = getUUIDHigherPart(MIELE_GW_UID_HOMEBUS, MIELE_BRAND_AND_MANUFACTURER_ID, MIELE_BRAND_AND_MANUFACTURER_ID);
        long uuidLow;
        try {
            uuidLow = getUUIDLowerPart(MIELE_GW_APPLIANCE_TYPE, mieleGatewayAddr);
        } catch (ControllerBoxException e) {
            getGlobalLogger().logError("should not happen", e);
            return null;
        }

        return new UUID( uuidHigh, uuidLow );
    }
    */

}
