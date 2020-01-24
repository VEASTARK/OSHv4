package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ApplianceRequest extends CommandExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -67829648900818704L;
    private final EN50523OIDExecutionOfACommandCommands command;

    public ApplianceRequest(UUID sender, UUID receiver, long timestamp, EN50523OIDExecutionOfACommandCommands command) {
        super(sender, receiver, timestamp);

        this.command = command;
    }

    public EN50523OIDExecutionOfACommandCommands getCommand() {
        return this.command;
    }
}
