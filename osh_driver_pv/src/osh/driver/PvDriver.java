package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PvDriver extends HALDeviceDriver {


    /**
     * CONSTRUCTOR
     *
     */
    public PvDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }


    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        //NOTHING
    }

}
