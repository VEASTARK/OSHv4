package osh.eal.hal.exchange.ipp;

import osh.eal.hal.exchange.HALObserverExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class IPPSchedulingExchange
        extends HALObserverExchange {

    private long newIppAfter;
    private long rescheduleAfter;
    private double triggerIfDeltaX;

    public IPPSchedulingExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    public long getNewIppAfter() {
        return this.newIppAfter;
    }

    public void setNewIppAfter(long newIppAfter) {
        this.newIppAfter = newIppAfter;
    }

    public long getRescheduleAfter() {
        return this.rescheduleAfter;
    }

    public void setRescheduleAfter(long rescheduleAfter) {
        this.rescheduleAfter = rescheduleAfter;
    }

    public double getTriggerIfDeltaX() {
        return this.triggerIfDeltaX;
    }

    public void setTriggerIfDeltaX(double triggerIfDeltaX) {
        this.triggerIfDeltaX = triggerIfDeltaX;
    }


}
