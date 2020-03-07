package org.uma.jmetal.algorithm.stoppingrule;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;

import java.util.List;
import java.util.Map;

/**
 * Represents a stopping condition based upon reaching a minimum amount of evaluations of candidate solutions.
 *
 * @author Sebastian Kramer
 */
public class EvaluationsStoppingRule extends StoppingRule {

    private final int populationSize;
    private final int maxEvaluations;

    /**
     * Constructs this stopping rule with the given parameter collection.
     *
     * @param parameters the parameters for the stopping condition
     */
    public EvaluationsStoppingRule(Map<String, Object> parameters) {
        super(parameters);

        if (this._parameters.get("populationSize") != null)
            this.populationSize = (int) parameters.get("populationSize");
        else {
            throw new JMetalException("no populationSize in parameters");
        }

        if (this._parameters.get("maxEvaluations") != null)
            this.maxEvaluations = (int) parameters.get("maxEvaluations");
        else {
            throw new JMetalException("no maxEvaluations in parameters");
        }
    }

    /**
     * Checks and returns if the stopping condition is reached based upon the given algorithm state.
     *
     * <p>
     * Condition will be true, if: <br/>
     * <ul>
     * <li> the number of evaluation of candidate solutions done is greater or equal to the set cutoff-point
     * </ul>
     * <p>
     *
     * @param problem the underlying problem of the algorithm
     * @param generation the number of generations that have passed since the start of the execution
     * @param evaluations the number of evaltuations that have been done since the start of the execution
     * @param currentSortedSolutions the current (sorted) set of solutions
     *
     * @return true if the number of evaluation of candidate solutions done is greater or equal to the set cutoff-point
     */
    @Override
    public boolean checkIfStop(Problem<? extends Solution<?>> problem, int generation, int evaluations, List<?
            extends Solution<?>> currentSortedSolutions) {
        if (evaluations > -1) {
            if (evaluations >= this.maxEvaluations) {
                this._msg = "Optimisation stopped after reaching max evaluations: " + evaluations;
                return true;
            }
        } else {
            int realSize = Math.max(currentSortedSolutions.size(), this.populationSize);
            if (generation * realSize >= this.maxEvaluations) {
                this._msg = "Optimisation stopped after reaching max evaluations: " + (generation * realSize);
                return true;
            }
        }

        return false;
    }
}
