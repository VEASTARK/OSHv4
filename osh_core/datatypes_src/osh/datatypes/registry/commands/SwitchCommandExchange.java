package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;

import java.util.UUID;


/**
 * @author Till Schuberth
 */
public class SwitchCommandExchange extends CommandExchange {

    /**
     *
     */
    private static final long serialVersionUID = -4073123591294900927L;
    private final boolean newState;

    public SwitchCommandExchange(UUID sender, UUID receiver, long timestamp, boolean newState) {
        super(sender, receiver, timestamp);
        this.newState = newState;
    }

    public boolean isNewState() {
        return this.newState;
    }


}
