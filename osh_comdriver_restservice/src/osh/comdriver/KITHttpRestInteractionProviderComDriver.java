package osh.comdriver;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;

import java.util.UUID;


/**
 * @author Kaibin Bao
 */
public class KITHttpRestInteractionProviderComDriver extends HttpRestInteractionProviderBusDriver {

    public KITHttpRestInteractionProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }

    @Override
    String getEnvironment() {
        return "kit";
    }

}
