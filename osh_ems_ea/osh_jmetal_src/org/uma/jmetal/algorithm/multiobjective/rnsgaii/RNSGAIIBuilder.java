package org.uma.jmetal.algorithm.multiobjective.rnsgaii;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class RNSGAIIBuilder<S extends Solution<?>> implements AlgorithmBuilder<RNSGAII<S>> {
    /**
     * NSGAIIBuilder class
     */
    private final Problem<S> problem;
    private final IEALogger eaLogger;
    private final CrossoverOperator<S> crossoverOperator;
    private final MutationOperator<S> mutationOperator;
    private final List<Double> interestPoint;
    private final double epsilon;
    protected int matingPoolSize;
    protected int offspringPopulationSize;
    private int maxEvaluations;
    private int populationSize;
    private SelectionOperator<List<S>, S> selectionOperator;
    private SolutionListEvaluator<S> evaluator;

    /**
     * NSGAIIBuilder constructor
     */
    public RNSGAIIBuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator, List<Double> interestPoint, double epsilon,
                          IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.matingPoolSize = 100;
        this.offspringPopulationSize = 100;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.epsilon = epsilon;
        this.interestPoint = interestPoint;
    }

    public RNSGAIIBuilder<S> setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
        }
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public RNSGAIIBuilder<S> setMatingPoolSize(int matingPoolSize) {
        if (matingPoolSize < 0) {
            throw new JMetalException("The mating pool size is negative: " + this.populationSize);
        }

        this.matingPoolSize = matingPoolSize;

        return this;
    }

    public RNSGAIIBuilder<S> setOffspringPopulationSize(int offspringPopulationSize) {
        if (offspringPopulationSize < 0) {
            throw new JMetalException("Offspring population size is negative: " + this.populationSize);
        }

        this.offspringPopulationSize = offspringPopulationSize;

        return this;
    }

    public RNSGAII<S> build() {
        RNSGAII<S> algorithm;

        algorithm = new RNSGAII<>(this.problem, this.populationSize, this.matingPoolSize, this.offspringPopulationSize,
                this.crossoverOperator, this.mutationOperator, this.selectionOperator, this.evaluator, this.interestPoint,
                this.epsilon, this.eaLogger);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
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

    public RNSGAIIBuilder<S> setPopulationSize(int populationSize) {
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

    public RNSGAIIBuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selectionOperator) {
        if (selectionOperator == null) {
            throw new JMetalException("selectionOperator is null");
        }
        this.selectionOperator = selectionOperator;

        return this;
    }

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return this.evaluator;
    }

    public RNSGAIIBuilder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }
}
