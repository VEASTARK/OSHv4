package osh.mgmt.globalcontroller.jmetal.solution;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.util.Comparator;

/**
 * Represents a {@link Comparator} for jMetal {@link Solution} based on Nash bargaining.
 *
 * @author Sebastian Kramer
 */
public class NashBargainingComparator<S extends Solution<?>> implements Comparator<S> {

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
        } else if (solution1.getNumberOfObjectives() !=  solution2.getNumberOfObjectives()) {
            throw new JMetalException("The solution1 has " + solution1.getNumberOfObjectives() + " objectives "
                    + "but solution1 has " + solution2.getNumberOfObjectives() + " objectives ");
        } else {
            double objective1 = 1.0, objective2 = 1.0;

            for (int i = 0; i < solution1.getNumberOfObjectives(); i++) {
                objective1 *= (1.0 - solution1.getObjective(i));
                objective2 *= (1.0 - solution2.getObjective(i));
            }
            objective1 *= -1.0;
            objective2 *= -1.0;

            result = Double.compare(objective1, objective2);
        }
        return result;
    }
}
