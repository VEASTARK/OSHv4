package osh.datatypes.logging.devices;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about a watertank.
 *
 * @author Sebastian Kramer
 */
public class WaterTankLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final Commodity commodity;
    private final double averageTemperature;
    private final double demand;
    private final double supply;
    private final double lastTemp;

    /**
     * Constructs this log exchange with the given sender, timestamp and the activity of the smart-heater.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param commodity the commodity of the tank
     * @param averageTemperature the average temperature of the tank
     * @param demand the aggregated demand the watertank experienced
     * @param supply the aggregated supply the watertank experienced
     * @param lastTemp the last temperature in the tank
     */
    public WaterTankLogObject(UUID sender, ZonedDateTime timestamp, Commodity commodity, double averageTemperature, double demand,
                              double supply, double lastTemp) {
        super(sender, timestamp);
        this.commodity = commodity;
        this.averageTemperature = averageTemperature;
        this.demand = demand;
        this.supply = supply;
        this.lastTemp = lastTemp;
    }

    public Commodity getCommodity() {
        return this.commodity;
    }

    public double getAverageTemperature() {
        return this.averageTemperature;
    }

    public double getDemand() {
        return this.demand;
    }

    public double getSupply() {
        return this.supply;
    }

    public double getLastTemp() {
        return this.lastTemp;
    }
}
