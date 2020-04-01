package osh.mgmt.globalcontroller.jmetal.esc;

import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import org.uma.jmetal.solution.Solution;
import osh.configuration.oc.EAObjectives;

import java.util.List;

/**
 * Represents the the optimization problem of the OSH energy simulation.
 *
 * @author Sebastian Kramer
 */
public abstract class EnergyManagementProblem<S extends Solution<?>> extends AbstractGenericProblem<S> {

    private static final long serialVersionUID = 8965460063628234919L;

    private final EMProblemEvaluator evaluator;

    /**
     * Constructs this energy management problem with the provided problem evaluator and the collection of objectives.
     *
     * @param evaluator the evaluator for the problem
     * @param objectives the collection of objective
     */
    public EnergyManagementProblem(EMProblemEvaluator evaluator, List<EAObjectives> objectives) {
        this.evaluator = evaluator;

        this.setNumberOfObjectives(objectives.size());
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

    /**
     * Returns the problem evaluator.
     *
     * @return the problem evaluator
     */
    public EMProblemEvaluator getEvaluator() {
        return this.evaluator;
    }
}
