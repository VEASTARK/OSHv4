package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class GenericApplianceStartTimesControllerExchange extends HALControllerExchange {

    private ZonedDateTime startTime;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GenericApplianceStartTimesControllerExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            ZonedDateTime startTime) {
        super(deviceID, timestamp);

        this.startTime = startTime;
    }


    public ZonedDateTime getStartTime() {
        return this.startTime;
    }


    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

}
