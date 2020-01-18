package jmetal.metaheuristics.stoppingRule;

import jmetal.core.Problem;
import jmetal.core.SolutionSet;

import java.util.Map;

public abstract class StoppingRule {

    protected String _msg;
    protected final Map<String, Object> _parameters;

    public StoppingRule(Map<String, Object> parameters) {
        this._parameters = parameters;
    }

    public abstract boolean checkIfStop(Problem problem, int generation, SolutionSet currentSortedSolutions);

    public String getMsg() {
        return this._msg;
    }
}
