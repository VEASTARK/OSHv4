package org.uma.jmetal.algorithm.multiobjective.mocell;

import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.neighborhood.Neighborhood;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.util.solutionattribute.impl.LocationAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @param <S>
 * @author JuanJo Durillo
 */
@SuppressWarnings("serial")
public class MOCell<S extends Solution<?>> extends AbstractGeneticAlgorithm<S, List<S>> {
    protected final SolutionListEvaluator<S> evaluator;
    protected final Neighborhood<S> neighborhood;
    protected final BoundedArchive<S> archive;
    protected final Comparator<S> dominanceComparator;
    protected int evaluations;
    protected int currentIndividual;
    protected List<S> currentNeighbors;
    protected LocationAttribute<S> location;

    /**
     * Constructor
     *
     * @param problem
     * @param populationSize
     * @param neighborhood
     * @param crossoverOperator
     * @param mutationOperator
     * @param selectionOperator
     * @param evaluator
     */
    public MOCell(Problem<S> problem, int populationSize, BoundedArchive<S> archive,
                  Neighborhood<S> neighborhood,
                  CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                  SelectionOperator<List<S>, S> selectionOperator, SolutionListEvaluator<S> evaluator) {
        super(problem);
        this.setMaxPopulationSize(populationSize);
        this.archive = archive;
        this.neighborhood = neighborhood;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.dominanceComparator = new DominanceComparator<>();

        this.evaluator = evaluator;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.getMaxPopulationSize();
        this.currentIndividual = 0;
    }

    @Override
    protected void updateProgress() {
        this.evaluations++;
        this.currentIndividual = (this.currentIndividual + 1) % this.getMaxPopulationSize();
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
    protected List<S> createInitialPopulation() {
        List<S> population = new ArrayList<>(this.getMaxPopulationSize());
        for (int i = 0; i < this.getMaxPopulationSize(); i++) {
            S newIndividual = this.getProblem().createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected List<S> evaluatePopulation(List<S> population) {
        population = this.evaluator.evaluate(population, this.getProblem());
        for (S solution : population) {
            this.archive.add((S) solution.copy());
        }

        return population;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> parents = new ArrayList<>(2);
        this.currentNeighbors = this.neighborhood.getNeighbors(population, this.currentIndividual);
        this.currentNeighbors.add(population.get(this.currentIndividual));

        parents.add(this.selectionOperator.execute(this.currentNeighbors));
        if (this.archive.size() > 0) { // TODO. REVISAR EN EL CASO DE TAMAÑO 1
            parents.add(this.selectionOperator.execute(this.archive.getSolutionList()));
        } else {
            parents.add(this.selectionOperator.execute(this.currentNeighbors));
        }
        return parents;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> result = new ArrayList<>(1);
        List<S> offspring = this.crossoverOperator.execute(population);
        this.mutationOperator.execute(offspring.get(0));
        result.add(offspring.get(0));
        return result;
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        this.location = new LocationAttribute<>(population);

        int flag = this.dominanceComparator.compare(population.get(this.currentIndividual), offspringPopulation.get(0));

        if (flag > 0) { //The new individual dominates
            population = this.insertNewIndividualWhenDominates(population, offspringPopulation);
        } else if (flag == 0) { //The new individual is non-dominated
            population = this.insertNewIndividualWhenNonDominated(population, offspringPopulation);
        }
        return population;
    }

    @Override
    public List<S> getResult() {
        return this.archive.getSolutionList();
    }

    private List<S> insertNewIndividualWhenDominates(List<S> population, List<S> offspringPopulation) {
        this.location.setAttribute(offspringPopulation.get(0),
                this.location.getAttribute(population.get(this.currentIndividual)));
        List<S> result = new ArrayList<>(population);
        result.set(this.location.getAttribute(offspringPopulation.get(0)), offspringPopulation.get(0));
        this.archive.add(offspringPopulation.get(0));
        return result;
    }

    private List<S> insertNewIndividualWhenNonDominated(List<S> population, List<S> offspringPopulation) {
        this.currentNeighbors.add(offspringPopulation.get(0));
        this.location.setAttribute(offspringPopulation.get(0), -1);
        List<S> result = new ArrayList<>(population);
        Ranking<S> rank = new DominanceRanking<>();
        rank.computeRanking(this.currentNeighbors);

        CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();
        for (int j = 0; j < rank.getNumberOfSubfronts(); j++) {
            crowdingDistance.computeDensityEstimator(rank.getSubfront(j));
        }

        this.currentNeighbors.sort(new RankingAndCrowdingDistanceComparator<>());
        S worst = this.currentNeighbors.get(this.currentNeighbors.size() - 1);

        if (this.location.getAttribute(worst) == -1) { //The worst is the offspring
            this.archive.add(offspringPopulation.get(0));
        } else {
            this.location.setAttribute(offspringPopulation.get(0),
                    this.location.getAttribute(worst));
            result.set(this.location.getAttribute(offspringPopulation.get(0)), offspringPopulation.get(0));
            this.archive.add(offspringPopulation.get(0));

        }
        return result;
    }

    @Override
    public String getName() {
        return "MOCell";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Cellular evolutionry algorithm";
    }


}
