package osh.mgmt.globalcontroller.jmetal.esc;

import osh.datatypes.power.ErsatzACLoadProfile;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OptimizationEnergySimulationCore;
import osh.esc.UUIDCommodityMap;

import java.util.Arrays;
import java.util.Objects;

/**
 * Data-container for all necessary parts to evaluate a proposed solution for the {@link EMProblemEvaluator}.
 *
 * @author Sebastian Kramer
 */
public class EnergyProblemDataContainer {

    private final InterdependentProblemPart<?, ?>[] allProblemParts;
    private final InterdependentProblemPart<?, ?>[] allActivePPs;
    private final InterdependentProblemPart<?, ?>[] allPassivePPs;
    private final InterdependentProblemPart<?, ?>[] allActiveNeedsInputPPs;
    private final OptimizationEnergySimulationCore ocESC;
    private final UUIDCommodityMap activeToPassiveMap;
    private final UUIDCommodityMap passiveToActiveMap;
    private final ErsatzACLoadProfile ancillaryLoadProfile;

    /**
     * Generates this data-object with all the given consituting parts.
     *
     * @param allProblemParts a collection of all problem-parts
     * @param allActivePPs a collection of all problem-parts that can be classified as active
     * @param allPassivePPs a collection of all problem-parts that can be cassified as passive
     * @param allActiveNeedsInputPPs a sub-collection of active problem-parts that require input to calculate their
     *                               state
     * @param ocESC the energy-simulation-core for use in the optimization loop
     * @param activeToPassiveMap the map used for active-to-passive exchange
     * @param passiveToActiveMap the map used for passive-to-active exchange
     */
    public EnergyProblemDataContainer(InterdependentProblemPart<?, ?>[] allProblemParts,
                                      InterdependentProblemPart<?, ?>[] allActivePPs,
                                      InterdependentProblemPart<?, ?>[] allPassivePPs,
                                      InterdependentProblemPart<?, ?>[] allActiveNeedsInputPPs,
                                      OptimizationEnergySimulationCore ocESC,
                                      UUIDCommodityMap activeToPassiveMap,
                                      UUIDCommodityMap passiveToActiveMap,
                                      ErsatzACLoadProfile ancillaryLoadProfile) {
        Objects.requireNonNull(allProblemParts);
        Objects.requireNonNull(allActivePPs);
        Objects.requireNonNull(allPassivePPs);
        Objects.requireNonNull(allActiveNeedsInputPPs);
        Objects.requireNonNull(ocESC);
        Objects.requireNonNull(activeToPassiveMap);
        Objects.requireNonNull(passiveToActiveMap);
        Objects.requireNonNull(ancillaryLoadProfile);

        this.allProblemParts = allProblemParts;
        this.allActivePPs = allActivePPs;
        this.allPassivePPs = allPassivePPs;
        this.allActiveNeedsInputPPs = allActiveNeedsInputPPs;
        this.ocESC = ocESC;
        this.activeToPassiveMap = activeToPassiveMap;
        this.passiveToActiveMap = passiveToActiveMap;
        this.ancillaryLoadProfile = ancillaryLoadProfile;
    }

    /**
     * Returns the collection of all problem-parts.
     *
     * @return a list of all problem-parts
     */
    public InterdependentProblemPart<?, ?>[] getAllProblemParts() {
        return this.allProblemParts;
    }

    /**
     * Returns the collection of all active problem-parts.
     *
     * @return a list of all active problem-parts
     */
    public InterdependentProblemPart<?, ?>[] getAllActivePPs() {
        return this.allActivePPs;
    }

    /**
     * Returns the collection of all passive problem-parts.
     *
     * @return a list of all passive problem-parts
     */
    public InterdependentProblemPart<?, ?>[] getAllPassivePPs() {
        return this.allPassivePPs;
    }

    /**
     * Returns the collection of all active problem-parts that need input.
     *
     * @return a list of all active problem-parts that need input
     */
    public InterdependentProblemPart<?, ?>[] getAllActiveNeedsInputPPs() {
        return this.allActiveNeedsInputPPs;
    }

    /**
     * Returns the energy-simulation-core for use in the optimization loop.
     *
     * @return the energy-simulation-core for use in the optimization loop
     */
    public OptimizationEnergySimulationCore getOcESC() {
        return this.ocESC;
    }

    /**
     * Returns the map used for active-to-passive exchange.
     *
     * @return the map used for active-to-passive exchange
     */
    public UUIDCommodityMap getActiveToPassiveMap() {
        return this.activeToPassiveMap;
    }

    /**
     * Returns the map used for passive-to-active exchange.
     *
     * @return the map used for passive-to-active exchange
     */
    public UUIDCommodityMap getPassiveToActiveMap() {
        return this.passiveToActiveMap;
    }

    /**
     * Returns the load profile used for storing the ancillary meter state.
     *
     * @return the load profile used for storing the ancillary meter state
     */
    public ErsatzACLoadProfile getAncillaryLoadProfile() {
        return this.ancillaryLoadProfile;
    }

    /**
     * Returns a deep-copy of this data-container.
     *
     * @return a deep-copy of this container
     */
    public EnergyProblemDataContainer getDeepCopy() {
        InterdependentProblemPart<?, ?>[] allPPsCopy =
                new InterdependentProblemPart<?, ?>[(int) (this.allProblemParts.length - Arrays.stream(this.allProblemParts).filter(InterdependentProblemPart::isCompletelyStatic).count())];
        InterdependentProblemPart<?, ?>[] allActivePPsCopy =
                new InterdependentProblemPart<?, ?>[this.allActivePPs.length];
        InterdependentProblemPart<?, ?>[] allPassivePPsCopy =
                new InterdependentProblemPart<?, ?>[this.allPassivePPs.length];
        InterdependentProblemPart<?, ?>[] allActiveNeedsInputPPsCopy =
                new InterdependentProblemPart<?, ?>[this.allActiveNeedsInputPPs.length];

        int allIndex = 0, activeIndex = 0, passiveIndex = 0, activeNeedsInputIndex = 0;
        for (InterdependentProblemPart<?, ?> part : this.allProblemParts) {
            //we do not need to copy completely static ipps
            if (!part.isCompletelyStatic()) {
                allPPsCopy[allIndex++] = part.getClone();
            }
        }

        for (InterdependentProblemPart<?, ?> part : allPPsCopy) {
            if (Arrays.stream(this.allActivePPs).anyMatch(p -> p.getId() == part.getId())) {
                allActivePPsCopy[activeIndex++] = part;
            }
            if (Arrays.stream(this.allPassivePPs).anyMatch(p -> p.getId() == part.getId())) {
                allPassivePPsCopy[passiveIndex++] = part;
            }
            if (Arrays.stream(this.allActiveNeedsInputPPs).anyMatch(p -> p.getId() == part.getId())) {
                allActiveNeedsInputPPsCopy[activeNeedsInputIndex++] = part;
            }
        }

        return new EnergyProblemDataContainer(allPPsCopy, allActivePPsCopy,
                allPassivePPsCopy, allActiveNeedsInputPPsCopy,
                new OptimizationEnergySimulationCore(this.ocESC),
                new UUIDCommodityMap(this.activeToPassiveMap),
                new UUIDCommodityMap(this.passiveToActiveMap),
                new ErsatzACLoadProfile(this.ancillaryLoadProfile));
    }
}
