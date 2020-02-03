package osh.mgmt.localcontroller;

import osh.datatypes.registry.EventExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Kaibin Bao
 */
public class ExpectedStartTimeChangedExchange extends EventExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = 806696758718863420L;
    private final ZonedDateTime expectedStartTime;


    /**
     * CONSTRUCTOR
     */
    public ExpectedStartTimeChangedExchange(UUID sender, ZonedDateTime timestamp, ZonedDateTime expectedStartTime) {
        super(sender, timestamp);
        this.expectedStartTime = expectedStartTime;
    }

    public ZonedDateTime getExpectedStartTime() {
        return this.expectedStartTime;
    }

}
