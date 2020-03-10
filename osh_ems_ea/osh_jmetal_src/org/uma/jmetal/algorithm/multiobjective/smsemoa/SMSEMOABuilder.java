package org.uma.jmetal.algorithm.multiobjective.smsemoa;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RandomSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;

import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMSEMOABuilder<S extends Solution<?>> implements AlgorithmBuilder<SMSEMOA<S>> {
    private static final double DEFAULT_OFFSET = 100.0;

    protected final Problem<S> problem;

    protected int populationSize;
    protected int maxEvaluations;

    protected CrossoverOperator<S> crossoverOperator;
    protected MutationOperator<S> mutationOperator;
    protected SelectionOperator<List<S>, S> selectionOperator;

    protected double offset;

    protected Hypervolume<S> hypervolumeImplementation;
    protected Comparator<S> dominanceComparator;

    public SMSEMOABuilder(Problem<S> problem, CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator) {
        this.problem = problem;
        this.offset = DEFAULT_OFFSET;
        this.populationSize = 100;
        this.maxEvaluations = 25000;
        this.hypervolumeImplementation = new PISAHypervolume<>();
        this.hypervolumeImplementation.setOffset(this.offset);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new RandomSelection<>();
        this.dominanceComparator = new DominanceComparator<>();
    }

    public SMSEMOABuilder<S> setHypervolumeImplementation(Hypervolume<S> hypervolumeImplementation) {
        this.hypervolumeImplementation = hypervolumeImplementation;

        return this;
    }

    public SMSEMOABuilder<S> setDominanceComparator(Comparator<S> dominanceComparator) {
        if (dominanceComparator == null) {
            throw new JMetalException("dominanceComparator is null");
        }
        this.dominanceComparator = dominanceComparator;

        return this;
    }

    @Override
    public SMSEMOA<S> build() {
        SMSEMOA<S> algorithm = new SMSEMOA<>(this.problem, this.populationSize, this.offset,
                this.crossoverOperator, this.mutationOperator, this.selectionOperator, this.dominanceComparator,
                this.hypervolumeImplementation);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }

    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public SMSEMOABuilder<S> setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public SMSEMOABuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public CrossoverOperator<S> getCrossoverOperator() {
        return this.crossoverOperator;
    }

    /*
     * Getters
     */

    public SMSEMOABuilder<S> setCrossoverOperator(CrossoverOperator<S> crossover) {
        this.crossoverOperator = crossover;

        return this;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public SMSEMOABuilder<S> setMutationOperator(MutationOperator<S> mutation) {
        this.mutationOperator = mutation;

        return this;
    }

    public SelectionOperator<List<S>, S> getSelectionOperator() {
        return this.selectionOperator;
    }

    public SMSEMOABuilder<S> setSelectionOperator(SelectionOperator<List<S>, S> selection) {
        this.selectionOperator = selection;

        return this;
    }

    public double getOffset() {
        return this.offset;
    }

    public SMSEMOABuilder<S> setOffset(double offset) {
        this.offset = offset;

        return this;
    }
}