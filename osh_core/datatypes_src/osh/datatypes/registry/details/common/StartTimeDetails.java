package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Kaibin Bao
 */
public class StartTimeDetails extends StateExchange {

    private long startTime;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public StartTimeDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);

    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}