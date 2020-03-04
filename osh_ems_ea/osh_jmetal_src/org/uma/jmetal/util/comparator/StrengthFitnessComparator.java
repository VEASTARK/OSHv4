package org.uma.jmetal.util.comparator;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.solutionattribute.impl.StrengthRawFitness;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @param <S>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class StrengthFitnessComparator<S extends Solution<?>> implements Comparator<S>, Serializable {
    private final StrengthRawFitness<S> fitnessValue = new StrengthRawFitness<>();

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
            double strengthFitness1 = Double.MIN_VALUE;
            double strengthFitness2 = Double.MIN_VALUE;

            if (this.fitnessValue.getAttribute(solution1) != null) {
                strengthFitness1 = this.fitnessValue.getAttribute(solution1);
            }

            if (this.fitnessValue.getAttribute(solution2) != null) {
                strengthFitness2 = this.fitnessValue.getAttribute(solution2);
            }

            result = Double.compare(strengthFitness1, strengthFitness2);
        }
        return result;
    }

}
