package osh.cal;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser, Till Schuberth
 */
public abstract class CALComExchange extends CALExchange {

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public CALComExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

}
