package osh.datatypes.registry;

import java.io.Serializable;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */

public abstract class StateExchange extends AbstractExchange implements Serializable {

    private static final long serialVersionUID = -701677297851327328L;

    @Deprecated
    protected StateExchange() {
        super();
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public StateExchange(UUID sender, long timestamp) {
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
