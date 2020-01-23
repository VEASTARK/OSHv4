package osh.datatypes.logger;

import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class LogThis extends CommandExchange {

    private static final long serialVersionUID = 5173636358971480331L;

    private final IAnnotatedForLogging toLog;


    /**
     * CONSTRUCTOR
     */
    public LogThis(UUID sender, UUID receiver, ZonedDateTime timestamp, IAnnotatedForLogging toLog) {
        super(sender, receiver, timestamp);
        this.toLog = toLog;
    }


    public IAnnotatedForLogging getToLog() {
        return this.toLog;
    }

}
