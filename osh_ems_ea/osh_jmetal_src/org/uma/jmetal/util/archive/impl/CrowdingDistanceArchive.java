package org.uma.jmetal.util.archive.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.solutionattribute.DensityEstimator;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.util.Comparator;

/**
 * Created by Antonio J. Nebro on 24/09/14.
 * Modified by Juanjo on 07/04/2015
 */
@SuppressWarnings("serial")
public class CrowdingDistanceArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
    private final Comparator<S> crowdingDistanceComparator;
    private final DensityEstimator<S> crowdingDistance;

    public CrowdingDistanceArchive(int maxSize) {
        super(maxSize);
        this.crowdingDistanceComparator = new CrowdingDistanceComparator<>();
        this.crowdingDistance = new CrowdingDistance<>();
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
    public Comparator<S> getComparator() {
        return this.crowdingDistanceComparator;
    }

    @Override
    public void computeDensityEstimator() {
        this.crowdingDistance.computeDensityEstimator(this.getSolutionList());
    }

    @Override
    public void sortByDensityEstimator() {
        this.getSolutionList().sort(new CrowdingDistanceComparator<>());
    }
}
