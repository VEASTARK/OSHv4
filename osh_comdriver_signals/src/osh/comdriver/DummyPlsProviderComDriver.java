package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class DummyPlsProviderComDriver extends CALComDriver {

    public DummyPlsProviderComDriver(IOSH osh, UUID deviceID, OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }

}
