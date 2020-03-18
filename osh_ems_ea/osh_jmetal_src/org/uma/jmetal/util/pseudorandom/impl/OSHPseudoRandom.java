package org.uma.jmetal.util.pseudorandom.impl;

import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;
import osh.core.OSHRandom;


/**
 * Represents a wrapper around a {@link OSHRandom} random generator for use in JMetal related algorithms, problem,
 * operators, etc.
 *
 * @author Sebastian Kramer
 */
public class OSHPseudoRandom implements PseudoRandomGenerator {
    private static final long serialVersionUID = 7376696020749460756L;

    private final OSHRandom randomGenerator;

    /**
     * Constructs this wrapper around the given {@link OSHRandom}.
     *
     * @param randomGenerator the random generator of the OSH
     */
    public OSHPseudoRandom(OSHRandom randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public int nextInt(int lowerBound, int upperBound) {
        return lowerBound + this.randomGenerator.getNextInt(upperBound - lowerBound + 1);
    }

    @Override
    public double nextDouble(double lowerBound, double upperBound) {
        double max = upperBound - lowerBound;
        return lowerBound + this.randomGenerator.getNextDouble(max);
    }

    @Override
    public double nextDouble() {
        return this.randomGenerator.getNextDouble();
    }

    @Override
    public double nextGaussian() {
        return this.randomGenerator.getNextGaussian();
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
        return "OSHPseudoRandom";
    }
}
