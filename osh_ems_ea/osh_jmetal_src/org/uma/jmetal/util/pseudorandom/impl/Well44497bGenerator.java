package org.uma.jmetal.util.pseudorandom.impl;

import org.apache.commons.math3.random.Well44497b;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class Well44497bGenerator implements PseudoRandomGenerator {
    private static final String NAME = "Well44497b";
    private final Well44497b rnd;
    private long seed;

    /**
     * Constructor
     */
    public Well44497bGenerator() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructor
     */
    public Well44497bGenerator(long seed) {
        this.seed = seed;
        this.rnd = new Well44497b(seed);
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
        this.rnd.setSeed(seed);
    }

    @Override
    public int nextInt(int lowerBound, int upperBound) {
        return lowerBound + this.rnd.nextInt((upperBound - lowerBound) + 1);
    }

    @Override
    public double nextDouble(double lowerBound, double upperBound) {
        return lowerBound + this.rnd.nextDouble() * (upperBound - lowerBound);
    }

    @Override
    public double nextDouble() {
        return this.nextDouble(0.0, 1.0);
    }

    @Override
    public String getName() {
        return NAME;
    }
}