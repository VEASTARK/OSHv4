package org.uma.jmetal.problem;

import org.uma.jmetal.solution.DoubleSolution;

/**
 * Interface representing continuous problems
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public interface DoubleProblem extends BoundedProblem<Double, DoubleSolution> {

    /**
     * @param index index of the variable
     * @return unboxed lower bound of the variable
     */
    default double getUnboxedLowerBound(int index) {
        return this.getLowerBound(index);
    }

    /**
     * @param index index of the variable
     * @return unboxed upper bound of the variable
     */
    default double getUnboxedUpperBound(int index) {
        return this.getUpperBound(index);
    }
    
}
