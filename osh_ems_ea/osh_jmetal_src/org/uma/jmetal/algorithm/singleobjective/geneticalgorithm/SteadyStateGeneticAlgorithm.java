package org.uma.jmetal.algorithm.singleobjective.geneticalgorithm;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SteadyStateGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
    private final Comparator<S> comparator;
    private int evaluations;

    /**
     * Constructor
     */
    public SteadyStateGeneticAlgorithm(Problem<S> problem, int populationSize,
                                       CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                       SelectionOperator<List<S>, S> selectionOperator, IEALogger eaLogger) {
        super(problem, eaLogger);
        this.setMaxPopulationSize(populationSize);

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;

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
        int worstSolutionIndex = population.size() - 1;
        if (this.comparator.compare(population.get(worstSolutionIndex), offspringPopulation.get(0)) > 0) {
            population.remove(worstSolutionIndex);
            population.add(offspringPopulation.get(0));
        }

        return population;
    }

    @Override
    protected List<S> reproduction(List<S> matingPopulation) {
        List<S> offspringPopulation = new ArrayList<>(1);

        List<S> parents = new ArrayList<>(2);
        parents.add(matingPopulation.get(0));
        parents.add(matingPopulation.get(1));

        List<S> offspring = this.crossoverOperator.execute(parents);
        this.mutationOperator.execute(offspring.get(0));

        offspringPopulation.add(offspring.get(0));
        return offspringPopulation;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            S solution = this.selectionOperator.execute(population);
            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<S> evaluatePopulation(List<S> population) {
        for (S solution : population) {
            this.getProblem().evaluate(solution);
        }

        return population;
    }

    @Override
    public S getResult() {
        this.getPopulation().sort(this.comparator);
        return this.getPopulation().get(0);
    }

    @Override
    public void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
        this.getEALogger().logPopulation(this.population, this.evaluations / this.getMaxPopulationSize());
    }

    @Override
    public void updateProgress() {
        this.evaluations++;

        if (this.evaluations % this.getMaxPopulationSize() == 0) {
            this.getEALogger().logPopulation(this.population, this.evaluations / this.getMaxPopulationSize());
        }
    }

    @Override
    public String getName() {
        return "ssGA";
    }

    @Override
    public String getDescription() {
        return "Steady-State Genetic Algorithm";
    }
}
