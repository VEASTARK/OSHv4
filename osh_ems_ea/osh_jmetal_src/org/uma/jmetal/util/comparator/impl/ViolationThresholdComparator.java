package org.uma.jmetal.util.comparator.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ConstraintViolationComparator;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.List;

/**
 * This class implements the ViolationThreshold Comparator *
 *
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class ViolationThresholdComparator<S extends Solution<?>> implements
        ConstraintViolationComparator<S> {

    private final OverallConstraintViolation<S> overallConstraintViolation;
    private final NumberOfViolatedConstraints<S> numberOfViolatedConstraints;
    private double threshold;

    /**
     * Constructor
     */
    public ViolationThresholdComparator() {
        this.overallConstraintViolation = new OverallConstraintViolation<>();
        this.numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
    }

    /**
     * Compares two solutions. If the solutions has no constraints the method return 0
     *
     * @param solution1 Object representing the first <code>Solution</code>.
     * @param solution2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if o1 is less than, equal, or greater than o2,
     * respectively.
     */
    @Override
    public int compare(S solution1, S solution2) {
        if (this.overallConstraintViolation.getAttribute(solution1) == null) {
            return 0;
        }

        double overall1, overall2;
        overall1 = this.numberOfViolatedConstraints.getAttribute(solution1) *
                this.overallConstraintViolation.getAttribute(solution1);
        overall2 = this.numberOfViolatedConstraints.getAttribute(solution2) *
                this.overallConstraintViolation.getAttribute(solution2);

        if ((overall1 < 0) && (overall2 < 0)) {
            return Double.compare(overall2, overall1);
        } else if ((overall1 == 0) && (overall2 < 0)) {
            return -1;
        } else if ((overall1 < 0) && (overall2 == 0)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Returns true if solutions s1 and/or s2 have an overall constraint
     * violation with value less than 0
     */
    public boolean needToCompare(S solution1, S solution2) {
        boolean needToCompare;
        double overall1, overall2;
        overall1 = Math.abs(this.numberOfViolatedConstraints.getAttribute(solution1) *
                this.overallConstraintViolation.getAttribute(solution1));
        overall2 = Math.abs(this.numberOfViolatedConstraints.getAttribute(solution2) *
                this.overallConstraintViolation.getAttribute(solution2));

        needToCompare = (overall1 > this.threshold) || (overall2 > this.threshold);

        return needToCompare;
    }

    /**
     * Computes the feasibility ratio
     * Return the ratio of feasible solutions
     */
    public double feasibilityRatio(List<S> solutionSet) {
        double aux = 0.0;
        for (S s : solutionSet) {
            if (this.overallConstraintViolation.getAttribute(s) < 0) {
                aux += 1.0;
            }
        }
        return aux / solutionSet.size();
    }

    /**
     * Computes the feasibility ratio
     * Return the ratio of feasible solutions
     */
    public double meanOverallViolation(List<S> solutionSet) {
        double aux = 0.0;
        for (S s : solutionSet) {
            aux += Math.abs(this.numberOfViolatedConstraints.getAttribute(s) *
                    this.overallConstraintViolation.getAttribute(s));
        }
        return aux / solutionSet.size();
    }

    /**
     * Updates the threshold value using the population
     */
    public void updateThreshold(List<S> set) {
        this.threshold = this.feasibilityRatio(set) * this.meanOverallViolation(set);
    }
}
