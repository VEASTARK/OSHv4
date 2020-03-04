package org.uma.jmetal.util.archive.impl;

import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.HypervolumeContributionComparator;

import java.util.Comparator;

/**
 * Created by Antonio J. Nebro on 24/09/14.
 */
@SuppressWarnings("serial")
public class HypervolumeArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
    final Hypervolume<S> hypervolume;
    private final Comparator<S> comparator;

    public HypervolumeArchive(int maxSize, Hypervolume<S> hypervolume) {
        super(maxSize);
        this.comparator = new HypervolumeContributionComparator<>();
        this.hypervolume = hypervolume;
    }

    @Override
    public void prune() {
        if (this.getSolutionList().size() > this.getMaxSize()) {
            this.computeDensityEstimator();
            S worst = new SolutionListUtils().findWorstSolution(this.getSolutionList(), this.comparator);
            this.getSolutionList().remove(worst);
        }
    }

    @Override
    public Comparator<S> getComparator() {
        return this.comparator;
    }

    @Override
    public void computeDensityEstimator() {
        this.hypervolume.computeHypervolumeContribution(this.archive.getSolutionList(), this.archive.getSolutionList());
    }

    @Override
    public void sortByDensityEstimator() {
        this.getSolutionList().sort(new HypervolumeContributionComparator<>());
    }
}
