package osh.cal;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * abstract class for the data object between the HAL and the O/C layer
 *
 * @author Florian Allerding
 */
public abstract class CALExchange
        implements ICALExchange {

    private UUID deviceID;
    private ZonedDateTime timestamp;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public CALExchange(UUID deviceID, ZonedDateTime timestamp) {
        super();

        this.deviceID = deviceID;
        this.timestamp = timestamp;
    }

    @Override
    public UUID getDeviceID() {
        return this.deviceID;
    }

    public void setDeviceID(UUID deviceID) {
        this.deviceID = deviceID;
    }

    @Override
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
