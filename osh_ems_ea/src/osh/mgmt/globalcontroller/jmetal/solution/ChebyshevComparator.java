package osh.mgmt.globalcontroller.jmetal.solution;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.util.Comparator;
import java.util.stream.IntStream;

/**
 * Represents a {@link Comparator} for jMetal {@link Solution} based on a chebyshev comparison.
 *
 * @author Sebastian Kramer
 */
public class ChebyshevComparator<S extends Solution<?>> implements Comparator<S> {

    private final double[] weights;
    private final int objectiveCount;

    /**
     * Constructs this comparator with the given objective weights.
     *
     * @param weights
     */
    public ChebyshevComparator(double[] weights) {
        this.objectiveCount = weights.length;
        this.weights = weights;
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
            double objective1 = IntStream.range(0, this.objectiveCount).mapToDouble(i -> solution1.getObjective(i) *
                    this.weights[i]).max().orElse(Double.MAX_VALUE);
            double objective2 = IntStream.range(0, this.objectiveCount).mapToDouble(i -> solution2.getObjective(i)  *
                    this.weights[i]).max().orElse(Double.MAX_VALUE);

            result = Double.compare(objective1, objective2);
        }
        return result;
    }
}
