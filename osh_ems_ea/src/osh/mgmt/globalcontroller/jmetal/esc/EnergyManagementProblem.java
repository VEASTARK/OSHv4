package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import org.uma.jmetal.solution.Solution;

/**
 * Represents the the optimization problem of the OSH energy simulation.
 *
 * @author Sebastian Kramer
 */
public abstract class EnergyManagementProblem<S extends Solution<?>> extends AbstractGenericProblem<S> {

    private static final long serialVersionUID = 8965460063628234919L;

    private final EMProblemEvaluator evaluator;

    public EnergyManagementProblem(EMProblemEvaluator evaluator) {
        this.evaluator = evaluator;

        this.setNumberOfObjectives(1);
        this.setNumberOfConstraints(0);
    }

    @Override
    public void evaluate(S solution) {
        this.evaluator.evaluate(solution);
    }

    @Override
    public void evaluateFinal(S solution, boolean log) {
        this.evaluator.evaluateFinalTime(solution, log);
    }

    public EMProblemEvaluator getEvaluator() {
        return this.evaluator;
    }
}
