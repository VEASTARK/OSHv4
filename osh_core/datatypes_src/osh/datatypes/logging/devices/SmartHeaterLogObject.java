package osh.datatypes.logging.devices;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about a smart-heater.
 *
 * @author Sebastian Kramer
 */
public class SmartHeaterLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final int[] switchOns;
    private final long[] powerTierRunTimes;
    private final long[] runTimes;

    /**
     * Constructs this log exchange with the given sender, timestamp and the activity of the smart-heater.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param switchOns the number of switch-ons of each element
     * @param powerTierRunTimes the run-times of each possible power-configuration
     * @param runTimes the run-times of each element
     */
    public SmartHeaterLogObject(UUID sender, ZonedDateTime timestamp, int[] switchOns, long[] powerTierRunTimes,
                                long[] runTimes) {
        super(sender, timestamp);
        this.switchOns = switchOns;
        this.powerTierRunTimes = powerTierRunTimes;
        this.runTimes = runTimes;
    }

    public int[] getSwitchOns() {
        return this.switchOns;
    }

    public long[] getPowerTierRunTimes() {
        return this.powerTierRunTimes;
    }

    public long[] getRunTimes() {
        return this.runTimes;
    }
}
