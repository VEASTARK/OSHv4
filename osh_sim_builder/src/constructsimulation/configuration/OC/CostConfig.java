package constructsimulation.configuration.OC;

import osh.configuration.oc.*;

/**
 * Static container for configuration of the cost-calculation (optimization objectives) in the house to construct.
 *
 * @author Sebastian Kramer
 */
public class CostConfig {

    public static final double overLimitFactor = 1.0;

    public static ReactiveCosts reactiveCosts = ReactiveCosts.NONE;
    public static ActivePlsCosts activePlsCosts = ActivePlsCosts.FULL;
    public static ReactivePlsCosts reactivePlsCosts = ReactivePlsCosts.NONE;
    public static FeedInCosts feedInCosts = FeedInCosts.BOTH;
    public static AutoConsumptionCosts autoConsumptionCosts = AutoConsumptionCosts.BOTH;
    public static final SelfSufficiencyRatio selfSufficiencyRatio = SelfSufficiencyRatio.NONE;
    public static final SelfConsumptionRatio selfConsumptionRatio = SelfConsumptionRatio.NONE;

    /**
     * Generates the cost configuration.
     *
     * @return the cost configuration
     */
    public static CostConfiguration generateCostConfig() {
        CostConfiguration costConfig = new CostConfiguration();

        costConfig.setReactiveCosts(reactiveCosts);
        costConfig.setActivePlsCosts(activePlsCosts);
        costConfig.setReactivePlsCosts(reactivePlsCosts);
        costConfig.setFeedInCosts(feedInCosts);
        costConfig.setAutoConsumptionCosts(autoConsumptionCosts);
        costConfig.setSelfSufficiencyRatio(selfSufficiencyRatio);
        costConfig.setSelfConsumptionRatio(selfConsumptionRatio);
        costConfig.setOverlimitFactor(overLimitFactor);

        return costConfig;
    }

}
