//  StrenghtRawFitnessArchive.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
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
import jmetal.util.Spea2Fitness;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.EqualSolutions;
import jmetal.util.comparators.FitnessComparator;

import java.util.Comparator;

/**
 * This class implements a bounded archive based on strength raw fitness (as
 * defined in SPEA2).
 */
@SuppressWarnings("rawtypes")
public class StrengthRawFitnessArchive extends Archive {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the maximum size of the archive.
     */
    private final int maxSize_;

    /**
     * Stores a <code>Comparator</code> for dominance checking.
     */
    private final Comparator dominance_;

    /**
     * Stores a <code>Comparator</code> for fitness checking.
     */
    private final Comparator fitnessComparator_;

    /**
     * Stores a <code>Comparator</code> for equality checking (in the objective
     * space).
     */
    private final Comparator equals_;

    /**
     * Constructor.
     *
     * @param maxSize The maximum size of the archive.
     */
    public StrengthRawFitnessArchive(int maxSize) {
        super(maxSize);
        this.maxSize_ = maxSize;
        this.dominance_ = new DominanceComparator();
        this.equals_ = new EqualSolutions();
        this.fitnessComparator_ = new FitnessComparator();
    } // StrengthRawFitnessArchive

    /**
     * Adds a <code>Solution</code> to the archive. If the <code>Solution</code>
     * is dominated by any member of the archive then it is discarded. If the
     * <code>Solution</code> dominates some members of the archive, these are
     * removed. If the archive is full and the <code>Solution</code> has to be
     * inserted, all the solutions are ordered by his strengthRawFitness value and
     * the one having the worst value is removed.
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean add(Solution solution) {
        int flag;
        int i = 0;
        Solution aux;
        while (i < this.solutionsList_.size()) {
            aux = this.solutionsList_.get(i);
            flag = this.dominance_.compare(solution, aux);
            if (flag == 1) {                // The solution to add is dominated
                return false;                 // Discard the new solution
            } else if (flag == -1) {        // A solution in the archive is dominated
                this.solutionsList_.remove(i);     // Remove the dominated solution
            } else {
                if (this.equals_.compare(aux, solution) == 0) {
                    return false;
                }
                i++;
            }
        }
        // Insert the solution in the archive
        this.solutionsList_.add(solution);

        if (this.size() > this.maxSize_) { // The archive is full
            (new Spea2Fitness(this)).fitnessAssign();
            //Remove the last
            this.remove(this.indexWorst(this.fitnessComparator_));
        }
        return true;
    } // add
} //StrengthRawFitnessArchive
