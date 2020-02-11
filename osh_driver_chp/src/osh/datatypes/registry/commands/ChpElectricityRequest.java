package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ChpElectricityRequest extends CommandExchange implements IPromiseToBeImmutable {

    private final boolean on;

    public ChpElectricityRequest(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp,
            boolean on) {
        super(sender, receiver, timestamp);

        this.on = on;
    }

    public boolean isOn() {
        return this.on;
    }
}
