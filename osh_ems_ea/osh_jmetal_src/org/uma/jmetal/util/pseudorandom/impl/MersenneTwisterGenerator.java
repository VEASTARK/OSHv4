package org.uma.jmetal.util.pseudorandom.impl;

import org.apache.commons.math3.random.MersenneTwister;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class MersenneTwisterGenerator implements PseudoRandomGenerator {
    private static final String NAME = "MersenneTwister";
    private final MersenneTwister rnd;
    private long seed;

    /**
     * Constructor
     */
    public MersenneTwisterGenerator() {
        this(System.currentTimeMillis());
    }

    /**
     * Constructor
     */
    public MersenneTwisterGenerator(long seed) {
        this.seed = seed;
        this.rnd = new MersenneTwister(seed);
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
