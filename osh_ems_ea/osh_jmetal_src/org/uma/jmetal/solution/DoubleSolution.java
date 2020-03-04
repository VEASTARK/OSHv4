package org.uma.jmetal.solution;

/**
 * Interface representing a double solutions
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface DoubleSolution extends Solution<Double> {
    Double getLowerBound(int index);

    Double getUpperBound(int index);
}
