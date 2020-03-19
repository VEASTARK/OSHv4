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

import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class GenerationalGeneticAlgorithm<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, S> {
    private final Comparator<S> comparator;
    private final SolutionListEvaluator<S> evaluator;
    private int evaluations;

    /**
     * Constructor
     */
    public GenerationalGeneticAlgorithm(Problem<S> problem, int populationSize,
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
        offspringPopulation.remove(offspringPopulation.size() - 1);
        offspringPopulation.remove(offspringPopulation.size() - 1);

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

    @Override
    public String getName() {
        return "gGA";
    }

    @Override
    public String getDescription() {
        return "Generational Genetic Algorithm";
    }
}
