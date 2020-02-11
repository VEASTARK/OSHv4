package osh.datatypes.registry;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */

public abstract class StateExchange extends AbstractExchange {

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public StateExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    @Override
    public StateExchange clone() {
        return (StateExchange) super.clone();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": Sender " + this.getSender() + ", time: " + this.getTimestamp();
    }
}
