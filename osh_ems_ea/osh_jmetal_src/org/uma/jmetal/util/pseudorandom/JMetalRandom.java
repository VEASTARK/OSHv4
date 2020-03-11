package org.uma.jmetal.util.pseudorandom;

import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;

import java.io.Serializable;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public final class JMetalRandom implements Serializable {
    private static JMetalRandom instance;
    private PseudoRandomGenerator randomGenerator;

    private JMetalRandom() {
        this.randomGenerator = new JavaRandomGenerator();
    }

    public static JMetalRandom getInstance() {
        if (instance == null) {
            instance = new JMetalRandom();
        }
        return instance;
    }

    public PseudoRandomGenerator getRandomGenerator() {
        return this.randomGenerator;
    }

    public void setRandomGenerator(PseudoRandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public int nextInt(int lowerBound, int upperBound) {
        return this.randomGenerator.nextInt(lowerBound, upperBound);
    }

    public double nextDouble() {
        return this.randomGenerator.nextDouble();
    }

    public double nextDouble(double lowerBound, double upperBound) {
        return this.randomGenerator.nextDouble(lowerBound, upperBound);
    }

    public double nextGaussian() {
        return this.randomGenerator.nextGaussian();
    }

    public long getSeed() {
        return this.randomGenerator.getSeed();
    }

    public void setSeed(long seed) {
        this.randomGenerator.setSeed(seed);
    }

    public String getGeneratorName() {
        return this.randomGenerator.getName();
    }
}
