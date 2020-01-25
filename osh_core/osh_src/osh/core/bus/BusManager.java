package osh.core.bus;

import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.IOCHALDataPublisher;
import osh.core.oc.IOCHALDataSubscriber;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.IDriverDataSubscriber;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.time.TimeExchange;
import osh.registry.interfaces.IProvidesIdentity;
import osh.registry.interfaces.ITimeRegistryListener;

import java.util.UUID;

/**
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 */
public abstract class BusManager extends OCComponent
        implements ITimeRegistryListener,
        ILifeCycleListener,
        IDriverDataSubscriber,
        IOCHALDataPublisher,
        IProvidesIdentity {

    private HALBusDriver busDriver;
    private final UUID uuid;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public BusManager(IOSHOC osh, UUID uuid) {
        super(osh);
        this.uuid = uuid;
    }


    @Override
    public IOSHOC getOSH() {
        return super.getOSH();
    }


    @Override
    public void setOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
        this.busDriver = (HALBusDriver) monitorObject;
    }

    @Override
    public void removeOcDataSubscriber(IOCHALDataSubscriber monitorObject) {
        this.busDriver = null;
    }


    public HALBusDriver getBusDriver() {
        return this.busDriver;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }


    @Override
    public void updateOcDataSubscriber(IHALExchange halExchange) {
        if (this.busDriver != null) {
            this.busDriver.onDataFromOcComponent(halExchange);
        } else {
            //NOTHING
            //TODO: error message/exception
        }
    }

    @Override
    public final void onDataFromCALDriver(IHALExchange exchangeObject) {
        synchronized (this) {
            this.onDriverUpdate(exchangeObject);
        }
    }

    public abstract void onDriverUpdate(IHALExchange exchangeObject);

    @Override
    public void onSystemRunning() {
        //NOTHING
    }

    @Override
    public void onSystemShutdown() {
        //NOTHING
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        //NOTHING
    }

    @Override
    public void onSystemHalt() {
        //NOTHING
    }

    @Override
    public void onSystemResume() {
        //NOTHING
    }

    @Override
    public void onSystemError() {
        //NOTHING
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        //NOTHING
    }

}
