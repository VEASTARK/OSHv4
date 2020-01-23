package osh.eal.hal.exchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Please remeber cloning!
 *
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public abstract class HALObserverExchange extends HALExchange {

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public HALObserverExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }


}
