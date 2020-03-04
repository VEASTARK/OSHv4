package org.uma.jmetal.operator.impl.selection;

import org.apache.commons.collections4.CollectionUtils;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DirScoreComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DirScore;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * created at 11:47 am, 2019/1/29
 * Used for DIR-enhanced NSGA-II (D-NSGA-II) to select the joint solutions for next iteration
 * this code implemented according to
 * "Cai X, Sun H, Fan Z. A diversity indicator based on reference vectors for many-objective optimization[J]. Information Sciences, 2018, 430-431:467-486."
 *
 * @author sunhaoran <nuaa_sunhr@yeah.net>
 */
@SuppressWarnings("serial")
public class RankingAndDirScoreSelection<S extends Solution<?>>
        extends RankingAndCrowdingSelection<S> {

    private final int solutionsToSelect;
    private final Comparator<S> dominanceComparator;
    private final double[][] referenceVectors;

    public RankingAndDirScoreSelection(int solutionsToSelect, Comparator<S> dominanceComparator, double[][] referenceVectors) {
        super(solutionsToSelect, dominanceComparator);
        this.solutionsToSelect = solutionsToSelect;
        this.dominanceComparator = dominanceComparator;
        this.referenceVectors = referenceVectors;
    }

    @Override
    public List<S> execute(List<S> solutionSet) {
        if (this.referenceVectors == null || this.referenceVectors.length == 0) {
            throw new JMetalException("reference vectors can not be null.");
        }
        if (CollectionUtils.isEmpty(solutionSet)) {
            throw new JMetalException("solution set can not be null");
        }
        Ranking<S> ranking = new DominanceRanking<>(this.dominanceComparator);
        ranking.computeRanking(solutionSet);
        return this.dirScoreSelection(ranking);
    }

    private List<S> dirScoreSelection(Ranking<S> ranking) {
        DirScore<S> dirScore = new DirScore<>(this.referenceVectors);
        List<S> population = new ArrayList<>(this.solutionsToSelect);
        int rankingIndex = 0;
        while (population.size() < this.solutionsToSelect) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                dirScore.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                dirScore.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }
        return population;
    }

    @Override
    protected void addLastRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
        List<S> currentRankedFront = ranking.getSubfront(rank);

        currentRankedFront.sort(new DirScoreComparator<>());

        int i = 0;
        while (population.size() < this.solutionsToSelect) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }
}
