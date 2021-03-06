package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAII<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {

    protected final SolutionListEvaluator<S> evaluator;
    protected final Comparator<S> dominanceComparator;
    protected final int matingPoolSize;
    protected final int offspringPopulationSize;
    protected int evaluations;

    /**
     * Constructor
     */
    public NSGAII(Problem<S> problem, int populationSize,
                  int matingPoolSize, int offspringPopulationSize,
                  CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator,
                  IEALogger eaLogger) {
        this(problem, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator, selectionOperator, new DominanceComparator<>(), evaluator, eaLogger);
    }

    /**
     * Constructor
     */
    public NSGAII(Problem<S> problem, int populationSize,
                  int matingPoolSize, int offspringPopulationSize,
                  CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                  SolutionListEvaluator<S> evaluator, IEALogger eaLogger) {
        super(problem, eaLogger);
        this.setMaxPopulationSize(populationSize);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;

        this.evaluator = evaluator;
        this.dominanceComparator = dominanceComparator;

        this.matingPoolSize = matingPoolSize;
        this.offspringPopulationSize = offspringPopulationSize;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
        this.getEALogger().logPopulation(this.population, this.evaluations / this.getMaxPopulationSize());
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.offspringPopulationSize;
        this.getEALogger().logPopulation(this.population, this.evaluations / this.getMaxPopulationSize());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.getPopulation())) {
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

    /**
     * This method iteratively applies a {@link SelectionOperator} to the population to fill the mating pool population.
     *
     * @param population
     * @return The mating pool population
     */
    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(population.size());
        for (int i = 0; i < this.matingPoolSize; i++) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    /**
     * This methods iteratively applies a {@link CrossoverOperator} a  {@link MutationOperator} to the population to
     * create the offspring population. The population size must be divisible by the number of parents required
     * by the {@link CrossoverOperator}; this way, the needed parents are taken sequentially from the population.
     * <p>
     * The number of solutions returned by the {@link CrossoverOperator} must be equal to the offspringPopulationSize
     * state variable
     *
     * @param matingPool
     * @return The new created offspring population
     */
    @Override
    protected List<S> reproduction(List<S> matingPool) {
        int numberOfParents = this.crossoverOperator.getNumberOfRequiredParents();

        this.checkNumberOfParents(matingPool, numberOfParents);

        List<S> offspringPopulation = new ArrayList<>(this.offspringPopulationSize);
        for (int i = 0; i < matingPool.size(); i += numberOfParents) {
            List<S> parents = new ArrayList<>(numberOfParents);
            for (int j = 0; j < numberOfParents; j++) {
                parents.add(this.population.get(i + j));
            }

            List<S> offspring = this.crossoverOperator.execute(parents);

            for (S s : offspring) {
                this.mutationOperator.execute(s);
                offspringPopulation.add(s);
                if (offspringPopulation.size() >= this.offspringPopulationSize)
                    break;
            }
        }
        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        RankingAndCrowdingSelection<S> rankingAndCrowdingSelection;
        rankingAndCrowdingSelection = new RankingAndCrowdingSelection<>(this.getMaxPopulationSize(), this.dominanceComparator);

        return rankingAndCrowdingSelection.execute(jointPopulation);
    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(this.getPopulation());
    }

    @Override
    public String getName() {
        return "NSGAII";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II";
    }
}
