package osh.busdriver;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.eal.hal.HALBusDriver;
import osh.eal.hal.exchange.IHALExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class WagoWAMPBusDriver extends HALBusDriver {


    public WagoWAMPBusDriver(IOSH osh, UUID deviceID, OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
        //NOTHING
    }

    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        // NOTHING
    }
}
