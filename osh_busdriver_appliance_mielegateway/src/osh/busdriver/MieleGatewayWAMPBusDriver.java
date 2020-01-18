package osh.busdriver;

import osh.busdriver.mielegateway.MieleGatewayWAMPDispatcher;
import osh.busdriver.mielegateway.data.MieleApplianceRawDataJSON;
import osh.busdriver.mielegateway.data.MieleDeviceHomeBusDataJSON;
import osh.busdriver.mielegateway.data.MieleDuration;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.CommandExchange;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails.ConnectionStatus;
import osh.datatypes.registry.details.common.StartTimeDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.en50523.EN50523DeviceState;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.utils.uuid.UUIDGenerationHelperMiele;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * BusDriver for Miele Homebus Gateway at KIT
 *
 * @author Kaibin Bao, Ingo Mauser
 */
public class MieleGatewayWAMPBusDriver extends HALBusDriver implements Runnable {

    private final String mieleGatewayHost;
    private MieleGatewayWAMPDispatcher mieleGatewayDispatcher;

    private InetAddress mieleGatewayAddr;

    private Map<UUID, Integer> deviceIds;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws UnknownHostException
     */
    public MieleGatewayWAMPBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws UnknownHostException {
        super(osh, deviceID, driverConfig);

        this.mieleGatewayHost = driverConfig.getParameter("mielegatewayhost");
        this.mieleGatewayAddr = InetAddress.getByName(this.mieleGatewayHost);

        this.deviceIds = new HashMap<>();
    }

    static private StartTimeDetails createStartTimeDetails(
            final UUID devUUID,
            long timestamp,
            MieleDeviceHomeBusDataJSON dev) {

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
            long timestamp,
            MieleDeviceHomeBusDataJSON dev) throws OSHException {

        GenericApplianceDriverDetails details = new GenericApplianceDriverDetails(uuid, timestamp);
        MieleApplianceRawDataJSON devDetails = dev.getDeviceDetails();

        if (devDetails == null) {
            throw new OSHException("can't get device details");
        }

        if ((dev.getState() == EN50523DeviceState.PROGRAMMED || dev.getState() == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART)
                && (dev.getActions() == null || !dev.getActions().contains("start"))) {
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
        this.mieleGatewayDispatcher = new MieleGatewayWAMPDispatcher(this.getGlobalLogger());

        new Thread(this, "push proxy of Miele bus driver to WAMP").start();
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

                long timestamp = this.getTimer().getUnixTime();

                if (this.mieleGatewayDispatcher.getDeviceData().isEmpty()) { // an error has occurred
                    for (UUID uuid : this.deviceIds.keySet()) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
                    }
                }

                for (MieleDeviceHomeBusDataJSON dev : this.mieleGatewayDispatcher.getDeviceData()) {
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

                    MieleGatewayWAMPBusDriver driver = this;

                    // register UUID as command receiver to the registry
                    if (!this.deviceIds.containsKey(devUUID)) { // device already known?
                        IEventTypeReceiver eventReceiver = new IEventTypeReceiver() {
                            @Override
                            public Object getSyncObject() {
                                return MieleGatewayWAMPBusDriver.this;
                            }

                            @Override
                            public UUID getUUID() {
                                return devUUID;
                            }

                            @Override
                            public <T extends EventExchange> void onQueueEventTypeReceived(
                                    Class<T> type, T event) {
//								this.onQueueEventTypeReceived(type, event);
                                driver.onQueueEventReceived(event, devUUID);
                            }
                        };

                        // register device
                        try {
                            this.getDriverRegistry().register(StartDeviceRequest.class, eventReceiver);
                            this.getDriverRegistry().register(StopDeviceRequest.class, eventReceiver);
                            this.getDriverRegistry().register(SwitchRequest.class, eventReceiver);
                            this.getDriverRegistry().registerStateChangeListener(ExpectedStartTimeExchange.class, eventReceiver);
                            this.deviceIds.put(devUUID, dev.getUid());
                        } catch (OSHException e) {
                            // nop. happens.
                            this.getGlobalLogger().logError("should not happen", e);
                        }
                    }
                }

                for (Entry<UUID, Integer> ent : this.deviceIds.entrySet()) {
                    final UUID devUUID = ent.getKey();
                    final MieleDeviceHomeBusDataJSON dev = this.mieleGatewayDispatcher.getDeviceData(ent.getValue());

                    // check if device is published by gateway at the moment
                    if (dev == null) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
                        continue;
                    }

                    // check if all data is available
                    if (dev.getDeviceDetails() == null) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
                        continue;
                    } else {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ATTACHED);
                        this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
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
                        long now = this.getTimer().getUnixTime();

                        ZonedDateTime time =
                                ZonedDateTime.ofInstant(Instant.ofEpochSecond(now),
                                        this.getTimer().getHostTimeZone());

                        time.withHour(dev.getStartTime().hour());
                        time.withMinute(dev.getStartTime().minute());
                        time.withSecond(0);

                        if (time.toEpochSecond() < now)
                            time.plusDays(1);

                        mieleDetails.setStartTime(time.toEpochSecond());
                    } else
                        mieleDetails.setStartTime(-1);


                    // set state of the UUID
                    try {
                        this.getDriverRegistry().setStateOfSender(
                                GenericApplianceDriverDetails.class,
                                createApplianceDetails(devUUID, timestamp, dev));
                        this.getDriverRegistry().setStateOfSender(
                                StartTimeDetails.class,
                                createStartTimeDetails(devUUID, timestamp, dev));
                        this.getDriverRegistry().setStateOfSender(
                                GenericApplianceProgramDriverDetails.class,
                                programDetails);
                        this.getDriverRegistry().setStateOfSender(
                                MieleApplianceDriverDetails.class,
                                mieleDetails);

                    } catch (OSHException e1) {
                        BusDeviceStatusDetails bs = new BusDeviceStatusDetails(devUUID, timestamp, ConnectionStatus.ERROR);
                        this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
                        this.getGlobalLogger().logError(e1);
                    }
                }
            }
        }
    }

    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        // NOTHING
    }

    public void onQueueEventReceived(EventExchange event, UUID deviceUUID) {
        if (event instanceof CommandExchange) {
            UUID devUUID = ((CommandExchange) event).getReceiver();
            Integer uid = this.deviceIds.get(devUUID);

            if (uid != null) { // known device?
                if (event instanceof StartDeviceRequest) {
                    this.mieleGatewayDispatcher.sendCommand("start", uid);
                } else if (event instanceof StopDeviceRequest) {
                    this.mieleGatewayDispatcher.sendCommand("stop", uid);
                } else if (event instanceof SwitchRequest) {
                    if (((SwitchRequest) event).isTurnOn()) {
                        this.mieleGatewayDispatcher.sendCommand("lighton", uid);
                    } else {
                        this.mieleGatewayDispatcher.sendCommand("lightoff", uid);
                    }
                }
            }
        } else if (event instanceof StateChangedExchange) {
            if (deviceUUID.equals(((StateChangedExchange) event).getStatefulEntity())) {
                if (((StateChangedExchange) event).getType().equals(ExpectedStartTimeExchange.class)) {

                    ExpectedStartTimeExchange este = this.getDriverRegistry().getState(ExpectedStartTimeExchange.class, ((StateChangedExchange) event).getStatefulEntity());
                    UUID devUUID = este.getSender();
                    Integer uid = this.deviceIds.get(devUUID);
                    this.mieleGatewayDispatcher.sendStartTimes(este.getExpectedStartTime(), uid);
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
