package osh.mgmt.busmanager;

import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.eal.hal.exchange.IHALExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class WagoBusManager extends BusManager {

    public WagoBusManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

//		getTimer().registerComponent(this, 1);

//		this.ocRegistry.register(NAME.class, this);
//		this.ocRegistry.registerStateChangeListener(NAME.class, this);
    }

    @Override
    public void onDriverUpdate(IHALExchange exchangeObject) {
        //NOTHING
    }

}
