package org.uma.jmetal.algorithm.singleobjective.geneticalgorithm;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Legacy Algorithm mimicking behaviour of modified jMetal 4.5 algorithms in use in the OSH.
 */
@SuppressWarnings("serial")
public class OSHLegacyGenerationalGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
    private final Comparator<S> comparator;
    private final SolutionListEvaluator<S> evaluator;
    private int evaluations;

    public OSHLegacyGenerationalGeneticAlgorithm(Problem<S> problem, int populationSize,
                                                 CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                                 SelectionOperator<List<S>, S> selectionOperator,
                                                 SolutionListEvaluator<S> evaluator, IEALogger eaLogger) {
        super(problem, eaLogger);
        this.setMaxPopulationSize(populationSize);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;

        this.evaluator = evaluator;

        this.comparator = new ObjectiveComparator<>(0);
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                this.getEALogger().logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        population.sort(this.comparator);
        offspringPopulation.add(population.get(0));
        offspringPopulation.add(population.get(1));
        offspringPopulation.sort(this.comparator);

        return offspringPopulation;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        return this.evaluator.evaluate(population, this.getProblem());
    }

    @Override
    public S getResult() {
        this.getPopulation().sort(this.comparator);
        return this.getPopulation().get(0);
    }

    @Override
    public void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
        this.getEALogger().logPopulation(this.population, this.evaluations / this.maxPopulationSize);
    }

    @Override
    public void updateProgress() {
        this.evaluations += this.getMaxPopulationSize();
        this.getEALogger().logPopulation(this.population, this.evaluations / this.maxPopulationSize);
    }

    /**
     * This method iteratively applies a {@link SelectionOperator} to the population to fill the mating pool population.
     *
     * @param population
     * @return The mating pool population
     */
    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(2);
        matingPopulation.add(this.selectionOperator.execute(population));
        matingPopulation.add(this.selectionOperator.execute(population));

        return matingPopulation;
    }

    /**
     * This methods iteratively applies a {@link CrossoverOperator} a  {@link MutationOperator} to the population to
     * create the offspring population. The population size must be divisible by the number of parents required
     * by the {@link CrossoverOperator}; this way, the needed parents are taken sequentially from the population.
     * <p>
     * No limits are imposed to the number of solutions returned by the {@link CrossoverOperator}.
     *
     * @param population
     * @return The new created offspring population
     */
    @Override
    protected List<S> reproduction(List<S> population) {
        int numberOfParents = this.crossoverOperator.getNumberOfRequiredParents();

        this.checkNumberOfParents(population, numberOfParents);

        List<S> offspringPopulation = new ArrayList<>(2);
        List<S> parents = new ArrayList<>(numberOfParents);
        parents.add(population.get(0));
        parents.add(population.get(1));

        List<S> offspring = this.crossoverOperator.execute(parents);

        for (S s : offspring) {
            this.mutationOperator.execute(s);
            offspringPopulation.add(s);
        }
        return offspringPopulation;
    }

    @Override
    public void run() {
        List<S> offspringPopulation;
        List<S> offspringPool;
        List<S> matingPopulation;

        this.population = this.createInitialPopulation();
        this.population = this.evaluatePopulation(this.population);
        this.initProgress();
        while (!this.isStoppingConditionReached()) {
            offspringPool = new ArrayList<>();
            for (int i = 0; i < ((this.maxPopulationSize / 2) - 1); i++) {
                matingPopulation = this.selection(this.population);
                offspringPopulation = this.reproduction(matingPopulation);
                offspringPool.addAll(offspringPopulation);
            }
            this.evaluatePopulation(offspringPool);
            this.population = this.replacement(this.population, offspringPool);
            this.updateProgress();
        }
    }

    @Override
    public String getName() {
        return "OLgGA";
    }

    @Override
    public String getDescription() {
        return "OSH Legacy Generational Genetic Algorithm";
    }
}
