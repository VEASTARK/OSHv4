package org.uma.jmetal.algorithm.singleobjective.geneticalgorithm;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * Created by ajnebro on 10/12/14.
 */
public class GeneticAlgorithmBuilder<S extends Solution<?>> {
    /**
     * Builder class
     */
    private final Problem<S> problem;
    private final CrossoverOperator<S> crossoverOperator;
    private final MutationOperator<S> mutationOperator;
    private final SelectionOperator<List<S>, S> defaultSelectionOperator = new BinaryTournamentSelection<>();
    private int maxEvaluations;
    private int populationSize;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;

    private GeneticAlgorithmVariant variant;
    /**
     * Builder constructor
     */
    public GeneticAlgorithmBuilder(Problem<S> problem,
                                   CrossoverOperator<S> crossoverOperator,
                                   MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.mutationOperator = mutationOperator;
        this.crossoverOperator = crossoverOperator;
        this.selectionOperator = this.defaultSelectionOperator;

        this.evaluator = new SequentialSolutionListEvaluator<>();

        this.variant = GeneticAlgorithmVariant.GENERATIONAL;
    }

    public GeneticAlgorithmBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public Algorithm<S> build() {
        AbstractGeneticAlgorithm<S, S> algorithm;
        if (this.variant == GeneticAlgorithmVariant.GENERATIONAL) {
            algorithm = new GenerationalGeneticAlgorithm<>(this.problem, this.populationSize,
                    this.crossoverOperator, this.mutationOperator, this.selectionOperator, this.evaluator);
        } else if (this.variant == GeneticAlgorithmVariant.STEADY_STATE) {
            algorithm = new SteadyStateGeneticAlgorithm<>(this.problem, this.populationSize,
                    this.crossoverOperator, this.mutationOperator, this.selectionOperator);
        } else {
            throw new JMetalException("Unknown variant: " + this.variant);
        }
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }

    /*
     * Getters
     */
    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public GeneticAlgorithmBuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public GeneticAlgorithmBuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    public GeneticAlgorithmBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        this.selectionOperator = selectionOperator;

        return this;
    }

    public SolutionListEvaluator<S> getEvaluator() {
        return this.evaluator;
    }

    public GeneticAlgorithmVariant getVariant() {
        return this.variant;
    }

    public GeneticAlgorithmBuilder<S> setVariant(GeneticAlgorithmVariant variant) {
        this.variant = variant;

        return this;
    }

    public enum GeneticAlgorithmVariant {GENERATIONAL, STEADY_STATE}
}
