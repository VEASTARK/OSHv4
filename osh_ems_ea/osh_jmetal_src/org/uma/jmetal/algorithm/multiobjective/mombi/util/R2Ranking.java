package org.uma.jmetal.algorithm.multiobjective.mombi.util;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.*;

@SuppressWarnings("serial")
public class R2Ranking<S extends Solution<?>> extends GenericSolutionAttribute<S, R2SolutionData> {

    private final AbstractUtilityFunctionsSet<S> utilityFunctions;
    private final R2RankingAttribute<S> attribute = new R2RankingAttribute<>();
    private List<List<S>> rankedSubpopulations;
    private int numberOfRanks;


    public R2Ranking(AbstractUtilityFunctionsSet<S> utilityFunctions) {
        this.utilityFunctions = utilityFunctions;
    }

    public R2Ranking<S> computeRanking(List<S> population) {

        for (S solution : population) {
            solution.setAttribute(this.getAttributeIdentifier(), new R2SolutionData());
        }

        for (int i = 0; i < this.utilityFunctions.getSize(); i++) {
            for (S solution : population) {
                R2SolutionData solutionData = this.getAttribute(solution);
                solutionData.alpha = this.utilityFunctions.evaluate(solution, i);

                if (solutionData.alpha < solutionData.utility)
                    solutionData.utility = solutionData.alpha;
            }

            population.sort((o1, o2) -> {
                R2RankingAttribute<S> attribute = new R2RankingAttribute<>();
                R2SolutionData data1 = attribute.getAttribute(o1);
                R2SolutionData data2 = attribute.getAttribute(o2);

                return Double.compare(data1.alpha, data2.alpha);
            });

            int rank = 1;
            for (S p : population) {
                R2SolutionData r2Data = this.getAttribute(p);
                if (rank < r2Data.rank) {
                    r2Data.rank = rank;
                    this.numberOfRanks = Math.max(this.numberOfRanks, rank);
                }
                rank += 1;
            }
        }

        Map<Integer, List<S>> fronts = new TreeMap<>(); // sorted on key
        for (S solution : population) {
            R2SolutionData r2Data = this.getAttribute(solution);
            fronts.computeIfAbsent(r2Data.rank, k -> new LinkedList<>());

            fronts.get(r2Data.rank).add(solution);
        }

        this.rankedSubpopulations = new ArrayList<>(fronts.size());
        this.rankedSubpopulations.addAll(fronts.values());

        return this;
    }

    public List<S> getSubfront(int rank) {
        return this.rankedSubpopulations.get(rank);
    }

    public int getNumberOfSubfronts() {
        return this.rankedSubpopulations.size();
    }

    @Override
    public void setAttribute(S solution, R2SolutionData value) {
        this.attribute.setAttribute(solution, value);
    }

    @Override
    public R2SolutionData getAttribute(S solution) {
        return this.attribute.getAttribute(solution);
    }

    @Override
    public Object getAttributeIdentifier() {
        return this.attribute.getAttributeIdentifier();
    }

    public AbstractUtilityFunctionsSet<S> getUtilityFunctions() {
        return this.utilityFunctions;
    }
}
