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

package org.uma.jmetal.util.archive.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.SpatialSpreadDeviationComparator;
import org.uma.jmetal.util.solutionattribute.DensityEstimator;
import org.uma.jmetal.util.solutionattribute.impl.SpatialSpreadDeviation;

import java.util.Comparator;

/**
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
public class SpatialSpreadDeviationArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
    private static final long serialVersionUID = -1589179581147401214L;
    private final Comparator<S> crowdingDistanceComparator;
    private final DensityEstimator<S> crowdingDistance;

    public SpatialSpreadDeviationArchive(int maxSize) {
        super(maxSize);
        this.crowdingDistanceComparator = new SpatialSpreadDeviationComparator<>();
        this.crowdingDistance = new SpatialSpreadDeviation<>();
    }

    @Override
    public void prune() {
        if (this.getSolutionList().size() > this.getMaxSize()) {
            this.computeDensityEstimator();
            S worst = new SolutionListUtils().findWorstSolution(this.getSolutionList(), this.crowdingDistanceComparator);
            this.getSolutionList().remove(worst);
        }
    }

    @Override
    public void sortByDensityEstimator() {
        this.getSolutionList().sort(this.crowdingDistanceComparator);
    }

    @Override
    public Comparator<S> getComparator() {
        return this.crowdingDistanceComparator;
    }

    @Override
    public void computeDensityEstimator() {
        this.crowdingDistance.computeDensityEstimator(this.getSolutionList());
    }
}
