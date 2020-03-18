package org.uma.jmetal.util.evaluator.impl;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.List;

/**
 * @author Sebastian Kramer
 */
@SuppressWarnings("serial")
public class MultithreadedStealingSolutionListEvaluator<S extends Solution<?>> implements SolutionListEvaluator<S> {

    public MultithreadedStealingSolutionListEvaluator() {
    }

    @Override
    public List<S> evaluate(List<S> solutionList, Problem<S> problem) {

        solutionList.parallelStream().forEach(problem::evaluate);

        return solutionList;
    }

    @Override
    public void evaluateFinal(S solution, Problem<S> problem) {
        problem.evaluateFinal(solution, true);
    }

    @Override
    public void shutdown() {
        //This method is an intentionally-blank override.
    }
}
