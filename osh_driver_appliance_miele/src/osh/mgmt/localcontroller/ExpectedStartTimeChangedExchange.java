package osh.mgmt.localcontroller;

import osh.datatypes.registry.EventExchange;

import java.util.UUID;


/**
 * @author Kaibin Bao
 */
public class ExpectedStartTimeChangedExchange extends EventExchange {

    /**
     *
     */
    private static final long serialVersionUID = 806696758718863420L;
    private final long expectedStartTime;


    /**
     * CONSTRUCTOR
     */
    public ExpectedStartTimeChangedExchange(UUID sender, long timestamp, long expectedStartTime) {
        super(sender, timestamp);
        this.expectedStartTime = expectedStartTime;
    }

    public long getExpectedStartTime() {
        return this.expectedStartTime;
    }

}
