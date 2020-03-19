package org.uma.jmetal.util.archivewithreferencepoint;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.AbstractBoundedArchive;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Comparator;
import java.util.List;

/**
 * This class defines a bounded archive that has associated a reference point as described in the paper
 * "Extending the Speed-constrained Multi-Objective PSO (SMPSO) With Reference Point Based Preference Articulation
 * Accepted in PPSN 2018.
 *
 * @param <S>
 */
@SuppressWarnings("serial")
public abstract class ArchiveWithReferencePoint<S extends Solution<?>> extends AbstractBoundedArchive<S> {
    protected final Comparator<S> comparator;
    protected List<Double> referencePoint;
    protected S referencePointSolution;

    public ArchiveWithReferencePoint(
            int maxSize,
            List<Double> referencePoint,
            Comparator<S> comparator) {
        super(maxSize);
        this.referencePoint = referencePoint;
        this.comparator = comparator;
        this.referencePointSolution = null;
    }

    @Override
    public synchronized boolean add(S solution) {
        boolean result;

        if (this.referencePointSolution == null) {
            @SuppressWarnings("unchecked")
            S copy = (S) solution.copy();
            this.referencePointSolution = copy;
            for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
                this.referencePointSolution.setObjective(i, this.referencePoint.get(i));
            }
        }

        S dominatedSolution = null;

        if (this.dominanceTest(solution, this.referencePointSolution) == 0) {
            if (this.getSolutionList().isEmpty()) {
                result = true;
            } else {
                if (JMetalRandom.getInstance().nextDouble() < 0.05) {
                    result = true;
                    dominatedSolution = solution;
                } else {
                    result = false;
                }
            }
        } else {
            result = true;
        }

        if (result) {
            result = super.add(solution);
        }

        if (result && (dominatedSolution != null) && (this.getSolutionList().size() > 1)) {
            this.getSolutionList().remove(dominatedSolution);
        }

        return result;
    }

    @Override
    public synchronized void prune() {
        if (this.getSolutionList().size() > this.getMaxSize()) {

            this.computeDensityEstimator();

            S worst = new SolutionListUtils().findWorstSolution(this.getSolutionList(), this.comparator);
            this.getSolutionList().remove(worst);
        }
    }

    public synchronized void changeReferencePoint(List<Double> newReferencePoint) {
        this.referencePoint = newReferencePoint;
        for (int i = 0; i < this.referencePoint.size(); i++) {
            this.referencePointSolution.setObjective(i, this.referencePoint.get(i));
        }

        int i = 0;
        while (i < this.getSolutionList().size()) {
            if (this.dominanceTest(this.getSolutionList().get(i), this.referencePointSolution) == 0) {
                this.getSolutionList().remove(i);
            } else {
                i++;
            }
        }

        this.referencePointSolution = null;
    }

    private int dominanceTest(S solution1, S solution2) {
        int bestIsOne = 0;
        int bestIsTwo = 0;
        int result;
        for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
            double value1 = solution1.getObjective(i);
            double value2 = solution2.getObjective(i);
            if (value1 != value2) {
                if (value1 < value2) {
                    bestIsOne = 1;
                }
                if (value2 < value1) {
                    bestIsTwo = 1;
                }
            }
        }
        result = Integer.compare(bestIsTwo, bestIsOne);
        return result;
    }
}
