package osh.eal.hal.exchange.ipp;

import osh.eal.hal.exchange.HALObserverExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class IPPSchedulingExchange
        extends HALObserverExchange {

    private Duration newIppAfter;
    private Duration rescheduleAfter;
    private double triggerIfDeltaX;

    public IPPSchedulingExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    public Duration getNewIppAfter() {
        return this.newIppAfter;
    }

    public void setNewIppAfter(Duration newIppAfter) {
        this.newIppAfter = newIppAfter;
    }

    public Duration getRescheduleAfter() {
        return this.rescheduleAfter;
    }

    public void setRescheduleAfter(Duration rescheduleAfter) {
        this.rescheduleAfter = rescheduleAfter;
    }

    public double getTriggerIfDeltaX() {
        return this.triggerIfDeltaX;
    }

    public void setTriggerIfDeltaX(double triggerIfDeltaX) {
        this.triggerIfDeltaX = triggerIfDeltaX;
    }


}
