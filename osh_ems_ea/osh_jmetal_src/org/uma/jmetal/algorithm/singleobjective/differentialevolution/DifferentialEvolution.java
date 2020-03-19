package org.uma.jmetal.algorithm.singleobjective.differentialevolution;

import org.uma.jmetal.algorithm.impl.AbstractDifferentialEvolution;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements a differential evolution algorithm.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DifferentialEvolution extends AbstractDifferentialEvolution<DoubleSolution> {
    private final int populationSize;
    private final SolutionListEvaluator<DoubleSolution> evaluator;
    private final Comparator<DoubleSolution> comparator;

    private int evaluations;

    /**
     * Constructor
     *
     * @param problem           Problem to solve
     * @param populationSize
     * @param crossoverOperator
     * @param selectionOperator
     * @param evaluator
     */
    public DifferentialEvolution(DoubleProblem problem, int populationSize,
                                 DifferentialEvolutionCrossover crossoverOperator,
                                 DifferentialEvolutionSelection selectionOperator,
                                 SolutionListEvaluator<DoubleSolution> evaluator,
                                 IEALogger eaLogger) {
        this.setProblem(problem);
        this.populationSize = populationSize;
        this.crossoverOperator = crossoverOperator;
        this.selectionOperator = selectionOperator;
        this.evaluator = evaluator;

        this.comparator = new ObjectiveComparator<>(0);

        this.setEALogger(eaLogger);
        this.getEALogger().logStart(this);
    }

    public int getEvaluations() {
        return this.evaluations;
    }

    public void setEvaluations(int evaluations) {
        this.evaluations = evaluations;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.populationSize;
        this.getEALogger().logPopulation(this.population, this.evaluations / this.populationSize);
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.populationSize;
        this.getEALogger().logPopulation(this.population, this.evaluations / this.populationSize);
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
    protected List<DoubleSolution> createInitialPopulation() {
        List<DoubleSolution> population = new ArrayList<>(this.populationSize);
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newIndividual = this.getProblem().createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    @Override
    protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
        return this.evaluator.evaluate(population, this.getProblem());
    }

    @Override
    protected List<DoubleSolution> selection(List<DoubleSolution> population) {
        return population;
    }

    @Override
    protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
        List<DoubleSolution> offspringPopulation = new ArrayList<>();

        for (int i = 0; i < this.populationSize; i++) {
            this.selectionOperator.setIndex(i);
            List<DoubleSolution> parents = this.selectionOperator.execute(matingPopulation);

            this.crossoverOperator.setCurrentSolution(matingPopulation.get(i));
            List<DoubleSolution> children = this.crossoverOperator.execute(parents);

            offspringPopulation.add(children.get(0));
        }

        return offspringPopulation;
    }

    @Override
    protected List<DoubleSolution> replacement(List<DoubleSolution> population,
                                               List<DoubleSolution> offspringPopulation) {
        List<DoubleSolution> pop = new ArrayList<>();

        for (int i = 0; i < this.populationSize; i++) {
            if (this.comparator.compare(population.get(i), offspringPopulation.get(i)) < 0) {
                pop.add(population.get(i));
            } else {
                pop.add(offspringPopulation.get(i));
            }
        }

        pop.sort(this.comparator);
        return pop;
    }

    /**
     * Returns the best individual
     */
    @Override
    public DoubleSolution getResult() {
        this.getPopulation().sort(this.comparator);

        return this.getPopulation().get(0);
    }

    @Override
    public String getName() {
        return "DE";
    }

    @Override
    public String getDescription() {
        return "Differential Evolution Algorithm";
    }
}
