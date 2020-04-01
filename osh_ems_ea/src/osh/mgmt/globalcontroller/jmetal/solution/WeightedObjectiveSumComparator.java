package osh.mgmt.globalcontroller.jmetal.solution;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.util.Comparator;

/**
 * Represents a {@link Comparator} for jMetal {@link Solution} based on the weighted sum of the solution objectives.
 *
 * @author Sebastian Kramer
 */
public class WeightedObjectiveSumComparator<S extends Solution<?>> implements Comparator<S> {

    private final double[] weights;
    private final int objectiveCount;

    /**
     * Constructs this comparator with the given objective weights.
     *
     * @param weights
     */
    public WeightedObjectiveSumComparator(double[] weights) {
        this.weights = weights;
        this.objectiveCount = weights.length;
    }

    @Override
    public int compare(S solution1, S solution2) {
        int result;
        if (solution1 == null) {
            if (solution2 == null) {
                result = 0;
            } else {
                result = 1;
            }
        } else if (solution2 == null) {
            result = -1;
        } else if (solution1.getNumberOfObjectives() < this.objectiveCount) {
            throw new JMetalException("The solution1 has " + solution1.getNumberOfObjectives() + " objectives "
                    + "and the total objectives are " + this.objectiveCount);
        } else if (solution2.getNumberOfObjectives() < this.objectiveCount) {
            throw new JMetalException("The solution2 has " + solution2.getNumberOfObjectives() + " objectives "
                    + "and the total objectives are " + this.objectiveCount);
        } else {
            double objective1 = 0.0, objective2 = 0.0;
            for (int i = 0; i < this.objectiveCount; i++) {
                objective1 += this.weights[i] * solution1.getObjective(i);
                objective2 += this.weights[i] * solution2.getObjective(i);
            }

            result = Double.compare(objective1, objective2);
        }
        return result;
    }
}
