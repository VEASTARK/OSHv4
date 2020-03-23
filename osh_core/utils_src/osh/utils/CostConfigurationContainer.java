package osh.utils;

import osh.utils.CostConfigurationTypes.AUTO_CONSUMPTION_COSTS;
import osh.utils.CostConfigurationTypes.FEED_IN_COSTS;
import osh.utils.CostConfigurationTypes.PLS_COSTS;
import osh.utils.CostConfigurationTypes.REACTIVE_COSTS;

/**
 * Represents a container for the full configuration of the cost-calculation-function.
 *
 * @author Sebastian Kramer
 */
public class CostConfigurationContainer implements Cloneable {

    private final REACTIVE_COSTS reactiveConfiguration;
    private final PLS_COSTS plsConfiguration;
    private final FEED_IN_COSTS feedInConfiguration;
    private final AUTO_CONSUMPTION_COSTS autoConsumptionConfiguration;

    /**
     * Creates this data-container with the given values for all configuration values.
     *
     * @param reactiveConfiguration configuration setting for the calculation of reactive power
     * @param plsConfiguration configuration setting for the calculation of pls-violation
     * @param feedInConfiguration configuration setting for the calculation of feed-in power
     * @param autoConsumptionConfiguration configuration setting for the calculation of self-consumption of produced
     *                                     power
     */
    public CostConfigurationContainer(REACTIVE_COSTS reactiveConfiguration, PLS_COSTS plsConfiguration,
                                      FEED_IN_COSTS feedInConfiguration,
                                      AUTO_CONSUMPTION_COSTS autoConsumptionConfiguration) {
        this.reactiveConfiguration = reactiveConfiguration;
        this.plsConfiguration = plsConfiguration;
        this.feedInConfiguration = feedInConfiguration;
        this.autoConsumptionConfiguration = autoConsumptionConfiguration;
    }

    /**
     * Creates this data-container based on the old optimization objective framework.
     *
     * @param epsOptimizationObjective optimization objective for the active power calculation
     * @param plsOptimizationObjective optimization objective for the calculation of pls-violations
     * @param varOptimizationObjective optimization objective for the reactive power calculation
     */
    public CostConfigurationContainer(int epsOptimizationObjective, int plsOptimizationObjective,
                                      int varOptimizationObjective) {

         switch(varOptimizationObjective) {
             case 0:
                 this.reactiveConfiguration = REACTIVE_COSTS.NONE;
                 break;
             case 1:
                 this.reactiveConfiguration = REACTIVE_COSTS.FULL;
                 break;
             default:
                 throw new IllegalArgumentException("optimization objective for reactive power not recognized.");
         }

        switch(plsOptimizationObjective) {
            case 0:
                this.plsConfiguration = PLS_COSTS.NONE;
                break;
            case 1:
                this.plsConfiguration = PLS_COSTS.FULL_ACTIVE;
                break;
            case 2:
                this.plsConfiguration = PLS_COSTS.FULL;
                break;
            default:
                throw new IllegalArgumentException("optimization objective for power-limit violation not recognized.");
        }

        switch(epsOptimizationObjective) {
            case 0:
                this.feedInConfiguration = FEED_IN_COSTS.NONE;
                this.autoConsumptionConfiguration = AUTO_CONSUMPTION_COSTS.NONE;
                break;
            case 1:
                this.feedInConfiguration = FEED_IN_COSTS.PV;
                this.autoConsumptionConfiguration = AUTO_CONSUMPTION_COSTS.NONE;
                break;
            case 2:
                this.feedInConfiguration = FEED_IN_COSTS.PV;
                this.autoConsumptionConfiguration = AUTO_CONSUMPTION_COSTS.PV;
                break;
            case 3:
                this.feedInConfiguration = FEED_IN_COSTS.BOTH;
                this.autoConsumptionConfiguration = AUTO_CONSUMPTION_COSTS.NONE;
                break;
            case 4:
                this.feedInConfiguration = FEED_IN_COSTS.BOTH;
                this.autoConsumptionConfiguration = AUTO_CONSUMPTION_COSTS.BOTH;
                break;
            default:
                throw new IllegalArgumentException("eps-optimization objective not recognized.");
        }
    }

    /**
     * Creates a copy of the given data-container.
     *
     * @param other the data-container to copy.
     */
    public CostConfigurationContainer(CostConfigurationContainer other) {
        this.reactiveConfiguration = other.reactiveConfiguration;
        this.plsConfiguration = other.plsConfiguration;
        this.feedInConfiguration = other.feedInConfiguration;
        this.autoConsumptionConfiguration = other.autoConsumptionConfiguration;
    }

    /**
     * Returns the configuration for reactive power calculation.
     *
     * @return the configuration for reactive power calculation
     */
    public REACTIVE_COSTS getReactiveConfiguration() {
        return this.reactiveConfiguration;
    }

    /**
     * Returns the configuration for the calculation of pls-violations.
     *
     * @return the configuration for the calculation of pls-violations
     */
    public PLS_COSTS getPlsConfiguration() {
        return this.plsConfiguration;
    }

    /**
     * Returns the configuration for feed-in power calculation.
     *
     * @return the configuration for feed-in power calculation
     */
    public FEED_IN_COSTS getFeedInConfiguration() {
        return this.feedInConfiguration;
    }

    /**
     * Returns the configuration for the calculation of self-consumption of produced power.
     *
     * @return the configuration for calculation of self-consumption of produced power
     */
    public AUTO_CONSUMPTION_COSTS getAutoConsumptionConfiguration() {
        return this.autoConsumptionConfiguration;
    }

    /**
     * Clones this data-container and returns the copy.
     *
     * @return a clone of this data-container
     */
    public CostConfigurationContainer clone() {
        return new CostConfigurationContainer(this);
    }
}
