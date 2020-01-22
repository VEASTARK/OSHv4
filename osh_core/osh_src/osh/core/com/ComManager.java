package osh.core.com;

import osh.OSHComponent;
import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.cal.IComDataSubscriber;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.oc.IOCCALDataPublisher;
import osh.core.oc.IOCCALDataSubscriber;
import osh.registry.Registry.OCRegistry;
import osh.registry.interfaces.IProvidesIdentity;

import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser
 */
public abstract class ComManager
        extends OSHComponent
        implements IRealTimeSubscriber,
        ILifeCycleListener,
        IComDataSubscriber,
        IOCCALDataPublisher,
        IProvidesIdentity {

    private CALComDriver comDriver;
    private final UUID uuid;


    /**
     * CONSTRUCTOR
     *
     * @param oc
     * @param uuid
     */
    public ComManager(IOSHOC oc, UUID uuid) {
        super(oc);
        this.uuid = uuid;
    }


    @Override
    public IOSHOC getOSH() {
        return (IOSHOC) super.getOSH();
    }


    @Override
    public OSHComponent getSyncObject() {
        return this;
    }

    @Override
    public void setOcDataSubscriber(IOCCALDataSubscriber monitorObject) {
        this.comDriver = (CALComDriver) monitorObject;
    }

    @Override
    public void removeOcDataSubscriber(IOCCALDataSubscriber monitorObject) {
        this.comDriver = null;
    }

    public CALComDriver getComDriver() {
        return this.comDriver;
    }

    @Override
    public UUID getUUID() {
        return this.uuid;
    }


    @Override
    public void updateOcDataSubscriber(ICALExchange calExchange) {
        if (this.comDriver != null) {
            this.comDriver.onDataFromOcComponent(calExchange);
        } else {
            //NOTHING
            //TODO: error message/exception
            throw new IllegalArgumentException("No ComDriver available.");
        }
    }

    /**
     * the observer in the design pattern (called method)
     */
    @Override
    public final void onDataFromCALDriver(ICALExchange exchangeObject) {
        synchronized (this.getSyncObject()) {
            this.onDriverUpdate(exchangeObject);
        }
    }

    public abstract void onDriverUpdate(ICALExchange exchangeObject);


    @Override
    public void onSystemRunning() {
        //...in case of use please override
    }

    @Override
    public void onSystemShutdown() {
        //...in case of use please override
    }

    @Override
    public void onSystemIsUp() throws OSHException {
    }

    @Override
    public void onSystemHalt() {
        //...in case of use please override
    }

    @Override
    public void onSystemResume() {
        //...in case of use please override
    }

    @Override
    public void onSystemError() {
        //...in case of use please override
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        //...in case of use please override
    }

    protected OCRegistry getOCRegistry() {
        return this.getOSH().getOCRegistry();
    }
}
