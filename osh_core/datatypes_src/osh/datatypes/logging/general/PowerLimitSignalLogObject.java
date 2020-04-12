package osh.datatypes.logging.general;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents specific logging information about a power-limit-signal.
 *
 * @author Sebastian Kramer
 */
public class PowerLimitSignalLogObject extends LoggingObjectStateExchange implements IPromiseToBeImmutable {

    private final Map<AncillaryCommodity, PowerLimitSignal> pls;

    /**
     * Constructs this log exchange with the given sender, timestamp and the power-limit-signal.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param pls the power-limit-signal
     */
    public PowerLimitSignalLogObject(UUID sender, ZonedDateTime timestamp, Map<AncillaryCommodity, PowerLimitSignal> pls) {
        super(sender, timestamp);
        this.pls = pls;
    }

    public Map<AncillaryCommodity, PowerLimitSignal> getPls() {
        return Collections.unmodifiableMap(this.pls);
    }
}
