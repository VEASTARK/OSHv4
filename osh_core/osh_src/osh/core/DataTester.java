package osh.core;

import osh.OSH;
import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.logging.IGlobalLogger;
import osh.core.logging.OSHGlobalLogger;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.eal.hal.HALRealTimeDriver;
import osh.registry.ComRegistry;
import osh.registry.DriverRegistry;
import osh.registry.OCRegistry;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

import java.time.ZoneId;
import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public final class DataTester {

    private DataTester() {

    }

    public static void main(String[] args) throws OSHException {
        OSH osh = new OSH();

        SystemLoggerConfiguration systemLoggingConfiguration = new SystemLoggerConfiguration(
                "DEBUG",
                true, //systemLoggingToConsoleActive
                true, //systemLoggingToFileActive
                false,
                true,
                true,
                "test");

        IGlobalLogger globalLogger = new OSHGlobalLogger(osh, systemLoggingConfiguration);
        osh.setLogger(globalLogger);

        osh.getOSHStatusObj().setIsSimulation(true);

        HALRealTimeDriver realTimeDriver = new HALRealTimeDriver(
                globalLogger,
                ZoneId.of("UTC"),
                true,
                true,
                1,
                0);
        osh.setTimer(realTimeDriver);

        ComRegistry comRegistry = new ComRegistry(osh);
        OCRegistry ocRegistry = new OCRegistry(osh);
        DriverRegistry driverRegistry = new DriverRegistry(osh);

        osh.setDriverRegistry(driverRegistry);
        osh.setExternalRegistry(comRegistry);
        osh.setOCRegistry(ocRegistry);

        DataBroker dataCustodian = new DataBroker(UUID.randomUUID(), osh);
        dataCustodian.onSystemIsUp();

        DataTester dt = new DataTester();

        DataTester.driverCom dc = new driverCom(osh, dataCustodian);
        DataTester.ocCom oc = new ocCom(osh, dataCustodian);

//		dc.sendEvent();
//		oc.sendEvent();

        dc.sendState();
        oc.sendState();

        ocRegistry.flushAllQueues();
        driverRegistry.flushAllQueues();

        ocRegistry.flushAllQueues();
        driverRegistry.flushAllQueues();

    }


    public static class driverCom extends OSHComponent implements IEventTypeReceiver, IHasState {

        final UUID uuid = UUID.randomUUID();

        public driverCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
            super(theOrganicSmartHome);
            ((OSH) this.getOSH()).getDriverRegistry().register(StartDeviceRequest.class, this);
            dc.registerDataReachThroughEvent(this.uuid, StartDeviceRequest.class, RegistryType.OC, RegistryType.DRIVER);

            ((OSH) this.getOSH()).getDriverRegistry().registerStateChangeListener(PlsStateExchange.class, this);
            dc.registerDataReachThroughState(this.uuid, PlsStateExchange.class, RegistryType.OC, RegistryType.DRIVER);
        }

        public void sendEvent() {
            StopDeviceRequest sdr = new StopDeviceRequest(this.uuid, UUID.randomUUID(), 0);
            ((OSH) this.getOSH()).getDriverRegistry().sendEvent(StopDeviceRequest.class, sdr);
        }

        public void sendState() {
            EpsStateExchange pls = new EpsStateExchange(this.uuid, 0);
            ((OSH) this.getOSH()).getDriverRegistry().setState(EpsStateExchange.class, this, pls);
            EpsPlsStateExchange epsPls = new EpsPlsStateExchange(this.uuid, 0, null, null, 0, 0, 0, 0, 0, false);
            ((OSH) this.getOSH()).getDriverRegistry().setState(EpsPlsStateExchange.class, this, epsPls);
        }

        @Override
        public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
            System.out.println("driverCom received Event, type: " + type);
            System.out.println("sender is: " + event.getSender() + ", this is: " + this.uuid);
        }

        @Override
        public Object getSyncObject() {
            return this;
        }

        @Override
        public UUID getUUID() {
            return this.uuid;
        }

    }

    public static class ocCom extends OSHComponent implements IEventTypeReceiver, IHasState {

        final UUID uuid = UUID.randomUUID();

        public ocCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
            super(theOrganicSmartHome);
            ((OSH) this.getOSH()).getOCRegistry().register(StopDeviceRequest.class, this);
            dc.registerDataReachThroughEvent(this.uuid, StopDeviceRequest.class, RegistryType.DRIVER, RegistryType.OC);

            ((OSH) this.getOSH()).getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
            dc.registerDataReachThroughState(this.uuid, EpsStateExchange.class, RegistryType.DRIVER, RegistryType.OC);
        }

        public void sendEvent() {
            StartDeviceRequest sdr = new StartDeviceRequest(this.uuid, UUID.randomUUID(), 0);
            ((OSH) this.getOSH()).getOCRegistry().sendEvent(StartDeviceRequest.class, sdr);
        }

        public void sendState() {
            PlsStateExchange pls = new PlsStateExchange(this.uuid, 0);
            ((OSH) this.getOSH()).getOCRegistry().setState(PlsStateExchange.class, this, pls);
        }

        @Override
        public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
            if (event instanceof StateChangedExchange) {
                System.out.println("ocCom received StateChangedExchange, type: " + ((StateChangedExchange) event).getType());

                if (((StateChangedExchange) event).getType().equals(EpsStateExchange.class)) {
                    EpsStateExchange ex = ((OSH) this.getOSH()).getOCRegistry().getState(EpsStateExchange.class, ((StateChangedExchange) event).getStatefulEntity());
                    System.out.println("sender is: " + ex.getSender() + ", this is: " + this.uuid);
                }


            } else {
                System.out.println("ocCom received Event, type: " + type);
                System.out.println("sender is: " + event.getSender() + ", this is: " + this.uuid);
            }
        }

        @Override
        public Object getSyncObject() {
            return this;
        }

        @Override
        public UUID getUUID() {
            return this.uuid;
        }

    }
}
