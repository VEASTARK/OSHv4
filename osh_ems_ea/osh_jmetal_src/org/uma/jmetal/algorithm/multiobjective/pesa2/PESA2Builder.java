package org.uma.jmetal.algorithm.multiobjective.pesa2;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

/**
 * Created by Antonio J. Nebro
 */
public class PESA2Builder<S extends Solution<?>> implements AlgorithmBuilder<PESA2<S>> {
    private final Problem<S> problem;
    private final CrossoverOperator<S> crossoverOperator;
    private final MutationOperator<S> mutationOperator;
    private int maxEvaluations;
    private int archiveSize;
    private int populationSize;
    private int biSections;
    private SolutionListEvaluator<S> evaluator;

    /**
     * Constructor
     */
    public PESA2Builder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                        MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.archiveSize = 100;
        this.biSections = 5;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;

        this.evaluator = new SequentialSolutionListEvaluator<>();
    }

    public PESA2Builder<S> setBisections(int biSections) {
        if (biSections < 0) {
            throw new JMetalException("biSections is negative: " + this.maxEvaluations);
        }
        this.biSections = biSections;

        return this;
    }

    public PESA2<S> build() {
        PESA2<S> algorithm;
        algorithm = new PESA2<>(this.problem, this.maxEvaluations, this.populationSize, this.archiveSize, this.biSections,
                this.crossoverOperator, this.mutationOperator, this.evaluator);

        return algorithm;
    }

    /* Getters */
    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public PESA2Builder<S> setMaxEvaluations(int maxEvaluations) {
        if (maxEvaluations < 0) {
            throw new JMetalException("maxEvaluations is negative: " + maxEvaluations);
        }
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public PESA2Builder<S> setPopulationSize(int populationSize) {
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

    public SolutionListEvaluator<S> getSolutionListEvaluator() {
        return this.evaluator;
    }

    public PESA2Builder<S> setSolutionListEvaluator(SolutionListEvaluator<S> evaluator) {
        if (evaluator == null) {
            throw new JMetalException("evaluator is null");
        }
        this.evaluator = evaluator;

        return this;
    }

    public int getBiSections() {
        return this.biSections;
    }

    public int getArchiveSize() {
        return this.archiveSize;
    }

    public PESA2Builder<S> setArchiveSize(int archiveSize) {
        if (archiveSize < 0) {
            throw new JMetalException("archiveSize is negative: " + this.maxEvaluations);
        }
        this.archiveSize = archiveSize;

        return this;
    }
}
