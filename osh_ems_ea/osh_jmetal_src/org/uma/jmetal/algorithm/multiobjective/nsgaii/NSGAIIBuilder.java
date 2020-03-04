package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class NSGAIIBuilder<S extends Solution<?>> implements AlgorithmBuilder<NSGAII<S>> {
    /**
     * NSGAIIBuilder class
     */
    private final Problem<S> problem;
    private final int populationSize;
    private final CrossoverOperator<S> crossoverOperator;
    private final MutationOperator<S> mutationOperator;
    protected int matingPoolSize;
    protected int offspringPopulationSize;
    private int maxEvaluations;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;
    private Comparator<S> dominanceComparator;
    private NSGAIIVariant variant;

    /**
     * NSGAIIBuilder constructor
     */
    public NSGAIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                         MutationOperator<S> mutationOperator, int populationSize) {
        this.problem = problem;
        this.maxEvaluations = 25000;
        this.populationSize = populationSize;
        this.matingPoolSize = populationSize;
        this.offspringPopulationSize = populationSize;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.dominanceComparator = new DominanceComparator<>();

        this.variant = NSGAIIVariant.NSGAII;
    }

    public NSGAIIBuilder<S> setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
        }
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public NSGAIIBuilder<S> setMatingPoolSize(int matingPoolSize) {
        if (matingPoolSize < 0) {
            throw new JMetalException("The mating pool size is negative: " + this.populationSize);
        }

        this.matingPoolSize = matingPoolSize;

        return this;
    }

    public NSGAIIBuilder<S> setOffspringPopulationSize(int offspringPopulationSize) {
        if (offspringPopulationSize < 0) {
            throw new JMetalException("Offspring population size is negative: " + this.populationSize);
        }

        this.offspringPopulationSize = offspringPopulationSize;

        return this;
    }

    public NSGAIIBuilder<S> setDominanceComparator(Comparator<S> dominanceComparator) {
        if (dominanceComparator == null) {
            throw new JMetalException("dominanceComparator is null");
        }
        this.dominanceComparator = dominanceComparator;

        return this;
    }

    public NSGAIIBuilder<S> setVariant(NSGAIIVariant variant) {
        this.variant = variant;

        return this;
    }

    public NSGAII<S> build() {
        NSGAII<S> algorithm = null;
        if (this.variant == NSGAIIVariant.NSGAII) {
            algorithm = new NSGAII<>(this.problem, this.maxEvaluations, this.populationSize, this.matingPoolSize, this.offspringPopulationSize,
                    this.crossoverOperator,
                    this.mutationOperator, this.selectionOperator, this.dominanceComparator, this.evaluator);
        } else if (this.variant == NSGAIIVariant.SteadyStateNSGAII) {
            algorithm = new SteadyStateNSGAII<>(this.problem, this.maxEvaluations, this.populationSize, this.crossoverOperator,
                    this.mutationOperator, this.selectionOperator, this.dominanceComparator, this.evaluator);
        } else if (this.variant == NSGAIIVariant.Measures) {
            algorithm = new NSGAIIMeasures<>(this.problem, this.maxEvaluations, this.populationSize, this.matingPoolSize, this.offspringPopulationSize,
                    this.crossoverOperator, this.mutationOperator, this.selectionOperator, this.dominanceComparator, this.evaluator);
        } else if (this.variant == NSGAIIVariant.DNSGAII) {
            algorithm = new DNSGAII<>(this.problem, this.maxEvaluations, this.populationSize, this.matingPoolSize, this.offspringPopulationSize,
                    this.crossoverOperator, this.mutationOperator, this.selectionOperator, this.dominanceComparator, this.evaluator);
        }

        return algorithm;
    }

    /* Getters */
    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxIterations() {
        return this.maxEvaluations;
    }

    public int getPopulationSize() {
        return this.populationSize;
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

    public NSGAIIBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        if (selectionOperator == null) {
            throw new JMetalException("selectionOperator is null");
        }
        this.selectionOperator = selectionOperator;

        return this;
    }

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return this.evaluator;
    }

    public NSGAIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }

    public enum NSGAIIVariant {NSGAII, SteadyStateNSGAII, Measures, NSGAII45, DNSGAII}
}
