//  HypervolumeArchive.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2013 Antonio J. Nebro
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.util.archive;

import jmetal.core.Solution;
import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.Distance;
import jmetal.util.comparators.CrowdingDistanceComparator;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.EqualSolutions;

import java.util.Comparator;

/**
 * This class implements a bounded archive based on crowding distances (as
 * defined in NSGA-II).
 */
@SuppressWarnings({"rawtypes", "unused", "unchecked"})
public class HypervolumeArchive extends Archive {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the maximum size of the archive.
     */
    private final int maxSize_;

    /**
     * stores the number of the objectives.
     */
    private final int objectives_;

    /**
     * Stores a <code>Comparator</code> for dominance checking.
     */
    private final Comparator dominance_;

    /**
     * Stores a <code>Comparator</code> for equality checking (in the objective
     * space).
     */
    private final Comparator equals_;

    /**
     * Stores a <code>Distance</code> object, for distances utilities
     */
    private final Distance distance_;

    private final MetricsUtil utils_;

    private final double offset_;
    private final Comparator crowdingDistance_;

    /**
     * Constructor.
     *
     * @param maxSize            The maximum size of the archive.
     * @param numberOfObjectives The number of objectives.
     */
    public HypervolumeArchive(int maxSize, int numberOfObjectives) {
        super(maxSize);
        this.maxSize_ = maxSize;
        this.objectives_ = numberOfObjectives;
        this.dominance_ = new DominanceComparator();
        this.equals_ = new EqualSolutions();
        this.distance_ = new Distance();
        this.utils_ = new MetricsUtil();
        this.offset_ = 100;
        this.crowdingDistance_ = new CrowdingDistanceComparator();

    } // CrowdingArchive


    /**
     * Adds a <code>Solution</code> to the archive. If the <code>Solution</code>
     * is dominated by any member of the archive, then it is discarded. If the
     * <code>Solution</code> dominates some members of the archive, these are
     * removed. If the archive is full and the <code>Solution</code> has to be
     * inserted, the solutions are sorted by crowding distance and the one having
     * the minimum crowding distance value.
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    public boolean add(Solution solution) {
        int flag;
        int i = 0;
        Solution aux; //Store an solution temporally

        while (i < this.solutionsList_.size()) {
            aux = this.solutionsList_.get(i);

            flag = this.dominance_.compare(solution, aux);
            if (flag == 1) {               // The solution to add is dominated
                return false;                // Discard the new solution
            } else if (flag == -1) {       // A solution in the archive is dominated
                this.solutionsList_.remove(i);    // Remove it from the population
            } else {
                if (this.equals_.compare(aux, solution) == 0) { // There is an equal solution
                    // in the population
                    return false; // Discard the new solution
                }  // if
                i++;
            }
        }
        // Insert the solution into the archive
        this.solutionsList_.add(solution);
        if (this.size() > this.maxSize_) { // The archive is full
            double[][] frontValues = this.writeObjectivesToMatrix();
            int numberOfObjectives = this.objectives_;
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            double[] maximumValues = this.utils_.getMaximumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            double[] minimumValues = this.utils_.getMinimumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            // STEP 2. Get the normalized front
            double[][] normalizedFront = this.utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
            // compute offsets for reference point in normalized space
            double[] offsets = new double[maximumValues.length];
            for (i = 0; i < maximumValues.length; i++) {
                offsets[i] = this.offset_ / (maximumValues[i] - minimumValues[i]);
            }
            // STEP 3. Inverse the pareto front. This is needed because the original
            //metric by Zitzler is for maximization problems
            double[][] invertedFront = this.utils_.invertedFront(normalizedFront);
            // shift away from origin, so that boundary points also get a contribution > 0
            for (double[] point : invertedFront) {
                for (i = 0; i < point.length; i++) {
                    point[i] += offsets[i];
                }
            }

            // calculate contributions and sort
            double[] contributions = this.utils_.hvContributions(this.objectives_, invertedFront);
            for (i = 0; i < contributions.length; i++) {
                // contribution values are used analogously to crowding distance
                this.get(i).setCrowdingDistance(contributions[i]);
            }

            this.sort(new CrowdingDistanceComparator());

            //remove(indexWorst(crowdingDistance_));
            this.remove(this.size() - 1);
        }
        return true;
    } // add


    /**
     * This method forces to compute the contribution of each solution (required for PAEShv)
     */
    public void actualiseHVContribution() {
        if (this.size() > 2) { // The contribution can be updated
            double[][] frontValues = this.writeObjectivesToMatrix();
            int numberOfObjectives = this.objectives_;
            // STEP 1. Obtain the maximum and minimum values of the Pareto front
            double[] maximumValues = this.utils_.getMaximumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            double[] minimumValues = this.utils_.getMinimumValues(this.writeObjectivesToMatrix(), numberOfObjectives);
            // STEP 2. Get the normalized front
            double[][] normalizedFront = this.utils_.getNormalizedFront(frontValues, maximumValues, minimumValues);
            // compute offsets for reference point in normalized space
            double[] offsets = new double[maximumValues.length];
            for (int i = 0; i < maximumValues.length; i++) {
                offsets[i] = this.offset_ / (maximumValues[i] - minimumValues[i]);
            }
            // STEP 3. Inverse the pareto front. This is needed because the original
            //metric by Zitzler is for maximization problems
            double[][] invertedFront = this.utils_.invertedFront(normalizedFront);
            // shift away from origin, so that boundary points also get a contribution > 0
            for (double[] point : invertedFront) {
                for (int i = 0; i < point.length; i++) {
                    point[i] += offsets[i];
                }
            }

            // calculate contributions and sort
            double[] contributions = this.utils_.hvContributions(this.objectives_, invertedFront);
            for (int i = 0; i < contributions.length; i++) {
                // contribution values are used analogously to crowding distance
                this.get(i).setCrowdingDistance(contributions[i]);
            }
        }
    } // computeHVContribution


    /**
     * This method returns the location (integer position) of a solution in the archive.
     * For that, the equals_ comparator is used
     */
    public int getLocation(Solution solution) {
        int index = 0;
        while (index < this.size()) {
            if (this.equals_.compare(solution, this.get(index)) == 0) {
                return index;
            }
            index++;
        }
        return -1;
    }

} // HypervolumeArchive
