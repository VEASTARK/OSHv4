package osh.old.busdriver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.commands.SwitchRequest;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails;
import osh.datatypes.registry.details.common.BusDeviceStatusDetails.ConnectionStatus;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;
import osh.old.busdriver.wago.LowLevelWagoByteDetails;
import osh.old.busdriver.wago.SmartPlugException;
import osh.old.busdriver.wago.Wago750860ModuleType;
import osh.old.busdriver.wago.WagoTCPUDPDispatcher;
import osh.old.busdriver.wago.WagoTCPUDPDispatcher.UpdateListener;
import osh.old.busdriver.wago.data.*;
import osh.registry.interfaces.IEventTypeReceiver;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
@Deprecated
public class WagoTCPUDPBusDriver extends HALBusDriver implements UpdateListener, IEventTypeReceiver {

    private final String controllerHostname;
    private WagoTCPUDPDispatcher wagoControllerDispatcher;
    private long wagoControllerIdPart;
    private Set<UUID> knownUUIDs;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws OSHException
     */
    public WagoTCPUDPBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws OSHException {
        super(osh, deviceID, driverConfig);

        this.controllerHostname = driverConfig.getParameter("hostname");

        if (this.controllerHostname == null)
            throw new OSHException("bus driver config parameter hostname not set!");

        this.knownUUIDs = new HashSet<>();

        //TODO devices from variable... ?!?
    }

    private void connectToWagoController() throws OSHException {
        try {
            InetAddress addr = InetAddress.getByName(this.controllerHostname);

            // Port of Wago 750-860 Protocol is 9155
            this.wagoControllerIdPart = UUIDGenerationHelperWago.getUUIDLowerPart(addr, UUIDGenerationHelperWago.WAGO_750_860_DEFAULT_PORT);

            this.wagoControllerDispatcher = new WagoTCPUDPDispatcher(this.getGlobalLogger(), addr);
            this.wagoControllerDispatcher.registerUpdateListener(this);
        } catch (Exception e) {
            throw new OSHException(e);
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.connectToWagoController();

        this.getDriverRegistry().register(SwitchRequest.class, this);
    }

    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        // currently NOTHING
    }

    public void checkUUID(final UUID uuid, Wago750860ModuleType type)
            throws OSHException {
        if (!this.knownUUIDs.contains(uuid)) {
            this.knownUUIDs.add(uuid);

            BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid,
                    this.getTimer().getUnixTime(),
                    this.wagoControllerDispatcher.isConnected() ? ConnectionStatus.ATTACHED
                            : ConnectionStatus.ERROR);
            this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class,
                    bs);

            this.getDriverRegistry().register(SwitchRequest.class,
                    new IEventTypeReceiver() {
                        @Override
                        public Object getSyncObject() {
                            return WagoTCPUDPBusDriver.this.getSyncObject();
                        }

                        @Override
                        public <T extends EventExchange> void onQueueEventTypeReceived(
                                Class<T> type, T event) throws OSHException {
                            WagoTCPUDPBusDriver.this
                                    .onQueueEventTypeReceived(type, event);
                        }

                        @Override
                        public UUID getUUID() {
                            return uuid;
                        }
                    });
        }
    }

    @Override
    public void wagoUpdateEvent() {

        long now = this.getTimer().getUnixTime();
        boolean connected = this.wagoControllerDispatcher.isConnected();

        for (UUID uuid : this.knownUUIDs) {
            BusDeviceStatusDetails bs = new BusDeviceStatusDetails(uuid, now, connected ? ConnectionStatus.ATTACHED : ConnectionStatus.ERROR);
            this.getDriverRegistry().setStateOfSender(BusDeviceStatusDetails.class, bs);
        }

        // set metering data
        for (WagoPowerMeter meterData : this.wagoControllerDispatcher.getPowerData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(
                    Wago750860ModuleType.METER,
                    (short) meterData.getGroupId(),
                    (short) meterData.getMeterId()),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.METER);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        // set switch data
        for (WagoRelayData relay : this.wagoControllerDispatcher.getSwitchData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.SWITCH,
                    (short) relay.getId(), (short) 0),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.SWITCH);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        // set virtual switch data
        for (WagoVirtualSwitch vSwitch : this.wagoControllerDispatcher.getVirtualSwitchData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.VIRTUAL_SWITCH,
                    (short) vSwitch.getGroupId(), (short) vSwitch.getId()),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.VIRTUAL_SWITCH);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        // set vs data group
        for (WagoVirtualGroup vsg : this.wagoControllerDispatcher.getVirtualSwitchGroupData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.VIRTUAL_SWITCH,
                    (short) vsg.getGroupId(), UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.VIRTUAL_SWITCH);
            } catch (OSHException e) {
                e.printStackTrace();
            }

            LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, this.getTimer().getUnixTime());

            llDetail.setData(vsg.getByte());

            this.getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
        }

        // set digital in data
        for (WagoDiData di : this.wagoControllerDispatcher.getDigitalInData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.DIGITAL_INPUT,
                    (short) di.getGroupId(), (short) di.getId()),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.DIGITAL_INPUT);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        // set digital in data group
        for (WagoDiGroup dig : this.wagoControllerDispatcher.getDigitalInGroup()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.DIGITAL_INPUT,
                    (short) dig.getGroupId(), UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.DIGITAL_INPUT);
            } catch (OSHException e) {
                e.printStackTrace();
            }

            LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, this.getTimer().getUnixTime());

            llDetail.setData(dig.getByte());

            this.getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
        }


        // set digital out data
        for (WagoDoData do8 : this.wagoControllerDispatcher.getDigitalOutData()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.DIGITAL_OUTPUT,
                    (short) do8.getGroupId(), (short) do8.getId()),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.DIGITAL_OUTPUT);
            } catch (OSHException e) {
                e.printStackTrace();
            }
        }

        // set do8 data group
        for (WagoDoGroup vsg : this.wagoControllerDispatcher.getDigitalOutGroup()) {
            UUID uuid = new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(Wago750860ModuleType.DIGITAL_OUTPUT,
                    (short) vsg.getGroupId(), UUIDGenerationHelperWago.WAGO_750_860_GROUP_ID),
                    this.wagoControllerIdPart);

            try {
                this.checkUUID(uuid, Wago750860ModuleType.DIGITAL_OUTPUT);
            } catch (OSHException e) {
                e.printStackTrace();
            }

            LowLevelWagoByteDetails llDetail = new LowLevelWagoByteDetails(uuid, this.getTimer().getUnixTime());

            llDetail.setData(vsg.getByte());

            this.getDriverRegistry().setStateOfSender(LowLevelWagoByteDetails.class, llDetail);
        }

        // TODO: handle analog input, digital output, etc...
    }

    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(
            Class<T> type, T event) throws OSHException {

        if (event instanceof SwitchRequest) {
            SwitchRequest switchReq = (SwitchRequest) event;

            // sanity checks...
            UUID targetId = switchReq.getReceiver();
            if (targetId.getLeastSignificantBits() != this.wagoControllerIdPart)
                throw new OSHException("received command with wrong controller id");

            // extract wago target ids
            long higherPart = targetId.getMostSignificantBits();
            short moduleId = (short) ((higherPart >> 16) & 0xffff);
            short portId = (short) ((higherPart) & 0xffff);
            byte moduleType = (byte) ((higherPart >> 32) & 0xff);
            int uuidPrefix = (int) ((higherPart >> 32) & 0xffffff00);

            // further sanity checks
            if (portId > 7 || (portId != 0 && moduleType == Wago750860ModuleType.SWITCH.value()))
                throw new OSHException("received command with wrong port id");
            if (moduleType != Wago750860ModuleType.SWITCH.value() &&
                    moduleType != Wago750860ModuleType.VIRTUAL_SWITCH.value() &&
                    moduleType != Wago750860ModuleType.DIGITAL_OUTPUT.value())
                throw new OSHException("received command with wrong module type");
            if (uuidPrefix != UUIDGenerationHelperWago.WAGO_750_860_UUID_PREFIX)
                throw new OSHException("received command with invalid uuid");

            try {
                if (moduleType == Wago750860ModuleType.SWITCH.value())
                    this.wagoControllerDispatcher.setSwitch(moduleId, switchReq.getTurnOn());
                else if (moduleType == Wago750860ModuleType.VIRTUAL_SWITCH.value())
                    this.wagoControllerDispatcher.setVirtualSwitch(moduleId, portId, switchReq.getTurnOn());
                else this.wagoControllerDispatcher.setDigitalOutput(moduleId, portId, switchReq.getTurnOn());
            } catch (SmartPlugException e) {
                // pass on exception
                throw new OSHException(e);
            }
        }
    }

    @Override
    public UUID getUUID() {
        // construct own UUID
        return new UUID(UUIDGenerationHelperWago.getWago750860UUIDHigherPart(
                Wago750860ModuleType.CONTROLLER, (short) 0, (short) 0), this.wagoControllerIdPart);
    }

}
