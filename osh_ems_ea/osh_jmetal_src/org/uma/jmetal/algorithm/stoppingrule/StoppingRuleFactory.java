package org.uma.jmetal.algorithm.stoppingrule;

import org.uma.jmetal.util.JMetalException;

import java.util.Map;

/**
 * Represents a factory for building stopping rule instances based on given parameters.
 */
public class StoppingRuleFactory {

    /**
     * Contrcuts and returns a stopping rule based on a given name and agiven parameter collection.
     *
     * @param name the name of the stopping rule
     * @param parameters the parameter collection of the stopping rule
     *
     * @return the constructed stopping rule
     */
    public static StoppingRule getStoppingRule(String name, Map<String, Object> parameters) {
        StoppingRuleType ruleType = StoppingRuleType.fromName(name);

        if (ruleType == StoppingRuleType.MAX_EVALUATIONS)
            return new EvaluationsStoppingRule(parameters);
        else if (ruleType == StoppingRuleType.DELTA_FITNESS)
            return new DeltaFitnessStoppingRule(parameters);
        else {
            throw new JMetalException("Exception in " + name + ".getStoppingRule()");
        }
    }
}
