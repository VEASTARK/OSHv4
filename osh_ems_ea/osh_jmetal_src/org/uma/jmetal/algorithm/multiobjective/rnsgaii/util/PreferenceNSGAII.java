package org.uma.jmetal.algorithm.multiobjective.rnsgaii.util;

import org.uma.jmetal.solution.Solution;

import java.util.ArrayList;
import java.util.List;

public class PreferenceNSGAII<S extends Solution<?>> {
    private final List<Double> weights;
    private List<Double> interestPoint;
    private List<Double> upperBounds;
    private List<Double> lowerBounds;

    public PreferenceNSGAII(List<Double> weights) {
        this.weights = weights;


    }


    public void updatePointOfInterest(List<Double> newInterestPoint) {
        this.interestPoint = newInterestPoint;
    }

    public Double evaluate(S solution) {

        List<Double> objectiveValues = new ArrayList<>(solution.getNumberOfObjectives());

        for (int i = 0; i < solution.getNumberOfObjectives(); ++i) {
            objectiveValues.add(solution.getObjective(i));
        }

        double normalizeDiff;
        double distance = 0.0D;
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            if (this.upperBounds != null && this.lowerBounds != null) {
                normalizeDiff = (solution.getObjective(i) - this.interestPoint.get(i)) /
                        (this.upperBounds.get(i) - this.lowerBounds.get(i));
            } else {
                normalizeDiff = solution.getObjective(i) - this.interestPoint.get(i);
            }
            distance += this.weights.get(i) * Math.pow(normalizeDiff, 2.0D);

        }

        return Math.sqrt(distance);

    }

    public int getSize() {
        return this.weights.size();
    }

    public void setUpperBounds(List<Double> upperBounds) {
        this.upperBounds = upperBounds;
    }

    public void setLowerBounds(List<Double> lowerBounds) {
        this.lowerBounds = lowerBounds;
    }
}
