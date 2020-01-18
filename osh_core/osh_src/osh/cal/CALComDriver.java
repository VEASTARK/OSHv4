package osh.cal;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.ComDeviceTypes;
import osh.core.com.ComManager;
import osh.core.interfaces.IOSH;
import osh.core.oc.IOCCALDataSubscriber;

import java.util.UUID;

/**
 * Superclass for all ComDrivers (devices with ComManager but without full O/C-Unit)
 *
 * @author Till Schuberth, Ingo Mauser
 */
public abstract class CALComDriver
        extends CALDriver
        implements IComDataPublisher, IOCCALDataSubscriber {

    private ComManager assignedComManager;
    private ComDeviceTypes comDeviceType;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public CALComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }

    /**
     * @return the assigned ComManager
     */
    public ComManager getAssignedComManager() {
        return this.assignedComManager;
    }

    public ComDeviceTypes getComDeviceType() {
        return this.comDeviceType;
    }

    public void setComDeviceType(ComDeviceTypes comDeviceType) {
        this.comDeviceType = comDeviceType;
    }

    // HALdataObject

    /**
     * receive data from ComManager
     */
    @Override
    public final void onDataFromOcComponent(ICALExchange exchangeObject) {
        this.updateDataFromComManager(exchangeObject);
    }

    public abstract void updateDataFromComManager(ICALExchange exchangeObject);

    // HALdataSubject

    @Override
    public final void setComDataSubscriber(IComDataSubscriber monitorObject) {
        this.assignedComManager = (ComManager) monitorObject;
    }

    @Override
    public final void removeComDataSubscriber(IComDataSubscriber monitorObject) {
        this.assignedComManager = null;
    }

    @Override
    public final void updateComDataSubscriber(ICALExchange halExchange) {
        this.assignedComManager.onDataFromCALDriver(halExchange);
    }

    public final void notifyComManager(ICALExchange exchangeObject) {
        this.updateComDataSubscriber(exchangeObject);
    }

}
