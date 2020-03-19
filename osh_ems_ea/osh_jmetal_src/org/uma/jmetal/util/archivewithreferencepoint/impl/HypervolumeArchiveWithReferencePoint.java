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

package org.uma.jmetal.util.archivewithreferencepoint.impl;

import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archivewithreferencepoint.ArchiveWithReferencePoint;
import org.uma.jmetal.util.comparator.HypervolumeContributionComparator;

import java.util.Comparator;
import java.util.List;


/**
 * Class representing a {@link ArchiveWithReferencePoint} archive using a hypervolume contribution based
 * density estimator.
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class HypervolumeArchiveWithReferencePoint<S extends Solution<?>> extends ArchiveWithReferencePoint<S> {
    private final Hypervolume<S> hypervolume;

    public HypervolumeArchiveWithReferencePoint(int maxSize, List<Double> refPointDM) {
        super(maxSize, refPointDM, new HypervolumeContributionComparator<>());

        this.hypervolume = new PISAHypervolume<>();
    }

    @Override
    public Comparator<S> getComparator() {
        return this.comparator;
    }

    @Override
    public void computeDensityEstimator() {
        if (this.archive.size() > 3) {
            this.hypervolume
                    .computeHypervolumeContribution(this.archive.getSolutionList(), this.archive.getSolutionList());
        }
    }

    @Override
    public void sortByDensityEstimator() {
        this.getSolutionList().sort(new HypervolumeContributionComparator<>());
    }
}
