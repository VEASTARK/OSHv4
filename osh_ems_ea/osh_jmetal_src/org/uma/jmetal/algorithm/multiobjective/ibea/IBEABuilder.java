package org.uma.jmetal.algorithm.multiobjective.ibea;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;

import java.util.List;

/**
 * This class implements the IBEA algorithm
 */
public class IBEABuilder implements AlgorithmBuilder<IBEA<DoubleSolution>> {
    private final Problem<DoubleSolution> problem;
    private int populationSize;
    private int archiveSize;
    private int maxEvaluations;

    private CrossoverOperator<DoubleSolution> crossover;
    private MutationOperator<DoubleSolution> mutation;
    private SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;

    /**
     * Constructor
     *
     * @param problem
     */
    public IBEABuilder(Problem<DoubleSolution> problem) {
        this.problem = problem;
        this.populationSize = 100;
        this.archiveSize = 100;
        this.maxEvaluations = 25000;

        double crossoverProbability = 0.9;
        double crossoverDistributionIndex = 20.0;
        this.crossover = new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        double mutationProbability = 1.0 / problem.getNumberOfVariables();
        double mutationDistributionIndex = 20.0;
        this.mutation = new PolynomialMutation(mutationProbability, mutationDistributionIndex);

        this.selection = new BinaryTournamentSelection<>();
    }

    /* Getters */
    public int getPopulationSize() {
        return this.populationSize;
    }

    /* Setters */
    public IBEABuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getArchiveSize() {
        return this.archiveSize;
    }

    public IBEABuilder setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public IBEABuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public CrossoverOperator<DoubleSolution> getCrossover() {
        return this.crossover;
    }

    public IBEABuilder setCrossover(CrossoverOperator<DoubleSolution> crossover) {
        this.crossover = crossover;

        return this;
    }

    public MutationOperator<DoubleSolution> getMutation() {
        return this.mutation;
    }

    public IBEABuilder setMutation(MutationOperator<DoubleSolution> mutation) {
        this.mutation = mutation;

        return this;
    }

    public SelectionOperator<List<DoubleSolution>, DoubleSolution> getSelection() {
        return this.selection;
    }

    public IBEABuilder setSelection(SelectionOperator<List<DoubleSolution>, DoubleSolution> selection) {
        this.selection = selection;

        return this;
    }

    public IBEA<DoubleSolution> build() {
        IBEA<DoubleSolution> algorithm = new IBEA<>(this.problem, this.populationSize, this.archiveSize, this.selection,
            this.crossover, this.mutation);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }
}
