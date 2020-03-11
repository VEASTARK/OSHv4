package org.uma.jmetal.algorithm.multiobjective.espea;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.espea.util.EnergyArchive;
import org.uma.jmetal.algorithm.multiobjective.espea.util.EnergyArchive.ReplacementStrategy;
import org.uma.jmetal.algorithm.multiobjective.espea.util.ScalarizationWrapper;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the Electrostatic Potential Energy Evolutionary Algorithm
 * (ESPEA) from the paper "Obtaining Optimal Pareto Front Approximations using
 * Scalarized Preference Information" by M. Braun et al.
 *
 * <p>
 * The algorithm generates preference-biased Pareto front approximations that
 * cover the entire front but focus more solutions in those regions that are
 * interesting to the decision maker. Preferences are presented to the algorithm
 * in the form of a scalarization function (value function) that maps the vector
 * of objective to a real value. Smaller values are deemed to indicate higher
 * desirability to comply with minimization.
 *
 * <p>
 * If no scalarized preference is specified, uniform preferences are assumed and
 * ESPEA generates a uniform approximation of the Pareto front.
 *
 * @author Marlon Braun
 */
@SuppressWarnings("serial")
public class ESPEA<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

    /**
     * ESPEA uses two different crossover operators depending on the current
     * archive size. If the archive is not full, it uses the crossover operator
     * provided by {@link #getCrossoverOperator()}. If the archive is full,
     * {@link #fullArchiveCrossoverOperator} is used.
     */
    protected final CrossoverOperator<S> fullArchiveCrossoverOperator;
    /**
     * Evaluates the solutions
     */
    protected final SolutionListEvaluator<S> evaluator;
    /**
     * An archive of nondominated solutions that approximates the energy minimum
     * state based on the chosen scalarization function.
     */
    protected final EnergyArchive<S> archive;
    /**
     * The number of function evaluations that have been executed so far.
     */
    protected int evaluations;

    /**
     * Constructor for setting all parameters of ESPEA.
     */
    public ESPEA(Problem<S> problem, int populationSize, CrossoverOperator<S> crossoverOperator,
                 CrossoverOperator<S> fullArchiveCrossoverOperator, MutationOperator<S> mutationOperator,
                 SelectionOperator<List<S>, S> selectionOperator, ScalarizationWrapper scalarizationWrapper, SolutionListEvaluator<S> evaluator,
                 boolean normalizeObjectives, ReplacementStrategy replacementStrategy) {
        super(problem);
        this.setMaxPopulationSize(populationSize);
        this.crossoverOperator = crossoverOperator;
        this.fullArchiveCrossoverOperator = fullArchiveCrossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.archive = new EnergyArchive<>(populationSize, scalarizationWrapper, normalizeObjectives, replacementStrategy);
        this.evaluator = evaluator;
    }

    @Override
    public String getName() {
        return "ESPEA";
    }

    @Override
    public String getDescription() {
        return "Electrostatic Potential Energy Evolutionary Algorithms";
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
        // Initialize archive
        this.population.forEach(this.archive::add);
    }

    @Override
    protected void updateProgress() {
        // ESPEA is steady-state
        this.evaluations++;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.archive.getSolutionList())) {
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
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        // population and archive are always in sync
        offspringPopulation.forEach(this.archive::add);
        return this.archive.getSolutionList();
    }

    @Override
    public List<S> getResult() {
        return this.archive.getSolutionList();
    }

    /*
     * Steady-state implementation of mating selection.
     *
     * @see
     * org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm#selection(java.
     * util.List)
     */
    @Override
    protected List<S> selection(List<S> population) {
        // Chosen operator depends on archive size
        CrossoverOperator<S> chosenOperator = this.archive.isFull() ? this.fullArchiveCrossoverOperator : this.crossoverOperator;

        List<S> matingPopulation = new ArrayList<>(chosenOperator.getNumberOfRequiredParents());

        for (int i = 0; i < chosenOperator.getNumberOfRequiredParents(); i++) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    /*
     * Steady-state implementation of reproduction.
     *
     * @see
     * org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm#reproduction(java.
     * util.List)
     */
    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspring = this.crossoverOperator.execute(population);
        this.mutationOperator.execute(offspring.get(0));

        return offspring;
    }
}
