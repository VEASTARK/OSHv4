package osh.datatypes.logging.general;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents specific logging information about a price-signal.
 *
 * @author Sebastian Kramer
 */
public class PriceSignalLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final Map<AncillaryCommodity, PriceSignal> eps;

    /**
     * Constructs this log exchange with the given sender, timestamp and the price-signal.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param eps the price-signal
     */
    public PriceSignalLogObject(UUID sender, ZonedDateTime timestamp, Map<AncillaryCommodity, PriceSignal> eps) {
        super(sender, timestamp);
        this.eps = eps;
    }

    public Map<AncillaryCommodity, PriceSignal> getEps() {
        return Collections.unmodifiableMap(this.eps);
    }
}
