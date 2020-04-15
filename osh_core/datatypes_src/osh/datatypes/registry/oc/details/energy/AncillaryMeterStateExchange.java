package osh.datatypes.registry.oc.details.energy;

import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.AncillaryMeterState.ImmutableAncillaryMeterState;
import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an exchange object for the ancillary meter state.
 *
 * @author Sebastian Kramer
 */
public class AncillaryMeterStateExchange extends StateExchange implements IPromiseToBeImmutable {

    private final ImmutableAncillaryMeterState meterState;

    /**
     * Construct this echange with the given sender, timestamp and the ancillary meter state.
     *
     * @param sender the sender
     * @param timestamp the timestamp
     * @param meterState the meter state
     */
    public AncillaryMeterStateExchange(final UUID sender, final ZonedDateTime timestamp,
                                       final AncillaryMeterState meterState) {
        this(sender, timestamp, new ImmutableAncillaryMeterState(meterState));
    }

    /**
     * Construct this echange with the given sender, timestamp and the immutable ancillary meter state.
     *
     * @param sender the sender
     * @param timestamp the timestamp
     * @param immutableMeterState the immutable meter state
     */
    public AncillaryMeterStateExchange(final UUID sender, final ZonedDateTime timestamp,
                                       final ImmutableAncillaryMeterState immutableMeterState) {
        super(sender, timestamp);
        this.meterState = immutableMeterState;
    }

    public ImmutableAncillaryMeterState getMeterState() {
        return this.meterState;
    }
}
