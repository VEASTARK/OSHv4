package osh.eal.hal;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.BusDeviceTypes;
import osh.core.bus.BusManager;
import osh.core.interfaces.IOSH;
import osh.core.oc.IOCHALDataSubscriber;
import osh.eal.hal.exchange.IHALExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class HALBusDriver extends HALDriver implements IDriverDataPublisher, IOCHALDataSubscriber {

    private BusManager assignedBusManager;
    private BusDeviceTypes busDeviceType;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public HALBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
        // currently NOTHING
    }


    /**
     * @return the assigned ComManager
     */
    public BusManager getAssignedBusManager() {
        return this.assignedBusManager;
    }

    public BusDeviceTypes getBusDeviceType() {
        return this.busDeviceType;
    }

    public void setBusDeviceType(BusDeviceTypes busDeviceType) {
        this.busDeviceType = busDeviceType;
    }

    // HALdataObject

    /**
     * receive data from BusManager
     */
    @Override
    public void onDataFromOcComponent(IHALExchange exchangeObject) {
        this.updateDataFromBusManager(exchangeObject);
    }

    public abstract void updateDataFromBusManager(IHALExchange exchangeObject);

    // HALdataSubject

    @Override
    public final void setOcDataSubscriber(IDriverDataSubscriber monitorObject) {
        this.assignedBusManager = (BusManager) monitorObject;
    }

    @Override
    public final void updateOcDataSubscriber(IHALExchange halExchange) {
        this.assignedBusManager.onDataFromCALDriver(halExchange);
    }

    public final void notifyBusManager(IHALExchange exchangeObject) {
        this.updateOcDataSubscriber(exchangeObject);
    }

}
