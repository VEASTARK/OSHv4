package org.uma.jmetal.util.comparator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares two solutions according to the crowding distance attribute. The higher
 * the distance the better
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class CrowdingDistanceComparator<S extends Solution<?>> implements Comparator<S>, Serializable {
    private final CrowdingDistance<S> crowdingDistance = new CrowdingDistance<>();

    /**
     * Compare two solutions.
     *
     * @param solution1 Object representing the first <code>Solution</code>.
     * @param solution2 Object representing the second <code>Solution</code>.
     * @return -1, or 0, or 1 if solution1 is has greater, equal, or less distance value than solution2,
     * respectively.
     */
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
        } else {
            double distance1 = Double.MIN_VALUE;
            double distance2 = Double.MIN_VALUE;

            if (this.crowdingDistance.getAttribute(solution1) != null) {
                distance1 = this.crowdingDistance.getAttribute(solution1);
            }

            if (this.crowdingDistance.getAttribute(solution2) != null) {
                distance2 = this.crowdingDistance.getAttribute(solution2);
            }

            result = Double.compare(distance2, distance1);
        }

        return result;
    }
}
