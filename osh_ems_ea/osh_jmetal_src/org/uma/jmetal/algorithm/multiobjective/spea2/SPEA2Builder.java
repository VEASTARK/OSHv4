package org.uma.jmetal.algorithm.multiobjective.spea2;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * @author Juan J. Durillo
 */
public class SPEA2Builder<S extends Solution<?>> implements AlgorithmBuilder<SPEA2<S>> {
    /**
     * SPEA2Builder class
     */
    protected final Problem<S> problem;
    protected final CrossoverOperator<S> crossoverOperator;
    protected final MutationOperator<S> mutationOperator;
    protected int maxIterations;
    protected int populationSize;
    protected SelectionOperator<List<S>, S> selectionOperator;
    protected SolutionListEvaluator<S> evaluator;
    protected int k;

    /**
     * SPEA2Builder constructor
     */
    public SPEA2Builder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                        MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.maxIterations = 250;
        this.populationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new BinaryTournamentSelection<>();
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.k = 1;
    }

    public SPEA2Builder<S> setK(int k) {
        this.k = k;

        return this;
    }

    public SPEA2<S> build() {
        SPEA2<S> algorithm;
        algorithm = new SPEA2<>(this.problem, this.maxIterations, this.populationSize, this.crossoverOperator,
                this.mutationOperator, this.selectionOperator, this.evaluator, this.k);

        return algorithm;
    }

    /* Getters */
    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public SPEA2Builder<S> setMaxIterations(int maxIterations) {
        if (maxIterations < 0) {
            throw new JMetalException("maxIterations is negative: " + maxIterations);
        }
        this.maxIterations = maxIterations;

        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public SPEA2Builder<S> setPopulationSize(int populationSize) {
        if (populationSize < 0) {
            throw new JMetalException("Population size is negative: " + populationSize);
        }

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

    public SPEA2Builder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        if (selectionOperator == null) {
            throw new JMetalException("selectionOperator is null");
        }
        this.selectionOperator = selectionOperator;

        return this;
    }

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return this.evaluator;
    }

    public SPEA2Builder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }
}
