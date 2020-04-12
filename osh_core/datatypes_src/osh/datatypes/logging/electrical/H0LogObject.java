package osh.datatypes.logging.electrical;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about the aggregate power usage per weekday/day.
 *
 * @author Sebastian Kramer
 */
public class H0LogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final double[][][] aggregateWeekdayPower;
    private final double[][] aggregateDayPower;

    /**
     * Constructs this log exchange with the given sender, timestamp and the aggregate power usage per weekday/day.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param aggregateWeekdayPower the aggregate power per weekday
     * @param aggregateDayPower the aggregate power per day
     */
    public H0LogObject(UUID sender, ZonedDateTime timestamp, double[][][] aggregateWeekdayPower, double[][] aggregateDayPower) {
        super(sender, timestamp);
        this.aggregateWeekdayPower = aggregateWeekdayPower;
        this.aggregateDayPower = aggregateDayPower;
    }

    public double[][][] getAggregateWeekdayPower() {
        return this.aggregateWeekdayPower;
    }

    public double[][] getAggregateDayPower() {
        return this.aggregateDayPower;
    }
}
