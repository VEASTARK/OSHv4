package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class GenericApplianceStartTimesControllerExchange extends HALControllerExchange {

    private long startTime;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GenericApplianceStartTimesControllerExchange(
            UUID deviceID,
            long timestamp,
            long startTime) {
        super(deviceID, timestamp);

        this.startTime = startTime;
    }


    public long getStartTime() {
        return this.startTime;
    }


    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}
