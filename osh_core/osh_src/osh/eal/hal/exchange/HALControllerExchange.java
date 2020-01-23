package osh.eal.hal.exchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Florian Allerding
 */
public abstract class HALControllerExchange extends HALExchange {
    public HALControllerExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }
}
