package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of NSGA-II following the scheme used in jMetal4.5 and former versions, i.e, without
 * implementing the {@link AbstractGeneticAlgorithm} interface.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAII45<S extends Solution<?>> implements Algorithm<List<S>> {
    protected final int populationSize;
    protected final Problem<S> problem;
    protected final SolutionListEvaluator<S> evaluator;
    protected final SelectionOperator<List<S>, S> selectionOperator;
    protected final CrossoverOperator<S> crossoverOperator;
    protected final MutationOperator<S> mutationOperator;
    protected List<S> population;
    protected int evaluations;

    private final List<StoppingRule> stoppingRules = new ArrayList<>();
    private IEALogger eaLogger;

    /**
     * Constructor
     */
    public NSGAII45(Problem<S> problem, int populationSize, CrossoverOperator<S> crossoverOperator,
                    MutationOperator<S> mutationOperator, SelectionOperator<List<S>, S> selectionOperator,
                    SolutionListEvaluator<S> evaluator, IEALogger eaLogger) {
        super();
        this.problem = problem;
        this.populationSize = populationSize;

        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;

        this.evaluator = evaluator;

        this.eaLogger = eaLogger;
        this.eaLogger.logStart(this);
    }

    /**
     * Run method
     */
    @Override
    public void run() {
        this.population = this.createInitialPopulation();
        this.evaluatePopulation(this.population);

        this.evaluations = this.populationSize;
        this.eaLogger.logPopulation(this.population, this.evaluations / this.populationSize);

        while (!this.isStoppingConditionReached()) {
            List<S> offspringPopulation = new ArrayList<>(this.populationSize);
            for (int i = 0; i < this.populationSize; i += 2) {
                List<S> parents = new ArrayList<>(2);
                parents.add(this.selectionOperator.execute(this.population));
                parents.add(this.selectionOperator.execute(this.population));

                List<S> offspring = this.crossoverOperator.execute(parents);

                this.mutationOperator.execute(offspring.get(0));
                this.mutationOperator.execute(offspring.get(1));

                offspringPopulation.add(offspring.get(0));
                offspringPopulation.add(offspring.get(1));
            }

            this.evaluatePopulation(offspringPopulation);

            List<S> jointPopulation = new ArrayList<>();
            jointPopulation.addAll(this.population);
            jointPopulation.addAll(offspringPopulation);

            Ranking<S> ranking = this.computeRanking(jointPopulation);

            this.population = this.crowdingDistanceSelection(ranking);

            this.evaluations += this.populationSize;
            this.eaLogger.logPopulation(this.population, this.evaluations / this.populationSize);
        }
    }

    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.stoppingRules) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                this.eaLogger.logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }

    @Override
    public List<S> getResult() {
        return this.getNonDominatedSolutions(this.population);
    }

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    protected List<S> createInitialPopulation() {
        List<S> population = new ArrayList<>(this.populationSize);
        for (int i = 0; i < this.populationSize; i++) {
            S newIndividual = this.problem.createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    protected List<S> evaluatePopulation(List<S> population) {
        return this.evaluator.evaluate(population, this.problem);
    }

    protected Ranking<S> computeRanking(List<S> solutionList) {
        Ranking<S> ranking = new DominanceRanking<>();
        ranking.computeRanking(solutionList);

        return ranking;
    }

    protected List<S> crowdingDistanceSelection(Ranking<S> ranking) {
        CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();
        List<S> population = new ArrayList<>(this.populationSize);
        int rankingIndex = 0;
        while (this.populationIsNotFull(population)) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                crowdingDistance.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }

        return population;
    }

    protected boolean populationIsNotFull(List<S> population) {
        return population.size() < this.populationSize;
    }

    protected boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int rank, List<S> population) {
        return ranking.getSubfront(rank).size() < (this.populationSize - population.size());
    }

    protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
        List<S> front;

        front = ranking.getSubfront(rank);

        population.addAll(front);
    }

    protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
        List<S> currentRankedFront = ranking.getSubfront(rank);

        currentRankedFront.sort(new CrowdingDistanceComparator<>());

        int i = 0;
        while (population.size() < this.populationSize) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }

    protected List<S> getNonDominatedSolutions(List<S> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

    @Override
    public void setEALogger(IEALogger eaLogger) {
        this.eaLogger = eaLogger;
    }

    @Override
    public IEALogger getEALogger() {
        return this.eaLogger;
    }

    @Override
    public String getName() {
        return "NSGAII45";
    }

    @Override
    public String getDescription() {
        return "Nondominated Sorting Genetic Algorithm version II. Version not using the AbstractGeneticAlgorithm template";
    }
}
