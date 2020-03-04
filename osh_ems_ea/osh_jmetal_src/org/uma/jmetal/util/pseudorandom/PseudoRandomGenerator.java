package org.uma.jmetal.util.pseudorandom;

import java.io.Serializable;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface PseudoRandomGenerator extends Serializable {
    int nextInt(int lowerBound, int upperBound);

    double nextDouble(double lowerBound, double upperBound);

    double nextDouble();

    long getSeed();

    void setSeed(long seed);

    String getName();
}
