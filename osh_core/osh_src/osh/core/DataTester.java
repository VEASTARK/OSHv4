package osh.core;

import osh.OSH;
import osh.OSHComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.core.logging.IGlobalLogger;
import osh.core.logging.OSHGlobalLogger;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.eal.hal.HALRealTimeDriver;
import osh.registry.DataRegistry.ComRegistry;
import osh.registry.DataRegistry.DriverRegistry;
import osh.registry.DataRegistry.OCRegistry;
import osh.registry.interfaces.IDataRegistryListener;
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

        ComRegistry comRegistry = new ComRegistry(osh, true);
        OCRegistry ocRegistry = new OCRegistry(osh, true);
        DriverRegistry driverRegistry = new DriverRegistry(osh, true);

        osh.setDriverRegistry(driverRegistry);
        osh.setComRegistry(comRegistry);
        osh.setOCRegistry(ocRegistry);

        DataBroker dataCustodian = new DataBroker(osh, UUID.randomUUID());
        dataCustodian.onSystemIsUp();

        DataTester dt = new DataTester();

        DataTester.driverCom dc = new driverCom(osh, dataCustodian);
        DataTester.ocCom oc = new ocCom(osh, dataCustodian);

        dc.sendEvent();
        oc.sendEvent();

        dc.sendState();
        oc.sendState();

        ocRegistry.flushQueue();
        driverRegistry.flushQueue();

        ocRegistry.flushQueue();
        driverRegistry.flushQueue();

    }


    public static class driverCom extends OSHComponent implements IDataRegistryListener, IHasState {

        final UUID uuid = UUID.randomUUID();

        public driverCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
            super(theOrganicSmartHome);
            ((OSH) this.getOSH()).getDriverRegistry().subscribe(StartDeviceRequest.class, this);
            dc.registerDataReachThrough(this.uuid, StartDeviceRequest.class, RegistryType.OC, RegistryType.DRIVER);

            ((OSH) this.getOSH()).getDriverRegistry().subscribe(PlsStateExchange.class, this);
            dc.registerDataReachThrough(this.uuid, PlsStateExchange.class, RegistryType.OC, RegistryType.DRIVER);
        }

        public void sendEvent() {
            StopDeviceRequest sdr = new StopDeviceRequest(this.uuid, UUID.randomUUID(), 0);
            ((OSH) this.getOSH()).getDriverRegistry().publish(StopDeviceRequest.class, sdr);
        }

        public void sendState() {
            EpsStateExchange pls = new EpsStateExchange(this.uuid, 0);
            ((OSH) this.getOSH()).getDriverRegistry().publish(EpsStateExchange.class, this, pls);
            EpsPlsStateExchange epsPls = new EpsPlsStateExchange(this.uuid, 0, null, null, 0, 0, 0, 0, 0, false);
            ((OSH) this.getOSH()).getDriverRegistry().publish(EpsPlsStateExchange.class, this, epsPls);
        }

        @Override
        public <T extends AbstractExchange> void onExchange(T exchange) {
            System.out.println("driverCom received Event, type: " + exchange.getClass());
            System.out.println("sender is: " + exchange.getSender() + ", this is: " + this.uuid);
        }

        @Override
        public UUID getUUID() {
            return this.uuid;
        }

    }

    public static class ocCom extends OSHComponent implements IDataRegistryListener, IHasState {

        final UUID uuid = UUID.randomUUID();

        public ocCom(IOSH theOrganicSmartHome, DataBroker dc) throws OSHException {
            super(theOrganicSmartHome);
            ((OSH) this.getOSH()).getOCRegistry().subscribe(StopDeviceRequest.class, this);
            dc.registerDataReachThrough(this.uuid, StopDeviceRequest.class, RegistryType.DRIVER, RegistryType.OC);

            ((OSH) this.getOSH()).getOCRegistry().subscribe(EpsStateExchange.class, this);
            dc.registerDataReachThrough(this.uuid, EpsStateExchange.class, RegistryType.DRIVER, RegistryType.OC);

            ((OSH) this.getOSH()).getOCRegistry().subscribe(EpsPlsStateExchange.class, this);
            dc.registerDataReachThrough(this.uuid, EpsPlsStateExchange.class, RegistryType.DRIVER, RegistryType.OC);
        }

        public void sendEvent() {
            StartDeviceRequest sdr = new StartDeviceRequest(this.uuid, UUID.randomUUID(), 0);
            ((OSH) this.getOSH()).getOCRegistry().publish(StartDeviceRequest.class, sdr);
        }

        public void sendState() {
            PlsStateExchange pls = new PlsStateExchange(this.uuid, 0);
            ((OSH) this.getOSH()).getOCRegistry().publish(PlsStateExchange.class, this, pls);
        }

        @Override
        public <T extends AbstractExchange> void onExchange(T exchange) {

            System.out.println("ocCom received Event, type: " + exchange.getClass());
            System.out.println("sender is: " + exchange.getSender() + ", this is: " + this.uuid);
        }

        @Override
        public UUID getUUID() {
            return this.uuid;
        }
    }
}
