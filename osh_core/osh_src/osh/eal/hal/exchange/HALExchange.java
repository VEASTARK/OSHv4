package osh.eal.hal.exchange;

import java.util.UUID;

/**
 * abstract class for the data object between the HAL and the O/C layer
 *
 * @author Florian Allerding
 */
public abstract class HALExchange
        implements IHALExchange {

    private UUID deviceID;
    private long timestamp;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public HALExchange(UUID deviceID, long timestamp) {
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
    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
