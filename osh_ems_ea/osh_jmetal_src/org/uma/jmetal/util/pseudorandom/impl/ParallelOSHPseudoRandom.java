package org.uma.jmetal.util.pseudorandom.impl;

import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;
import osh.core.OSHRandom;
import osh.mgmt.globalcontroller.jmetal.builder.ParallelAlgorithmRunner;


/**
 * Represents a wrapper around a bundle of {@link OSHRandom} random generator for use in multirheaded JMetal related
 * algorithms, problem, operators, etc.
 *
 * @author Sebastian Kramer
 */
public class ParallelOSHPseudoRandom implements PseudoRandomGenerator {
    private static final long serialVersionUID = 8539786749748637260L;

    private OSHRandom[] randomGenerators;

    /**
     * Constructs this wrapper around the given array of {@link OSHRandom}.
     *
     * @param randomGenerators the random generator array
     */
    public ParallelOSHPseudoRandom(OSHRandom[] randomGenerators) {
        this.randomGenerators = randomGenerators;
    }

    /**
     * Sets the random generators to the given array.
     *
     * @param randomGenerators the new array of random generators
     */
    public void setRandomGenerators(OSHRandom[] randomGenerators) {
        this.randomGenerators = randomGenerators;
    }

    private OSHRandom getRandomGenerators() {
        return this.randomGenerators[ParallelAlgorithmRunner.getAlgorithmId()];
    }

    @Override
    public int nextInt(int lowerBound, int upperBound) {
        return lowerBound + this.getRandomGenerators().getNextInt(upperBound - lowerBound + 1);
    }

    @Override
    public double nextDouble(double lowerBound, double upperBound) {
        double max = upperBound - lowerBound;
        return lowerBound + this.getRandomGenerators().getNextDouble(max);
    }

    @Override
    public double nextDouble() {
        return this.getRandomGenerators().getNextDouble();
    }

    @Override
    public double nextGaussian() {
        return this.getRandomGenerators().getNextGaussian();
    }

    @Override
    public long getSeed() {
        return 0L;
    }

    @Override
    public void setSeed(long seed) {
        throw new IllegalAccessError("method only exists because of interface - setting of seed is not allowed");
    }

    @Override
    public String getName() {
        return "ParallelOSHPseudoRandom";
    }
}
