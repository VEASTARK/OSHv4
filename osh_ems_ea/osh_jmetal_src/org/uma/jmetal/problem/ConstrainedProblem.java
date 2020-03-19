package org.uma.jmetal.problem;

/**
 * Interface representing problems having constraints
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@Deprecated
public interface ConstrainedProblem<S> extends Problem<S> {

    /* Getters */
    int getNumberOfConstraints();

    /* Methods */
    void evaluateConstraints(S solution);
}
