package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Till Schuberth
 */
public class SwitchCommandExchange extends CommandExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -4073123591294900927L;
    private final boolean newState;

    public SwitchCommandExchange(UUID sender, UUID receiver, ZonedDateTime timestamp, boolean newState) {
        super(sender, receiver, timestamp);
        this.newState = newState;
    }

    public boolean isNewState() {
        return this.newState;
    }


}
