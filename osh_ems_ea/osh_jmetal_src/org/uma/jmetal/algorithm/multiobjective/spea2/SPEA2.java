package org.uma.jmetal.algorithm.multiobjective.spea2;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.spea2.util.EnvironmentalSelection;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.impl.StrengthRawFitness;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Juan J. Durillo
 **/
@SuppressWarnings("serial")
public class SPEA2<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final SolutionListEvaluator<S> evaluator;
    protected final StrengthRawFitness<S> strenghtRawFitness = new StrengthRawFitness<>();
    protected final EnvironmentalSelection<S> environmentalSelection;
    protected final int k;
    protected int iterations;
    protected List<S> archive;

    public SPEA2(Problem<S> problem, int populationSize,
                 CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                 SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
                 int k, IEALogger eaLogger) {
        super(problem, eaLogger);
        this.setMaxPopulationSize(populationSize);

        this.k = k;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.environmentalSelection = new EnvironmentalSelection<>(populationSize, k);

        this.archive = new ArrayList<>(populationSize);

        this.evaluator = evaluator;
    }

    @Override
    protected void initProgress() {
        this.iterations = 1;
        this.getEALogger().logPopulation(this.archive, this.iterations);
    }

    @Override
    protected void updateProgress() {
        this.iterations++;
        this.getEALogger().logPopulation(this.archive, this.iterations);
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations, -1, this.archive)) {
                this.getEALogger().logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        return this.evaluator.evaluate(population, this.getProblem());
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> union = new ArrayList<>(2 * this.getMaxPopulationSize());
        union.addAll(this.archive);
        union.addAll(population);
        this.strenghtRawFitness.computeDensityEstimator(union);
        this.archive = this.environmentalSelection.execute(union);
        return this.archive;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offSpringPopulation = new ArrayList<>(this.getMaxPopulationSize());

        while (offSpringPopulation.size() < this.getMaxPopulationSize()) {
            List<S> parents = new ArrayList<>(2);
            S candidateFirstParent = this.selectionOperator.execute(population);
            parents.add(candidateFirstParent);
            S candidateSecondParent;
            candidateSecondParent = this.selectionOperator.execute(population);
            parents.add(candidateSecondParent);

            List<S> offspring = this.crossoverOperator.execute(parents);
            this.mutationOperator.execute(offspring.get(0));
            offSpringPopulation.add(offspring.get(0));
        }
        return offSpringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population,
                                  List<S> offspringPopulation) {
        return offspringPopulation;
    }

    @Override
    public List<S> getResult() {
        return this.archive;
    }

    @Override
    public String getName() {
        return "SPEA2";
    }

    @Override
    public String getDescription() {
        return "Strength Pareto. Evolutionary Algorithm";
    }
}
