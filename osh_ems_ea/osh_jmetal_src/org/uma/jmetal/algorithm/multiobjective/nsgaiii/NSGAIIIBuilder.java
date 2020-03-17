package org.uma.jmetal.algorithm.multiobjective.nsgaiii;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.List;


/**
 * Builder class
 */
public class NSGAIIIBuilder<S extends Solution<?>> implements AlgorithmBuilder<NSGAIII<S>> {
    // no access modifier means access from classes within the same package
    private final Problem<S> problem;
    private final IEALogger eaLogger;
    private int maxIterations;
    private int populationSize;
    private CrossoverOperator<S> crossoverOperator;
    private MutationOperator<S> mutationOperator;
    private SelectionOperator<List<S>, S> selectionOperator;

    private SolutionListEvaluator<S> evaluator;

    /**
     * Builder constructor
     */
    public NSGAIIIBuilder(Problem<S> problem, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.maxIterations = 250;
        this.populationSize = 100;
        this.evaluator = new SequentialSolutionListEvaluator<>();
    }

    public NSGAIIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public SolutionListEvaluator<S> getEvaluator() {
        return this.evaluator;
    }

    public Problem<S> getProblem() {
        return this.problem;
    }

    public IEALogger getEaLogger() {
        return this.eaLogger;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public NSGAIIIBuilder<S> setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public NSGAIIIBuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public NSGAIIIBuilder<S> setCrossoverOperator(CrossoverOperator<S> crossoverOperator) {
        this.crossoverOperator = crossoverOperator;

        return this;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public NSGAIIIBuilder<S> setMutationOperator(MutationOperator<S> mutationOperator) {
        this.mutationOperator = mutationOperator;

        return this;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    public NSGAIIIBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;

        return this;
    }

    public NSGAIII<S> build() {
        return new NSGAIII<>(this);
    }
}
