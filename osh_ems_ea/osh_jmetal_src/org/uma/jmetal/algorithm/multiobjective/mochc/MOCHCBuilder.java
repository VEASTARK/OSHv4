package org.uma.jmetal.algorithm.multiobjective.mochc;

/**
 * Created by ajnebro on 21/11/14.
 */

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.List;

/**
 * Builder class
 */
public class MOCHCBuilder implements AlgorithmBuilder<MOCHC> {
    final BinaryProblem problem;
    final IEALogger eaLogger;
    SolutionListEvaluator<BinarySolution> evaluator;
    int populationSize;
    int maxEvaluations;
    int convergenceValue;
    double preservedPopulation;
    double initialConvergenceCount;
    CrossoverOperator<BinarySolution> crossoverOperator;
    MutationOperator<BinarySolution> cataclysmicMutation;
    SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection;
    SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection;

    public MOCHCBuilder(BinaryProblem problem, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
        this.evaluator = new SequentialSolutionListEvaluator<>();
        this.populationSize = 100;
        this.maxEvaluations = 25000;
        this.convergenceValue = 3;
        this.preservedPopulation = 0.05;
        this.initialConvergenceCount = 0.25;
    }

    /* Getters */
    public BinaryProblem getProblem() {
        return this.problem;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    /* Setters */
    public MOCHCBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getMaxEvaluation() {
        return this.maxEvaluations;
    }

    public double getInitialConvergenceCount() {
        return this.initialConvergenceCount;
    }

    public MOCHCBuilder setInitialConvergenceCount(double initialConvergenceCount) {
        this.initialConvergenceCount = initialConvergenceCount;

        return this;
    }

    public int getConvergenceValue() {
        return this.convergenceValue;
    }

    public MOCHCBuilder setConvergenceValue(int convergenceValue) {
        this.convergenceValue = convergenceValue;

        return this;
    }

    public CrossoverOperator<BinarySolution> getCrossover() {
        return this.crossoverOperator;
    }

    public MOCHCBuilder setCrossover(CrossoverOperator<BinarySolution> crossover) {
        this.crossoverOperator = crossover;

        return this;
    }

    public MutationOperator<BinarySolution> getCataclysmicMutation() {
        return this.cataclysmicMutation;
    }

    public MOCHCBuilder setCataclysmicMutation(MutationOperator<BinarySolution> cataclysmicMutation) {
        this.cataclysmicMutation = cataclysmicMutation;

        return this;
    }

    public SelectionOperator<List<BinarySolution>, BinarySolution> getParentSelection() {
        return this.parentSelection;
    }

    public MOCHCBuilder setParentSelection(SelectionOperator<List<BinarySolution>, BinarySolution> parentSelection) {
        this.parentSelection = parentSelection;

        return this;
    }

    public SelectionOperator<List<BinarySolution>, List<BinarySolution>> getNewGenerationSelection() {
        return this.newGenerationSelection;
    }

    public MOCHCBuilder setNewGenerationSelection(SelectionOperator<List<BinarySolution>, List<BinarySolution>> newGenerationSelection) {
        this.newGenerationSelection = newGenerationSelection;

        return this;
    }

    public double getPreservedPopulation() {
        return this.preservedPopulation;
    }

    public MOCHCBuilder setPreservedPopulation(double preservedPopulation) {
        this.preservedPopulation = preservedPopulation;

        return this;
    }

    public MOCHCBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public MOCHCBuilder setEvaluator(SolutionListEvaluator<BinarySolution> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public MOCHC build() {

        MOCHC algorithm = new MOCHC(this.problem, this.populationSize, this.convergenceValue, this.preservedPopulation,
                this.initialConvergenceCount, this.crossoverOperator, this.cataclysmicMutation, this.newGenerationSelection,
                this.parentSelection, this.evaluator, this.eaLogger);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }
}
