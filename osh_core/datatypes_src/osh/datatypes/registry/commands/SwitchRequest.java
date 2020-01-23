package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


public class SwitchRequest extends CommandExchange {


    /**
     *
     */
    private static final long serialVersionUID = -2018595178055473489L;
    protected Boolean turnOn;

    public SwitchRequest(UUID sender, UUID receiver, ZonedDateTime timestamp) {
        super(sender, receiver, timestamp);
    }

    public Boolean getTurnOn() {
        return this.turnOn;
    }

    public boolean isTurnOn() {
        return this.turnOn;
    }

    public void setTurnOn(Boolean turnOn) {
        this.turnOn = turnOn;
    }
}
