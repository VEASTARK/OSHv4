package org.uma.jmetal.algorithm.multiobjective.pesa2;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.multiobjective.pesa2.util.PESA2Selection;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.impl.AdaptiveGridArchive;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class PESA2<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final SelectionOperator<AdaptiveGridArchive<S>, S> selectionOperator;
    protected final SolutionListEvaluator<S> evaluator;
    private final int archiveSize;
    private final int biSections;
    private final AdaptiveGridArchive<S> archive;
    private int evaluations;

    public PESA2(Problem<S> problem, int populationSize, int archiveSize,
                 int biSections, CrossoverOperator<S> crossoverOperator,
                 MutationOperator<S> mutationOperator, SolutionListEvaluator<S> evaluator) {
        super(problem);
        this.setMaxPopulationSize(populationSize);
        this.archiveSize = archiveSize;
        this.biSections = biSections;

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = new PESA2Selection<>();

        this.evaluator = evaluator;

        this.archive = new AdaptiveGridArchive<>(this.archiveSize, this.biSections, problem.getNumberOfObjectives());
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.getMaxPopulationSize();
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
        population = this.evaluator.evaluate(population, this.getProblem());

        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(this.getMaxPopulationSize());

        for (S solution : population) {
            this.archive.add(solution);
        }

        while (matingPopulation.size() < this.getMaxPopulationSize()) {
            S solution = this.selectionOperator.execute(this.archive);

            matingPopulation.add(solution);
        }

        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(this.getMaxPopulationSize());
        for (int i = 0; i < this.getMaxPopulationSize(); i += 2) {
            List<S> parents = new ArrayList<>(2);
            parents.add(population.get(i));
            parents.add(population.get(i + 1));

            List<S> offspring = this.crossoverOperator.execute(parents);

            this.mutationOperator.execute(offspring.get(0));

            offspringPopulation.add(offspring.get(0));
        }
        return offspringPopulation;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        for (S solution : offspringPopulation) {
            this.archive.add(solution);
        }

        return Collections.emptyList();
    }

    @Override
    public List<S> getResult() {
        return this.archive.getSolutionList();
    }

    @Override
    public String getName() {
        return "PESA2";
    }

    @Override
    public String getDescription() {
        return "Pareto Envelope-based Selection Algorithm ";
    }
}
