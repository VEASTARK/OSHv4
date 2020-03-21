package osh.utils;

/**
 * Represents the different type of calculated costs that can be returned.
 *
 * @author Sebastian Kramer
 */
public enum CostReturnType {

    ELECTRICITY,
    GAS;

    /**
     * Represents the different type of calculated costs that can be returned of calculated for a single simulation
     * step.
     *
     */
    public enum SingleStepCostReturnType {
        EPS,
        PLS,
        FEED_IN_PV,
        FEED_IN_CHP,
        AUTO_CONSUMPTION,
        GAS;
    }

}