package osh.datatypes.logging.general;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.datatypes.registry.StateExchange;
import osh.simulation.OSHSimulationResults;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about the result of the simulation.
 *
 * @author Sebastian Kramer
 */
public class SimulationResultsLogObject extends LoggingObjectStateExchange {

    private final OSHSimulationResults simResults;
    private final long relativeStart;
    private final long relativeEnd;
    private final Long simRuntime;

    /**
     * Constructs this log exchange with the given sender, timestamp and the results of the simulation.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param simResults the results of the simulation
     * @param relativeStart the start of the simulation
     * @param relativeEnd the end of the simulation
     * @param simRuntime the real runtime of the simulation
     */
    public SimulationResultsLogObject(UUID sender, ZonedDateTime timestamp, OSHSimulationResults simResults,
                                      long relativeStart, long relativeEnd, Long simRuntime) {
        super(sender, timestamp);
        this.simResults = simResults;
        this.relativeStart = relativeStart;
        this.relativeEnd = relativeEnd;
        this.simRuntime = simRuntime;
    }

    public OSHSimulationResults getSimResults() {
        return this.simResults;
    }

    public long getRelativeStart() {
        return this.relativeStart;
    }

    public long getRelativeEnd() {
        return this.relativeEnd;
    }

    public Long getSimRuntime() {
        return this.simRuntime;
    }

    @Override
    public StateExchange clone() {
        return new SimulationResultsLogObject(this.getSender(), this.getTimestamp(), this.simResults.clone(), this.relativeStart,
                this.relativeEnd, this.simRuntime);
    }
}
