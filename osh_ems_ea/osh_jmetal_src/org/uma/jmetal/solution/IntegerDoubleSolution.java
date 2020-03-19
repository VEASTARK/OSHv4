package org.uma.jmetal.solution;

/**
 * Interface representing a solution composed of integers and real values
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface IntegerDoubleSolution extends Solution<Number> {
    Number getLowerBound(int index);

    Number getUpperBound(int index);

    int getNumberOfIntegerVariables();

    int getNumberOfDoubleVariables();
}
