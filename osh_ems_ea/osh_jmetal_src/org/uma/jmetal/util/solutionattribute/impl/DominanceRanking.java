package org.uma.jmetal.util.solutionattribute.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;
import org.uma.jmetal.util.solutionattribute.Ranking;

import java.util.*;

/**
 * This class implements some facilities for ranking set of solutions.
 * Given a collection of solutions, they are ranked
 * according to scheme proposed in NSGA-II; as an output, a set of subsets
 * are obtained. The subsets are numbered starting from 0 (in NSGA-II, the
 * numbering starts from 1); thus, subset 0 contains the non-dominated
 * solutions, subset 1 contains the non-dominated solutions after removing those
 * belonging to subset 0, and so on.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class DominanceRanking<S extends Solution<?>>
        extends GenericSolutionAttribute<S, Integer> implements Ranking<S> {

    private static final Comparator<Solution<?>> CONSTRAINT_VIOLATION_COMPARATOR =
            new OverallConstraintViolationComparator<>();
    private Comparator<S> dominanceComparator;
    private List<ArrayList<S>> rankedSubPopulations;

    /**
     * Constructor
     */
    public DominanceRanking(Comparator<S> comparator) {
        this.dominanceComparator = comparator;
        this.rankedSubPopulations = new ArrayList<>();
    }

    /**
     * Constructor
     */
    public DominanceRanking() {
        this(new DominanceComparator<>());
    }

    public DominanceRanking(Object id) {
        super(id);
        this.rankedSubPopulations = new ArrayList<>();
    }

    @Override
    public Ranking<S> computeRanking(List<S> solutionSet) {

        // dominateMe[i] contains the number of solutions dominating i
        int[] dominateMe = new int[solutionSet.size()];

        // iDominate[k] contains the list of solutions dominated by k
        List<List<Integer>> iDominate = new ArrayList<>(solutionSet.size());

        // front[i] contains the list of individuals belonging to the front i
        ArrayList<List<Integer>> front = new ArrayList<>(solutionSet.size() + 1);

        // Initialize the fronts
        for (int i = 0; i < solutionSet.size() + 1; i++) {
            front.add(new LinkedList<>());
        }

        // Fast non dominated sorting algorithm
        // Contribution of Guillaume Jacquenot
        for (int p = 0; p < solutionSet.size(); p++) {
            // Initialize the list of individuals that i dominate and the number
            // of individuals that dominate me
            iDominate.add(new LinkedList<>());
            dominateMe[p] = 0;
        }

        int flagDominate;
        for (int p = 0; p < (solutionSet.size() - 1); p++) {
            // For all q individuals , calculate if p dominates q or vice versa
            for (int q = p + 1; q < solutionSet.size(); q++) {
                flagDominate =
                        CONSTRAINT_VIOLATION_COMPARATOR.compare(solutionSet.get(p), solutionSet.get(q));
                if (flagDominate == 0) {
                    flagDominate = this.dominanceComparator.compare(solutionSet.get(p), solutionSet.get(q));
                }
                if (flagDominate == -1) {
                    iDominate.get(p).add(q);
                    dominateMe[q]++;
                } else if (flagDominate == 1) {
                    iDominate.get(q).add(p);
                    dominateMe[p]++;
                }
            }
        }

        for (int i = 0; i < solutionSet.size(); i++) {
            if (dominateMe[i] == 0) {
                front.get(0).add(i);
                solutionSet.get(i).setAttribute(this.getAttributeIdentifier(), 0);
            }
        }

        //Obtain the rest of fronts
        int i = 0;
        Iterator<Integer> it1, it2; // Iterators
        while (!front.get(i).isEmpty()) {
            i++;
            it1 = front.get(i - 1).iterator();
            while (it1.hasNext()) {
                it2 = iDominate.get(it1.next()).iterator();
                while (it2.hasNext()) {
                    int index = it2.next();
                    dominateMe[index]--;
                    if (dominateMe[index] == 0) {
                        front.get(i).add(index);
                        //RankingAndCrowdingAttr.getAttributes(solutionSet.get(index)).setRank(i);
                        solutionSet.get(index).setAttribute(this.getAttributeIdentifier(), i);
                    }
                }
            }
        }

        this.rankedSubPopulations = new ArrayList<>();
        //0,1,2,....,i-1 are fronts, then i fronts
        for (int j = 0; j < i; j++) {
            this.rankedSubPopulations.add(j, new ArrayList<>(front.get(j).size()));
            it1 = front.get(j).iterator();
            while (it1.hasNext()) {
                this.rankedSubPopulations.get(j).add(solutionSet.get(it1.next()));
            }
        }

        return this;
    }

    @Override
    public List<S> getSubfront(int rank) {
        if (rank >= this.rankedSubPopulations.size()) {
            throw new JMetalException("Invalid rank: " + rank + ". Max rank = " + (this.rankedSubPopulations.size() - 1));
        }
        return this.rankedSubPopulations.get(rank);
    }

    @Override
    public int getNumberOfSubfronts() {
        return this.rankedSubPopulations.size();
    }
}
