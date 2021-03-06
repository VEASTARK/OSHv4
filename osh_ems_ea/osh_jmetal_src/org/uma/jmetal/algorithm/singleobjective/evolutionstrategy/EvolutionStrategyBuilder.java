package org.uma.jmetal.algorithm.singleobjective.evolutionstrategy;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractEvolutionStrategy;
import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

/**
 * Class implementing a (mu , lambda) Evolution Strategy (lambda must be divisible by mu)
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class EvolutionStrategyBuilder<S extends Solution<?>> implements AlgorithmBuilder<Algorithm<S>> {
    private final Problem<S> problem;
    private final IEALogger eaLogger;
    private final MutationOperator<S> mutation;
    private final EvolutionStrategyVariant variant;
    private int mu;
    private int lambda;
    private int maxEvaluations;
    public EvolutionStrategyBuilder(Problem<S> problem, MutationOperator<S> mutationOperator,
                                    EvolutionStrategyVariant variant, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.mu = 1;
        this.lambda = 10;
        this.maxEvaluations = 250000;
        this.mutation = mutationOperator;
        this.variant = variant;
    }

    @Override
    public Algorithm<S> build() {
        AbstractEvolutionStrategy<S, S> algorithm;
        if (this.variant == EvolutionStrategyVariant.ELITIST) {
            algorithm = new ElitistEvolutionStrategy<>(this.problem, this.mu, this.lambda, this.mutation, this.eaLogger);
        } else if (this.variant == EvolutionStrategyVariant.NON_ELITIST) {
            algorithm = new NonElitistEvolutionStrategy<>(this.problem, this.mu, this.lambda, this.mutation, this.eaLogger);
        } else {
            throw new JMetalException("Unknown variant: " + this.variant);
        }
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.lambda, this.maxEvaluations));
        return algorithm;
    }

    /* Getters */
    public int getMu() {
        return this.mu;
    }

    public EvolutionStrategyBuilder<S> setMu(int mu) {
        this.mu = mu;

        return this;
    }

    public int getLambda() {
        return this.lambda;
    }

    public EvolutionStrategyBuilder<S> setLambda(int lambda) {
        this.lambda = lambda;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public EvolutionStrategyBuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public MutationOperator<S> getMutation() {
        return this.mutation;
    }

    public enum EvolutionStrategyVariant {ELITIST, NON_ELITIST}
}
