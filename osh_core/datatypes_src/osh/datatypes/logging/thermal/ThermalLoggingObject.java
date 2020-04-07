package osh.datatypes.logging.thermal;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about a thermal power device.
 *
 * @author Sebastian Kramer
 */
public class ThermalLoggingObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final double[][] aggregateWeekdayPower;
    private final double[] aggregateDayPower;
    private final Commodity commodity;

    /**
     * Constructs this log exchange with the given sender, timestamp and the supply demand of thermal
     * power.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param aggregateWeekdayPower the aggregate thermal power per weekday
     * @param aggregateDayPower the aggregate thermal power per day
     * @param commodity the thermal commodity
     */
    public ThermalLoggingObject(UUID sender, ZonedDateTime timestamp,
                                double[][] aggregateWeekdayPower, double[] aggregateDayPower, Commodity commodity) {
        super(sender, timestamp);
        this.aggregateWeekdayPower = aggregateWeekdayPower;
        this.aggregateDayPower = aggregateDayPower;
        this.commodity = commodity;
    }

    public double[][] getAggregateWeekdayPower() {
        return this.aggregateWeekdayPower;
    }

    public double[] getAggregateDayPower() {
        return this.aggregateDayPower;
    }

    public Commodity getCommodity() {
        return this.commodity;
    }
}
