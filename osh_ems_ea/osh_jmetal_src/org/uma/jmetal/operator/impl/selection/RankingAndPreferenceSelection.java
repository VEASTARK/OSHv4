package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.util.solutionattribute.impl.PreferenceDistance;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RankingAndPreferenceSelection<S extends Solution<?>>
        implements SelectionOperator<List<S>, List<S>> {
    private final int solutionsToSelect;
    private final List<Double> interestPoint;
    private final double epsilon;

    /**
     * Constructor
     */
    public RankingAndPreferenceSelection(int solutionsToSelect, List<Double> interestPoint, double epsilon) {
        this.solutionsToSelect = solutionsToSelect;
        this.interestPoint = interestPoint;
        this.epsilon = epsilon;
    }

    /* Getter */
    public int getNumberOfSolutionsToSelect() {
        return this.solutionsToSelect;
    }

    @Override
    public List<S> execute(List<S> solutionList) {
        if (null == solutionList) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionList.size() < this.solutionsToSelect) {
            throw new JMetalException("The population size (" + solutionList.size() + ") is smaller than" +
                    "the solutions to selected (" + this.solutionsToSelect + ")");
        }

        Ranking<S> ranking = new DominanceRanking<>();
        ranking.computeRanking(solutionList);

        return this.preferenceDistanceSelection(ranking, solutionList.get(0).getNumberOfObjectives());
    }

    protected List<S> preferenceDistanceSelection(Ranking<S> ranking, int numberOfObjectives) {
        int nInteresPoint = this.interestPoint.size() / numberOfObjectives;

        List<S> population = new ArrayList<>(this.solutionsToSelect);

        while (population.size() < this.solutionsToSelect) {
            int indexPoint = 0;
            for (int n = 0; (n < nInteresPoint) && (population.size() < this.solutionsToSelect); n++) {
                List<S> auxPopulation = new ArrayList<>(this.solutionsToSelect / nInteresPoint);
                List<Double> auxInterestPoint = this.nextInterestPoint(indexPoint, numberOfObjectives);
                indexPoint += numberOfObjectives;
                PreferenceDistance<S> preferenceDistance = new PreferenceDistance<>(auxInterestPoint, this.epsilon);
                int rankingIndex = 0;
                while ((auxPopulation.size() < (this.solutionsToSelect / nInteresPoint)) && (population.size() < this.solutionsToSelect)) {
                    if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, auxPopulation)) {
                        this.addRankedSolutionsToPopulation(ranking, rankingIndex, auxPopulation);
                        rankingIndex++;
                    } else {
                        preferenceDistance.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                        this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, auxPopulation);
                    }
                }
                population.addAll(auxPopulation);
            }
        }
        PreferenceDistance<S> preferenceDistance = new PreferenceDistance<>(this.interestPoint, this.epsilon);
        population = preferenceDistance.epsilonClean(population);
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

    private List<Double> nextInterestPoint(int index, int size) {
        List<Double> result = null;
        if (index < this.interestPoint.size()) {
            result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(this.interestPoint.get(index));
                index++;
            }
        }
        return result;
    }
}
