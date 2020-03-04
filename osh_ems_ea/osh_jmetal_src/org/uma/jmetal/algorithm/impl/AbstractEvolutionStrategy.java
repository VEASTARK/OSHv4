package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;

/**
 * Abstract class representing an evolution strategy algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */

@SuppressWarnings("serial")
public abstract class AbstractEvolutionStrategy<S, Result> extends AbstractEvolutionaryAlgorithm<S, Result> {
    protected MutationOperator<S> mutationOperator;

    /**
     * Constructor
     *
     * @param problem The problem to solve
     */
    public AbstractEvolutionStrategy(Problem<S> problem) {
        this.setProblem(problem);
    }

    /* Getter */
    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }
}
