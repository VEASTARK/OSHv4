package org.uma.jmetal.algorithm.stoppingrule;

import org.uma.jmetal.util.JMetalException;
import osh.configuration.oc.StoppingRuleType;

import java.util.Map;

/**
 * Represents a factory for building stopping rule instances based on given parameters.
 */
public class StoppingRuleFactory {

    /**
     * Contrcuts and returns a stopping rule based on a given name and agiven parameter collection.
     *
     * @param stoppingRule the type of the stopping rule
     * @param parameters the parameter collection of the stopping rule
     *
     * @return the constructed stopping rule
     */
    public static StoppingRule getStoppingRule(StoppingRuleType stoppingRule, Map<String, Object> parameters) {

        if (stoppingRule == StoppingRuleType.MAX_EVALUATIONS)
            return new EvaluationsStoppingRule(parameters);
        else if (stoppingRule == StoppingRuleType.DELTA_FITNESS)
            return new DeltaFitnessStoppingRule(parameters);
        else {
            throw new JMetalException("Exception in " + stoppingRule.name() + ".getStoppingRule()");
        }
    }
}
