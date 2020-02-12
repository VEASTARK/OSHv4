package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ApplianceRequest extends CommandExchange implements IPromiseToBeImmutable {

    private final EN50523OIDExecutionOfACommandCommands command;

    public ApplianceRequest(UUID sender, UUID receiver, ZonedDateTime timestamp,
                            EN50523OIDExecutionOfACommandCommands command) {
        super(sender, receiver, timestamp);

        this.command = command;
    }

    public EN50523OIDExecutionOfACommandCommands getCommand() {
        return this.command;
    }
}
