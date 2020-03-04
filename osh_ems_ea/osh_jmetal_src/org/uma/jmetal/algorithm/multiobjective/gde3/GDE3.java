package org.uma.jmetal.algorithm.multiobjective.gde3;

import org.uma.jmetal.algorithm.impl.AbstractDifferentialEvolution;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.DensityEstimator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the GDE3 algorithm
 */
@SuppressWarnings("serial")
public class GDE3 extends AbstractDifferentialEvolution<List<DoubleSolution>> {
    protected final int maxEvaluations;
    protected final Comparator<DoubleSolution> dominanceComparator;
    protected final Ranking<DoubleSolution> ranking;
    protected final DensityEstimator<DoubleSolution> crowdingDistance;
    protected final SolutionListEvaluator<DoubleSolution> evaluator;
    protected int evaluations;
    private int maxPopulationSize;

    /**
     * Constructor
     */
    public GDE3(DoubleProblem problem, int populationSize, int maxEvaluations,
                DifferentialEvolutionSelection selection, DifferentialEvolutionCrossover crossover,
                SolutionListEvaluator<DoubleSolution> evaluator) {
        this.setProblem(problem);
        this.maxPopulationSize = populationSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverOperator = crossover;
        this.selectionOperator = selection;

        this.dominanceComparator = new DominanceComparator<>();
        this.ranking = new DominanceRanking<>();
        this.crowdingDistance = new CrowdingDistance<>();

        this.evaluator = evaluator;
    }

    public int getMaxPopulationSize() {
        return this.maxPopulationSize;
    }

    public void setMaxPopulationSize(int maxPopulationSize) {
        this.maxPopulationSize = maxPopulationSize;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.maxPopulationSize;
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.maxPopulationSize;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.evaluations >= this.maxEvaluations;
    }

    @Override
    protected List<DoubleSolution> createInitialPopulation() {
        List<DoubleSolution> population = new ArrayList<>(this.maxPopulationSize);
        for (int i = 0; i < this.maxPopulationSize; i++) {
            DoubleSolution newIndividual = this.getProblem().createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    /**
     * Evaluate population method
     *
     * @param population The list of solutions to be evaluated
     * @return A list of evaluated solutions
     */
    @Override
    protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
        return this.evaluator.evaluate(population, this.getProblem());
    }

    @Override
    protected List<DoubleSolution> selection(List<DoubleSolution> population) {
        List<DoubleSolution> matingPopulation = new LinkedList<>();
        for (int i = 0; i < this.maxPopulationSize; i++) {
            // Obtain parents. Two parameters are required: the population and the
            //                 index of the current individual
            this.selectionOperator.setIndex(i);
            List<DoubleSolution> parents = this.selectionOperator.execute(population);

            matingPopulation.addAll(parents);
        }

        return matingPopulation;
    }

    @Override
    protected List<DoubleSolution> reproduction(List<DoubleSolution> matingPopulation) {
        List<DoubleSolution> offspringPopulation = new ArrayList<>();

        for (int i = 0; i < this.maxPopulationSize; i++) {
            this.crossoverOperator.setCurrentSolution(this.getPopulation().get(i));
            List<DoubleSolution> parents = new ArrayList<>(3);
            for (int j = 0; j < 3; j++) {
                parents.add(matingPopulation.get(0));
                matingPopulation.remove(0);
            }

            this.crossoverOperator.setCurrentSolution(this.getPopulation().get(i));
            List<DoubleSolution> children = this.crossoverOperator.execute(parents);

            offspringPopulation.add(children.get(0));
        }

        return offspringPopulation;
    }

    @Override
    protected List<DoubleSolution> replacement(List<DoubleSolution> population,
                                               List<DoubleSolution> offspringPopulation) {
        List<DoubleSolution> tmpList = new ArrayList<>();
        for (int i = 0; i < this.maxPopulationSize; i++) {
            // Dominance test
            DoubleSolution child = offspringPopulation.get(i);
            int result;
            result = this.dominanceComparator.compare(population.get(i), child);
            if (result == -1) {
                // Solution i dominates child
                tmpList.add(population.get(i));
            } else if (result == 1) {
                // child dominates
                tmpList.add(child);
            } else {
                // the two solutions are non-dominated
                tmpList.add(child);
                tmpList.add(population.get(i));
            }
        }
        Ranking<DoubleSolution> ranking = this.computeRanking(tmpList);

        return this.crowdingDistanceSelection(ranking);
    }

    @Override
    public List<DoubleSolution> getResult() {
        return this.getNonDominatedSolutions(this.getPopulation());
    }


    protected Ranking<DoubleSolution> computeRanking(List<DoubleSolution> solutionList) {
        Ranking<DoubleSolution> ranking = new DominanceRanking<>();
        ranking.computeRanking(solutionList);

        return ranking;
    }

    protected List<DoubleSolution> crowdingDistanceSelection(Ranking<DoubleSolution> ranking) {
        CrowdingDistance<DoubleSolution> crowdingDistance = new CrowdingDistance<>();
        List<DoubleSolution> population = new ArrayList<>(this.maxPopulationSize);
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

    protected boolean populationIsNotFull(List<DoubleSolution> population) {
        return population.size() < this.maxPopulationSize;
    }

    protected boolean subfrontFillsIntoThePopulation(Ranking<DoubleSolution> ranking, int rank,
                                                     List<DoubleSolution> population) {
        return ranking.getSubfront(rank).size() < (this.maxPopulationSize - population.size());
    }

    protected void addRankedSolutionsToPopulation(Ranking<DoubleSolution> ranking, int rank,
                                                  List<DoubleSolution> population) {
        List<DoubleSolution> front;

        front = ranking.getSubfront(rank);

        population.addAll(front);
    }

    protected void addLastRankedSolutionsToPopulation(Ranking<DoubleSolution> ranking, int rank,
                                                      List<DoubleSolution> population) {
        List<DoubleSolution> currentRankedFront = ranking.getSubfront(rank);

        currentRankedFront.sort(new CrowdingDistanceComparator<>());

        int i = 0;
        while (population.size() < this.maxPopulationSize) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }

    protected List<DoubleSolution> getNonDominatedSolutions(List<DoubleSolution> solutionList) {
        return SolutionListUtils.getNondominatedSolutions(solutionList);
    }

    @Override
    public String getName() {
        return "GDE3";
    }

    @Override
    public String getDescription() {
        return "Generalized Differential Evolution version 3";
    }
} 
