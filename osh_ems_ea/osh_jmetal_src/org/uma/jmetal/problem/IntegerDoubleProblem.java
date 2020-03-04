package org.uma.jmetal.problem;

/**
 * Interface representing problems having integer and double variables
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface IntegerDoubleProblem<S> extends BoundedProblem<Number, S> {
    int getNumberOfIntegerVariables();

    int getNumberOfDoubleVariables();
}
