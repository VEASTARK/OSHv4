package osh.datatypes.logging.thermal;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about a device supplying thermal power.
 *
 * @author Sebastian Kramer
 */
public class ThermalSupplyLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final Commodity commodity;
    private final double supply;
    private final int starts;

    /**
     * Constructs this log exchange with the given sender, timestamp and the thermal supply information.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param commodity the thermal commodity that is supplied
     * @param supply the thermal power supplied
     * @param starts the number of starts of the device
     */
    public ThermalSupplyLogObject(UUID sender, ZonedDateTime timestamp, Commodity commodity, double supply, int starts) {
        super(sender, timestamp);
        this.commodity = commodity;
        this.supply = supply;
        this.starts = starts;
    }

    public Commodity getCommodity() {
        return this.commodity;
    }

    public double getSupply() {
        return this.supply;
    }

    public int getStarts() {
        return this.starts;
    }
}
