package org.uma.jmetal.algorithm.multiobjective.gde3;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import java.util.List;

/**
 * This class implements the GDE3 algorithm
 */
public class GDE3Builder implements AlgorithmBuilder<GDE3> {
    private final DoubleProblem problem;
    protected int populationSize;
    protected int maxEvaluations;

    protected DifferentialEvolutionCrossover crossoverOperator;
    protected DifferentialEvolutionSelection selectionOperator;

    protected SolutionListEvaluator<DoubleSolution> evaluator;

    /**
     * Constructor
     */
    public GDE3Builder(DoubleProblem problem) {
        this.problem = problem;
        this.maxEvaluations = 25000;
        this.populationSize = 100;
        this.selectionOperator = new DifferentialEvolutionSelection();
        this.crossoverOperator = new DifferentialEvolutionCrossover();
        this.evaluator = new SequentialSolutionListEvaluator<>();
    }

    public GDE3Builder setCrossover(DifferentialEvolutionCrossover crossover) {
        this.crossoverOperator = crossover;

        return this;
    }

    public GDE3Builder setSelection(DifferentialEvolutionSelection selection) {
        this.selectionOperator = selection;

        return this;
    }

    public GDE3Builder setSolutionSetEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public GDE3 build() {
        return new GDE3(this.problem, this.populationSize, this.maxEvaluations, this.selectionOperator, this.crossoverOperator, this.evaluator);
    }

    /* Getters */
    public CrossoverOperator<DoubleSolution> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public SelectionOperator<List<DoubleSolution>, List<DoubleSolution>> getSelectionOperator() {
        return this.selectionOperator;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    /* Setters */
    public GDE3Builder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public GDE3Builder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

}

