package org.uma.jmetal.algorithm.multiobjective.mombi;

import org.uma.jmetal.algorithm.multiobjective.mombi.util.*;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MOMBI<S extends Solution<?>> extends AbstractMOMBI<S> {

    protected final AbstractUtilityFunctionsSet<S> utilityFunctions;

    public MOMBI(Problem<S> problem,
                 CrossoverOperator<S> crossover,
                 MutationOperator<S> mutation,
                 SelectionOperator<List<S>, S> selection,
                 SolutionListEvaluator<S> evaluator,
                 String pathWeights,
                 IEALogger eaLogger) {
        super(problem, crossover, mutation, selection, evaluator, eaLogger);
        this.utilityFunctions = this.createUtilityFunction(pathWeights);
    }

    public AbstractUtilityFunctionsSet<S> createUtilityFunction(String pathWeights) {
        return new TchebycheffUtilityFunctionsSet<>(pathWeights, this.getReferencePoint());
    }

    public int getMaxPopulationSize() {
        return this.utilityFunctions.getSize();
    }

    @Override
    public void specificMOEAComputations() {
        this.updateNadirPoint(this.getPopulation());
        this.updateReferencePoint(this.getPopulation());
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);

        R2Ranking<S> ranking = this.computeRanking(jointPopulation);
        return this.selectBest(ranking);
    }

    protected R2Ranking<S> computeRanking(List<S> solutionList) {
        R2Ranking<S> ranking = new R2Ranking<>(this.utilityFunctions);
        ranking.computeRanking(solutionList);

        return ranking;
    }

    protected void addRankedSolutionsToPopulation(R2Ranking<S> ranking, int index, List<S> population) {
        population.addAll(ranking.getSubfront(index));
    }

    protected void addLastRankedSolutionsToPopulation(R2Ranking<S> ranking, int index, List<S> population) {
        List<S> front = ranking.getSubfront(index);
        front.sort((arg0, arg1) -> {
            R2RankingAttribute<S> attribute = new R2RankingAttribute<>();
            R2SolutionData dataFirst = attribute.getAttribute(arg0);
            R2SolutionData dataSecond = attribute.getAttribute(arg1);
            return Double.compare(dataSecond.utility, dataFirst.utility);
        });
        int remain = this.getMaxPopulationSize() - population.size();
        population.addAll(front.subList(0, remain));
    }

    protected List<S> selectBest(R2Ranking<S> ranking) {
        List<S> population = new ArrayList<>(this.getMaxPopulationSize());
        int rankingIndex = 0;

        while (this.populationIsNotFull(population)) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }
        return population;
    }

    private boolean subfrontFillsIntoThePopulation(R2Ranking<S> ranking, int index, List<S> population) {
        return (population.size() + ranking.getSubfront(index).size() < this.getMaxPopulationSize());
    }

    protected AbstractUtilityFunctionsSet<S> getUtilityFunctions() {
        return this.utilityFunctions;
    }

    @Override
    public String getName() {
        return "MOMBI";
    }

    @Override
    public String getDescription() {
        return "Many-Objective Metaheuristic Based on the R2 Indicator";
    }
}
