package osh.datatypes.logging;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents the generic log object intended to be handled by the configured database-logger.
 *
 * @author Sebastian Kramer
 */
public abstract class LoggingObjectStateExchange extends StateExchange {

    /**
     * Constructs this generic log object with the given sender and timestamp.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     */
    public LoggingObjectStateExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }
}
