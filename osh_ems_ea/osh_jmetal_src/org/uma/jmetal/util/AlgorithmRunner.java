package org.uma.jmetal.util;

import org.uma.jmetal.algorithm.Algorithm;

/**
 * Class for running algorithms in a concurrent thread
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public final class AlgorithmRunner {
    private final long computingTime;

    /**
     * Constructor
     */
    private AlgorithmRunner(Executor execute) {
        this.computingTime = execute.computingTime;
    }

    /* Getters */
    public long getComputingTime() {
        return this.computingTime;
    }

    /**
     * Executor class
     */
    public static class Executor {
        final Algorithm<?> algorithm;
        long computingTime;

        public Executor(Algorithm<?> algorithm) {
            this.algorithm = algorithm;
        }

        public AlgorithmRunner execute() {
            long initTime = System.currentTimeMillis();
            Thread thread = new Thread(this.algorithm);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new JMetalException("Error in thread.join()", e);
            }
            this.computingTime = System.currentTimeMillis() - initTime;

            return new AlgorithmRunner(this);
        }
    }
}

