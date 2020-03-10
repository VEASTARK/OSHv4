package org.uma.jmetal.algorithm.stoppingrule;


import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;

import java.util.List;
import java.util.Map;

/**
 * Represents a pre-determined condition upon which the executing algorithm should terminate.
 *
 * @author Sebastian Kramer
 */
public abstract class StoppingRule {

    String _msg;
    Map<String, Object> _parameters;

    /**
     * Creates this stopping condition with the given parameters.
     *
     * @param parameters the parameters for the stopping condition
     */
    public StoppingRule(Map<String, Object> parameters) {
        this._parameters = parameters;
    }

    /**
     * Checks and returns if the stopping condition is reached based upon the given algorithm state.
     *
     * @param problem the underlying problem of the algorithm
     * @param generation the number of generations that have passed since the start of the execution
     * @param evaluations the number of evaltuations that have been done since the start of the execution
     * @param currentSolutions the current set of solutions
     *
     * @return true if the stopping condition has been reached
     */
    public abstract <S extends Solution<?>> boolean checkIfStop(Problem<S> problem,
                                                                int generation,
                                                                int evaluations,
                                                                List<S> currentSolutions);

    /**
     * Returns a message containing information about why the stopping condition was triggered, or an empty string if
     * has not.
     *
     * @return a message containing information about why the stopping condition was triggered
     */
    public String getMsg() {
        return this._msg;
    }
}
