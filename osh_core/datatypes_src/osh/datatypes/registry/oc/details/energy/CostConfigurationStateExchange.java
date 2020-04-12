package osh.datatypes.registry.oc.details.energy;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;
import osh.utils.CostConfigurationContainer;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an exchnage about the cost configuration of the simulation.
 *
 * @author Sebastian Kramer
 */
public class CostConfigurationStateExchange extends StateExchange implements IPromiseToBeImmutable {

    private final CostConfigurationContainer costConfigurationContainer;

    /**
     * Constructs this exchange with the given sender, timestamp and the cost configuration.
     *
     * @param sender the sender
     * @param timestamp the timestamp
     * @param costConfigurationContainer the cost configuration
     */
    public CostConfigurationStateExchange(UUID sender, ZonedDateTime timestamp,
                                          CostConfigurationContainer costConfigurationContainer) {
        super(sender, timestamp);
        this.costConfigurationContainer = costConfigurationContainer;
    }

    public CostConfigurationContainer getCostConfigurationContainer() {
        return this.costConfigurationContainer;
    }
}
