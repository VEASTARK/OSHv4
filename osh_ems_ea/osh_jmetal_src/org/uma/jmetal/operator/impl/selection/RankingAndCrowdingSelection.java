package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements a selection for selecting a number of solutions from
 * a solution list. The solutions are taken by mean of its ranking and
 * crowding distance values.
 *
 * @author Antonio J. Nebro, Juan J. Durillo
 */
@SuppressWarnings("serial")
public class RankingAndCrowdingSelection<S extends Solution<?>>
        implements SelectionOperator<List<S>, List<S>> {
    private final int solutionsToSelect;
    private final Comparator<S> dominanceComparator;


    /**
     * Constructor
     */
    public RankingAndCrowdingSelection(int solutionsToSelect, Comparator<S> dominanceComparator) {
        this.dominanceComparator = dominanceComparator;
        this.solutionsToSelect = solutionsToSelect;
    }

    /**
     * Constructor
     */
    public RankingAndCrowdingSelection(int solutionsToSelect) {
        this(solutionsToSelect, new DominanceComparator<>());
    }

    /* Getter */
    public int getNumberOfSolutionsToSelect() {
        return this.solutionsToSelect;
    }

    /**
     * Execute() method
     */
    public List<S> execute(List<S> solutionList) throws JMetalException {
        if (null == solutionList) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionList.size() < this.solutionsToSelect) {
            throw new JMetalException("The population size (" + solutionList.size() + ") is smaller than" +
                    "the solutions to selected (" + this.solutionsToSelect + ")");
        }

        Ranking<S> ranking = new DominanceRanking<>(this.dominanceComparator);
        ranking.computeRanking(solutionList);

        return this.crowdingDistanceSelection(ranking);
    }

    protected List<S> crowdingDistanceSelection(Ranking<S> ranking) {
        CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();
        List<S> population = new ArrayList<>(this.solutionsToSelect);
        int rankingIndex = 0;
        while (population.size() < this.solutionsToSelect) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                crowdingDistance.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                crowdingDistance.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }

        return population;
    }

    protected boolean subfrontFillsIntoThePopulation(Ranking<S> ranking, int rank, List<S> population) {
        return ranking.getSubfront(rank).size() < (this.solutionsToSelect - population.size());
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
        while (population.size() < this.solutionsToSelect) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }
}
