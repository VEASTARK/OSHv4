package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import javax.xml.bind.annotation.XmlElement;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class MieleDofStateExchange extends StateExchange implements IPromiseToBeImmutable {

    /**
     * the duration, NOT an absolute point in time!
     */
    private final Duration lastDof;
    private final ZonedDateTime earliestStartTime;
    private final ZonedDateTime latestStartTime;
    private final ZonedDateTime expectedStartTime;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param lastDof
     * @param earliestStartTime
     * @param latestStartTime
     * @param expectedStartTime
     */
    public MieleDofStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            Duration lastDof,
            ZonedDateTime earliestStartTime,
            ZonedDateTime latestStartTime,
            ZonedDateTime expectedStartTime) {
        super(sender, timestamp);

        this.lastDof = lastDof;
        this.earliestStartTime = earliestStartTime;
        this.latestStartTime = latestStartTime;
        this.expectedStartTime = expectedStartTime;
    }

    /**
     * returns the last set degree of freedom in seconds as duration
     */
    @XmlElement
    public Duration getLastDof() {
        return this.lastDof;
    }

    @XmlElement
    public ZonedDateTime getEarliestStartTime() {
        return this.earliestStartTime;
    }

    @XmlElement
    public ZonedDateTime getLatestStartTime() {
        return this.latestStartTime;
    }

    @XmlElement
    public ZonedDateTime getExpectedStartTime() {
        return this.expectedStartTime;
    }


}
