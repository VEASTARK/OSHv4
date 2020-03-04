package jmetal.metaheuristics.stoppingRule;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;
import jmetal.util.Configuration;
import jmetal.util.JMException;

import java.util.Map;

public class EvaluationsStoppingRule extends StoppingRule {

    private final int populationSize;
    private final int maxEvaluations;

    public EvaluationsStoppingRule(Map<String, Object> parameters) throws JMException {
        super(parameters);

        if (this._parameters.get("populationSize") != null)
            this.populationSize = (int) parameters.get("populationSize");
        else {
            Configuration.logger_.severe("EvaluationsStoppingRule no populationSize in parameters.");
            throw new JMException("no populationSize in parameters");
        }

        if (this._parameters.get("maxEvaluations") != null)
            this.maxEvaluations = (int) parameters.get("maxEvaluations");
        else {
            Configuration.logger_.severe("EvaluationsStoppingRule no maxEvaluations in parameters.");
            throw new JMException("no maxEvaluations in parameters");
        }
    }

    @Override
    public boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions) {
        if ((generation + 1) * this.populationSize >= this.maxEvaluations) {
            this._msg = "Optimisation stopped after reaching max evaluations: " + ((generation + 1) * this.populationSize);
            return true;
        }

        return false;
    }
}
