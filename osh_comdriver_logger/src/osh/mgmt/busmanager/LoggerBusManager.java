package osh.mgmt.busmanager;

import osh.core.bus.BusManager;
import osh.core.interfaces.IOSHOC;
import osh.eal.hal.exchange.IHALExchange;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public abstract class LoggerBusManager extends BusManager implements IDataRegistryListener {

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public LoggerBusManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onDriverUpdate(IHALExchange exchangeObject) {
        //NOTHING
    }

}
