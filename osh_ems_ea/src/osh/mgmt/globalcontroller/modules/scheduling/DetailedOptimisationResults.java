package osh.mgmt.globalcontroller.modules.scheduling;

import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a {@link EAScheduleResult} combined with the collected command and prediction exchanges.
 *
 * @author Sebastian Kramer
 */
public class DetailedOptimisationResults {

    private final Map<UUID, EASolutionCommandExchange<?>> solutionExchanges;
    private final Map<UUID, EAPredictionCommandExchange<?>> predictionExchanges;
    private final EAScheduleResult scheduleResult;

    /**
     * Constructs this optimisation result with the given ea-schedule result and the command and prediciton exchanges.
     *
     * @param solutionExchanges the solution exchanges
     * @param predictionExchanges the prediction exchnanges
     * @param scheduleResult the ea schedule result
     */
    public DetailedOptimisationResults(Map<UUID, EASolutionCommandExchange<?>> solutionExchanges,
                                       Map<UUID, EAPredictionCommandExchange<?>> predictionExchanges,
                                       EAScheduleResult scheduleResult) {
        this.solutionExchanges = solutionExchanges;
        this.predictionExchanges = predictionExchanges;
        this.scheduleResult = scheduleResult;
    }

    public Map<UUID, EASolutionCommandExchange<?>> getSolutionExchanges() {
        return this.solutionExchanges;
    }

    public Map<UUID, EAPredictionCommandExchange<?>> getPredictionExchanges() {
        return this.predictionExchanges;
    }

    public EAScheduleResult getScheduleResult() {
        return this.scheduleResult;
    }
}

