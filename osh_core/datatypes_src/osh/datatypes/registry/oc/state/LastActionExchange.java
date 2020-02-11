package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


public class LastActionExchange extends StateExchange {

    private IAction lastAction;

    public LastActionExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    public LastActionExchange(UUID sender, ZonedDateTime timestamp, IAction lastAction) {
        super(sender, timestamp);
        this.lastAction = lastAction;
    }

    public IAction getLastAction() {
        return this.lastAction;
    }

    public void setLastAction(IAction lastAction) {
        this.lastAction = lastAction;
    }

}
