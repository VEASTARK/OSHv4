package osh.datatypes.logging.devices;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about the baseload consumption.
 *
 * @author Sebastian Kramer
 */
public class BaseloadLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final double activePower;
    private final double reactivePower;

    /**
     * Constructs this log exchange with the given sender, timestamp and the consumed active and reactive load.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param activePower the active power consumed by the baseload
     * @param reactivePower the reactive power consumed by the baseload
     */
    public BaseloadLogObject(UUID sender, ZonedDateTime timestamp, double activePower, double reactivePower) {
        super(sender, timestamp);
        this.activePower = activePower;
        this.reactivePower = reactivePower;
    }

    /**
     * Returns the active power load consumed by the baseload.
     *
     * @return the active power
     */
    public double getActivePower() {
        return this.activePower;
    }

    /**
     * Returns the reactive power load consumed by the baseload.
     *
     * @return the reactive power
     */
    public double getReactivePower() {
        return this.reactivePower;
    }
}
