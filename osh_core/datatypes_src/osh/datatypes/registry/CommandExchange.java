package osh.datatypes.registry;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Till Schuberth
 */
public abstract class CommandExchange extends EventExchange {

    protected final UUID receiver;

    public CommandExchange(UUID sender, UUID receiver, ZonedDateTime timestamp) {
        super(sender, timestamp);
        this.receiver = receiver;
    }

    public UUID getReceiver() {
        return this.receiver;
    }

}
