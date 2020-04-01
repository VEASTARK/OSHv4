package osh.utils;

/**
 * Represents a collection of configuration enums that dictate how the cost calculation function should operate.
 *
 * @author Sebastian Kramer
 */
public class CostConfigurationTypes {

    /**
     * Dictates how costs for reactive power should be calculated.
     */
    public enum REACTIVE_COSTS {
        NONE,
        FULL
    }

    /**
     * Dictates how costs for violating power-limit-signals for active power should be calculated.
     */
    public enum ACTIVE_PLS_COSTS {
        NONE,
        UPPER,
        FULL
    }

    /**
     * Dictates how costs for violating power-limit-signals for reactive power should be calculated.
     */
    public enum VAR_PLS_COSTS {
        NONE,
        FULL
    }

    /**
     * Dictates how costs for feed-in of power into the grid should be calculated.
     */
    public enum FEED_IN_COSTS {
        NONE,
        PV,
        CHP,
        BOTH
    }

    /**
     * Dictates how costs for self-consumption of produced power should be calculated.
     */
    public enum AUTO_CONSUMPTION_COSTS {
        NONE,
        PV,
        CHP,
        BOTH
    }

    /**
     * Dictates how costs the self-sufficiency-ratio should be calculated.
     */
    public enum SELF_SUFFICIENCY_RATIO {
        NO_RATIO,
        NORMAL
    }

    /**
     * Dictates how costs the self-soncumption-ratio should be calculated.
     */
    public enum SELF_CONSUMPTION_RATIO {
        NO_RATIO,
        NORMAL
    }
}
