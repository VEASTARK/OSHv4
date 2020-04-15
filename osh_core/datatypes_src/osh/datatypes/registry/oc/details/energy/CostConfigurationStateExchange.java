package osh.datatypes.registry.oc.details.energy;

import osh.configuration.oc.CostConfiguration;
import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an exchnage about the cost configuration of the simulation.
 *
 * @author Sebastian Kramer
 */
public class CostConfigurationStateExchange extends StateExchange implements IPromiseToBeImmutable {

    private final CostConfiguration costConfiguration;

    /**
     * Constructs this exchange with the given sender, timestamp and the cost configuration.
     *
     * @param sender the sender
     * @param timestamp the timestamp
     * @param costConfiguration the cost configuration
     */
    public CostConfigurationStateExchange(UUID sender, ZonedDateTime timestamp,
                                          CostConfiguration costConfiguration) {
        super(sender, timestamp);
        this.costConfiguration = costConfiguration;
    }

    public CostConfiguration getCostConfigurationContainer() {
        return this.costConfiguration;
    }
}
