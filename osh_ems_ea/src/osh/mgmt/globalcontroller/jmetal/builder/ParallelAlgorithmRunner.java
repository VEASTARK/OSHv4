package osh.mgmt.globalcontroller.jmetal.builder;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import osh.configuration.oc.AlgorithmType;
import osh.core.logging.IGlobalLogger;
import osh.mgmt.globalcontroller.jmetal.logging.ParallelEALogger;
import osh.mgmt.globalcontroller.jmetal.solution.AlgorithmSolutionCollection;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * Represents a runner-thread for a parallel execution of a jMetal {@link Algorithm}.
 *
 * @author Sebastian Kramer
 */
public class ParallelAlgorithmRunner extends Thread {

    private final Algorithm<?> algorithm;
    private final AlgorithmSolutionCollection resultCollection;
    private final ParallelEALogger eaLogger;
    private final IGlobalLogger logger;
    private final AlgorithmType type;
    private final CountDownLatch latch;
    //TODO: find a better solution for use of random gens in setup in parallel execution
    private static final ThreadLocal<Integer> threadId = ThreadLocal.withInitial(() -> 0);
    private final int id;

    /**
     * Constructs this runner with the algorithm to use, loggers, the data object to commit results to and management
     * paramters for multithreaded execution.
     *
     * @param algorithm the algorithm to run
     * @param eaLogger the multithreaded ea-logger
     * @param resultCollection the collection to commit results to
     * @param logger the global logger
     * @param type the algorithm type
     * @param latch the countdown latch for completion
     * @param id the id to use for this thread
     */
    public ParallelAlgorithmRunner(Algorithm<?> algorithm, ParallelEALogger eaLogger, AlgorithmSolutionCollection resultCollection,
                                   IGlobalLogger logger, AlgorithmType type, CountDownLatch latch, int id) {
        this.algorithm = algorithm;
        this.eaLogger = eaLogger;
        this.resultCollection = resultCollection;
        this.logger = logger;
        this.type = type;
        this.latch = latch;
        this.id = id;

        this.setDaemon(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        ParallelAlgorithmRunner.threadId.set(this.id);

        synchronized (this.logger) {
            this.logger.logDebug("[---- Parallel execution of " + this.algorithm.getName() + " started ----]");
        }

        this.eaLogger.logStartParallel(this.algorithm);
        Object result;

        try {
            this.algorithm.run();
            result = this.algorithm.getResult();
        } catch (Throwable t){
            this.latch.countDown();
            throw new RuntimeException(t);
        }

        if (result instanceof List) {
            this.resultCollection.addSolutionCollection(this.type, (List<Solution<?>>) result);
        } else {
            this.resultCollection.addSolution(this.type, (Solution<?>) result);
        }

        synchronized (this.logger) {
            this.logger.logDebug("[---- Parallel execution of " + this.algorithm.getName() + " ended ----]");
        }

        ParallelAlgorithmRunner.threadId.remove();
        this.latch.countDown();
    }

    /**
     * Returns the id this algorithm runs under.
     *
     * @return the id this algorithm runs under
     */
    public static int getAlgorithmId() {
        return ParallelAlgorithmRunner.threadId.get();
    }
}
