//  AdaptiveGridArchive.java
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
import jmetal.util.AdaptiveGrid;
import jmetal.util.comparators.DominanceComparator;

import java.util.Comparator;
import java.util.Iterator;

/**
 * This class implements an archive based on an adaptive grid used in PAES
 */
public class AdaptiveGridArchive extends Archive {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores the adaptive grid
     */
    private final AdaptiveGrid grid_;

    /**
     * Stores the maximum size of the archive
     */
    private final int maxSize_;

    /**
     * Stores a <code>Comparator</code> for dominance checking
     */
    @SuppressWarnings("rawtypes")
    private final Comparator dominance_;

    /**
     * Constructor.
     *
     * @param maxSize    The maximum size of the archive
     * @param bisections The maximum number of bi-divisions for the adaptive
     *                   grid.
     * @param objectives The number of objectives.
     */
    public AdaptiveGridArchive(int maxSize, int bisections, int objectives) {
        super(maxSize);
        this.maxSize_ = maxSize;
        this.dominance_ = new DominanceComparator();
        this.grid_ = new AdaptiveGrid(bisections, objectives);
    } // AdaptiveGridArchive

    /**
     * Adds a <code>Solution</code> to the archive. If the <code>Solution</code>
     * is dominated by any member of the archive then it is discarded. If the
     * <code>Solution</code> dominates some members of the archive, these are
     * removed. If the archive is full and the <code>Solution</code> has to be
     * inserted, one <code>Solution</code> of the most populated hypercube of the
     * adaptive grid is removed.
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean add(Solution solution) {
        //Iterator of individuals over the list
        Iterator<Solution> iterator = this.solutionsList_.iterator();

        while (iterator.hasNext()) {
            Solution element = iterator.next();
            int flag = this.dominance_.compare(solution, element);
            if (flag < 0) { // The Individual to insert dominates other
                // individuals in  the archive
                iterator.remove(); //Delete it from the archive
                int location = this.grid_.location(element);
                if (this.grid_.getLocationDensity(location) > 1) {//The hypercube contains
                    this.grid_.removeSolution(location);            //more than one individual
                } else {
                    this.grid_.updateGrid(this);
                } // else
            } // if
            else if (flag > 0) { // An Individual into the file dominates the
                // solution to insert
                return false; // The solution will not be inserted
            } // else if
        } // while

        // At this point, the solution may be inserted
        if (this.size() == 0) { //The archive is empty
            this.solutionsList_.add(solution);
            this.grid_.updateGrid(this);
            return true;
        } //

        if (this.size() < this.maxSize_) { //The archive is not full
            this.grid_.updateGrid(solution, this); // Update the grid if applicable
            int location;
            location = this.grid_.location(solution); // Get the location of the solution
            this.grid_.addSolution(location); // Increment the density of the hypercube
            this.solutionsList_.add(solution); // Add the solution to the list
            return true;
        } // if

        // At this point, the solution has to be inserted and the archive is full
        this.grid_.updateGrid(solution, this);
        int location = this.grid_.location(solution);
        if (location == this.grid_.getMostPopulated()) { // The solution is in the
            // most populated hypercube
            return false; // Not inserted
        } else {
            // Remove an solution from most populated area
            iterator = this.solutionsList_.iterator();
            boolean removed = false;
            while (iterator.hasNext()) {
                if (!removed) {
                    Solution element = iterator.next();
                    int location2 = this.grid_.location(element);
                    if (location2 == this.grid_.getMostPopulated()) {
                        iterator.remove();
                        this.grid_.removeSolution(location2);
                        removed = true;
                    } // if
                } // if
            } // while
            // A solution from most populated hypercube has been removed,
            // insert now the solution
            this.grid_.addSolution(location);
            this.solutionsList_.add(solution);
        } // else
        return true;
    } // add

    /**
     * Returns the AdaptativeGrid used
     *
     * @return the AdaptativeGrid
     */
    public AdaptiveGrid getGrid() {
        return this.grid_;
    } // AdaptativeGrid
} // AdaptativeGridArchive
