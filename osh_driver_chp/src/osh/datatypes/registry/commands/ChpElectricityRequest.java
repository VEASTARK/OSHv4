package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ChpElectricityRequest extends CommandExchange {

    /**
     *
     */
    private static final long serialVersionUID = 2650396121767761563L;
    private final boolean on;


    public ChpElectricityRequest(
            UUID sender,
            UUID receiver,
            long timestamp,
            boolean on) {
        super(sender, receiver, timestamp);

        this.on = on;
    }


    public boolean isOn() {
        return this.on;
    }


}
