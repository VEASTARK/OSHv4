package osh.utils.costs;

import osh.configuration.oc.*;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.PowerInterval;
import osh.utils.CostReturnType.SingleStepCostReturnType;
import osh.utils.dataStructures.Enum2DoubleMap;
import osh.utils.physics.PhysicalConstants;

import java.util.EnumMap;

/**
 * Represents the internal cost function of the OSH, configurable with the {@link CostConfiguration} variables,
 * executing the calculation inside the optimization loop or in the {@link osh.simulation.SimulationEngine}.
 *
 * @author Sebastian Kramer
 */
public class RegularCostFunction {

    private final CostConfiguration costConfiguration;

    private final double overlimitFactor;

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;

    /**
     * Constructs this cost function with the given cost configuration and all relevant signals.
     *
     * @param overlimitFactor the overlimit factor for upper pls violations
     * @param costConfiguration the cost configuration
     * @param priceSignals the price signals
     * @param powerLimitSignals the power limit signals
     * @param now the current time in epoch seconds
     */
    public RegularCostFunction(double overlimitFactor, CostConfiguration costConfiguration,
                               EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
                               EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals, long now) {

        this.costConfiguration = costConfiguration;
        this.overlimitFactor = overlimitFactor;
        this.priceSignals = priceSignals;
        this.powerLimitSignals = powerLimitSignals;
    }

    /**
     * Update the signals to new value.
     *
     * @param priceSignals the price signals
     * @param powerLimitSignals the power limit signals
     */
    public void updateSignals(EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
                              EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        this.priceSignals = priceSignals;
        this.powerLimitSignals = powerLimitSignals;
    }

    /**
     * Calculates the costs resulting from the supplied meter state and the current cost configuration.
     *
     * @param ancillaryMeter the meter state
     * @param now the current time in epoch seconds
     * @param timeFactor the time factor to multiply the results with
     *
     * @return the costs resulting from the supplied meter state and the current cost configuration as a map
     */
    public Enum2DoubleMap<SingleStepCostReturnType> calculateSingleStepCosts(AncillaryMeterState ancillaryMeter,
                                                                             Long now, Long timeFactor) {

        Enum2DoubleMap<SingleStepCostReturnType> costMap = new Enum2DoubleMap<>(SingleStepCostReturnType.class);

        double activePower = ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
        double activePrice = this.priceSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPrice(now);

        //active power costs
        if (activePower > 0) {
            costMap.add(SingleStepCostReturnType.EPS, activePower * activePrice);
        }

        //reactive power costs
        if (this.costConfiguration.getReactiveCosts() == ReactiveCosts.FULL) {
            costMap.add(SingleStepCostReturnType.EPS,
                    Math.abs(ancillaryMeter.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL)) *
                            this.priceSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPrice(now));
        }

        //feed-in costs
        if (this.costConfiguration.getFeedInCosts() != FeedInCosts.NONE) {

            if (this.costConfiguration.getFeedInCosts() == FeedInCosts.PV
                    || this.costConfiguration.getFeedInCosts() == FeedInCosts.BOTH) {
                if (ancillaryMeter.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN) < 0) {
                    costMap.add(SingleStepCostReturnType.FEED_IN_PV,
                            ancillaryMeter.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN) *
                                    this.priceSignals.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPrice(now));
                }
            }

            if (this.costConfiguration.getFeedInCosts() == FeedInCosts.CHP
                    || this.costConfiguration.getFeedInCosts() == FeedInCosts.BOTH) {
                if (ancillaryMeter.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) < 0) {
                    costMap.add(SingleStepCostReturnType.FEED_IN_CHP,
                            ancillaryMeter.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) *
                                    this.priceSignals.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPrice(now));
                }
            }
        }

        //auto-consumption costs
        if (this.costConfiguration.getAutoConsumptionCosts() != AutoConsumptionCosts.NONE) {
            if (this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.PV
                    || this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.BOTH) {
                costMap.add(SingleStepCostReturnType.AUTO_CONSUMPTION,
                        ancillaryMeter.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION) *
                                this.priceSignals.get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION).getPrice(now));

            }

            if (this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.CHP
                    || this.costConfiguration.getAutoConsumptionCosts() == AutoConsumptionCosts.BOTH) {
                costMap.add(SingleStepCostReturnType.AUTO_CONSUMPTION,
                        ancillaryMeter.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION) *
                                this.priceSignals.get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION).getPrice(now));
            }
        }

        //gas costs
        if (ancillaryMeter.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL) > 0) {
            costMap.add(SingleStepCostReturnType.GAS,
                    ancillaryMeter.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL) *
                            this.priceSignals.get(AncillaryCommodity.NATURALGASPOWEREXTERNAL).getPrice(now));
        }

        //power-limit costs
        if (this.costConfiguration.getActivePlsCosts() != ActivePlsCosts.NONE) {
            PowerInterval activePowerLimit =
                    this.powerLimitSignals.get(AncillaryCommodity.ACTIVEPOWEREXTERNAL).getPowerLimitInterval(now);

            double upperLimit = activePowerLimit.getPowerUpperLimit();

            if (activePower > upperLimit)
                costMap.add(SingleStepCostReturnType.PLS,
                        this.overlimitFactor * Math.abs(activePower - upperLimit) * activePrice);

            if (this.costConfiguration.getActivePlsCosts() != ActivePlsCosts.UPPER) {
                double lowerLimit = activePowerLimit.getPowerLowerLimit();

                if (activePower < lowerLimit) {
                    if (this.costConfiguration.getFeedInCosts() != FeedInCosts.NONE) {
                        //we need to refund the feed-in costs that are over the limit
                        double overLimitPercentage = Math.abs(Math.abs(lowerLimit - activePower) / activePower);
                        costMap.add(SingleStepCostReturnType.PLS,
                                Math.abs(this.overlimitFactor * overLimitPercentage * (costMap.get(SingleStepCostReturnType.FEED_IN_PV) +
                                        costMap.get(SingleStepCostReturnType.FEED_IN_CHP))));
                    } else {
                        costMap.add(SingleStepCostReturnType.PLS,
                                this.overlimitFactor * Math.abs(activePower - lowerLimit) * activePrice);
                    }
                }
            }

            if (this.costConfiguration.getReactiveCosts() == ReactiveCosts.FULL
                    && this.costConfiguration.getReactivePlsCosts() == ReactivePlsCosts.FULL) {
                PowerInterval reactiveLimit =
                        this.powerLimitSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPowerLimitInterval(now);
                double reactivePower = ancillaryMeter.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

                if (reactivePower > reactiveLimit.getPowerUpperLimit()) {
                    costMap.add(SingleStepCostReturnType.PLS,
                            Math.abs(this.overlimitFactor * Math.abs(reactivePower - reactiveLimit.getPowerUpperLimit())
                                    * this.priceSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPrice(now)));
                } else if (reactivePower < reactiveLimit.getPowerLowerLimit()) {
                    costMap.add(SingleStepCostReturnType.PLS,
                            Math.abs(this.overlimitFactor * Math.abs(reactiveLimit.getPowerLowerLimit() - reactivePower)
                                    * this.priceSignals.get(AncillaryCommodity.REACTIVEPOWEREXTERNAL).getPrice(now)));
                }
            }
        }

        //SELF-SUFF
        if (this.costConfiguration.getSelfSufficiencyRatio() == SelfSufficiencyRatio.NORMAL) {

            double ssr = 1.0;

            if (ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL) > 0) {
                //apExt(+), autoconPV(-), autoconCHP(-), autoConBat(-)

                double numerator = ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
                //total cons --> ap + sum of autoCon
                double denominator = ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL)
                        - ancillaryMeter.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)
                        - ancillaryMeter.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)
                        - ancillaryMeter.getPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION);

                ssr = 1.0 - (numerator / denominator);
            }

            //minimization problem, but higher ssr is more desirable -> negate
            costMap.add(SingleStepCostReturnType.SELF_SUFFICIENCY_RATIO, -ssr);
        }

        //SELF-CONS
        if (this.costConfiguration.getSelfConsumptionRatio() == SelfConsumptionRatio.NORMAL) {

            double scr = 1.0;

            if (ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL) < 0) {
                //1 = apSuper(-), autoconPV(-), autoconCHP(-), autoConBat(-)

                double numerator = ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
                //total production sum --> ap + sum of autoCon
                // signs reversed as numerator is neg
                double denominator = ancillaryMeter.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL)
                        + ancillaryMeter.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION)
                        + ancillaryMeter.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION)
                        + ancillaryMeter.getPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION);

                scr = 1.0 - (numerator / denominator);
            }

            //minimization problem, but higher scr is more desirable -> negate
            costMap.add(SingleStepCostReturnType.SELF_CONSUMPTION_RATIO, -scr);
        }

        //apply time
        for (SingleStepCostReturnType value : SingleStepCostReturnType.values()) {
            if (value == SingleStepCostReturnType.SELF_CONSUMPTION_RATIO ||
                    value == SingleStepCostReturnType.SELF_SUFFICIENCY_RATIO) continue;
            costMap.put(value, (costMap.get(value) * timeFactor) / PhysicalConstants.factor_wsToKWh);
        }

        return costMap;
    }
}
