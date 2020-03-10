package org.uma.jmetal.algorithm.stoppingrule;

/**
 * Represents the different types of stopping rules.
 *
 * @author Sebastian Kramer
 */
public enum StoppingRuleType {

    MAX_EVALUATIONS("EvaluationsStoppingRule"),
    DELTA_FITNESS("DeltaFitnessStoppingRule");

    private final String name;

    /**
     * Construct the stopping rule with the given name.
     *
     * @param name the name of the stopping rule
     */
    StoppingRuleType(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the stopping rule.
     * @return the name of the stopping rule
     */
    public String getName() {
        return this.name;
    }

    /**
     * Finds and returns the enum value corresponding to the given name.
     *
     * @param name the name of the enum value to find
     * @return the enum value corresponding to the name
     */
    public static StoppingRuleType fromName(String name) {
        for (StoppingRuleType e : StoppingRuleType.values()) {
            if (e.name.equalsIgnoreCase(name)) return e;
        }

        throw new IllegalArgumentException(name);
    }
}
