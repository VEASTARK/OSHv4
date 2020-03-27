package osh.mgmt.globalcontroller.jmetal.logging;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import osh.configuration.oc.EAObjectives;
import osh.core.logging.IGlobalLogger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a simple logger implementing the EA logger interface {@link IEALogger} designed for multithreaded usage.
 *
 * @author Sebastian Kramer
 */
public class ParallelEALogger extends EALogger {

    private final Map<Integer, double[]> bestFirstFitness = new HashMap<>();
    private final Map<Integer, List<String>> logMessages = new HashMap<>();

    private final Lock extendedLock = new ReentrantLock();

    /**
     * Constructs this ea-logger with the given global logger and the configuration parameters.
     *
     * @param globalLogger the global logger
     * @param log flag if anything should be logged
     * @param logExtended flag if extended population values should be logged
     * @param logXthGeneration counter after how many generations there should be a log of population values
     * @param logExtendedGenerations counter after how many generations there should be an exteneded log of population
     *                               values
     * @param logOverallEA flag if the number of optimization runs should be logges
     * @param eaObjectives collection of the objectives of the optimization
     */
    public ParallelEALogger(IGlobalLogger globalLogger, boolean log, boolean logExtended, int logXthGeneration,
                            int logExtendedGenerations, boolean logOverallEA, Collection<EAObjectives> eaObjectives) {
        super(globalLogger, log, logExtended, logXthGeneration, logExtendedGenerations, logOverallEA, eaObjectives);
    }

    /**
     * Returns a unique identifier of the current thread.
     *
     * @return a unique identifier of the current thread
     */
    private int getId() {
        return 0;
    }

    @Override
    double[] getBestFirstFitness() {
        return this.bestFirstFitness.get(this.getId());
    }

    @Override
    public void logStart(Algorithm<?> usedAlgorithm) {
        //nothing, in parallel usage the logStartParallel method should be called
    }

    /**
     * Similar to {@link IEALogger#logStart(Algorithm)} this logs the start of an algorithm but is designed to be
     * called from a multithreaded context.
     *
     * @param usedAlgorithm the started algorithm
     */
    public void logStartParallel(Algorithm<?> usedAlgorithm) {
        int id = this.getId();
        if (this.log) {
            this.logMessages.put(id, new ArrayList<>());
            this.bestFirstFitness.put(id, new double[this.objectiveCount]);

            this.log("===    New Optimization, using " + usedAlgorithm.getDescription() + "    ===");
            Arrays.fill(this.getBestFirstFitness(), Double.NaN);
        }
    }

    /**
     * Flushes the saved log messages.
     */
    public void flush() {
        for (List<String> strings : this.logMessages.values()) {
            strings.forEach(super::log);
        }
        this.logMessages.clear();
        this.bestFirstFitness.clear();
    }

    @Override
    void log(String message, boolean prependTime) {
        this.logMessages.get(this.getId()).add(message);
    }

    @Override
    String logExtendedPopulation(List<? extends Solution<?>> population, int generation) {
        this.extendedLock.lock();
        try {
            return super.logExtendedPopulation(population, generation);
        } finally {
            this.extendedLock.unlock();
        }
    }
}
