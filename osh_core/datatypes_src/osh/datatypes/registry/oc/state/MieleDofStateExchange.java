package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import javax.xml.bind.annotation.XmlElement;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class MieleDofStateExchange extends StateExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = 1795073842080845700L;
    /**
     * the duration, NOT an absolute point in time!
     */
    private final long lastDof;
    private final long earliestStartTime;
    private final long latestStartTime;
    private final long expectedStartTime;


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
            long timestamp,
            long lastDof,
            long earliestStartTime,
            long latestStartTime,
            long expectedStartTime) {
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
    public long getLastDof() {
        return this.lastDof;
    }

    @XmlElement
    public long getEarliestStartTime() {
        return this.earliestStartTime;
    }

    @XmlElement
    public long getLatestStartTime() {
        return this.latestStartTime;
    }

    @XmlElement
    public long getExpectedStartTime() {
        return this.expectedStartTime;
    }


}
