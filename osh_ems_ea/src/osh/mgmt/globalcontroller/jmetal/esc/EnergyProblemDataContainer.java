package osh.mgmt.globalcontroller.jmetal.esc;

import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.esc.UUIDCommodityMap;
import osh.utils.DeepCopy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data-container for all necessary parts to evaluate a proposed solution for the {@link EnergyManagementProblem}.
 *
 * @author Sebastian Kramer
 */
public class EnergyProblemDataContainer {

    private final List<InterdependentProblemPart<?, ?>> allProblemParts;
    private final List<InterdependentProblemPart<?, ?>> allActivePPs;
    private final List<InterdependentProblemPart<?, ?>> allPassivePPs;
    private final List<InterdependentProblemPart<?, ?>> allActiveNeedsInputPPs;
    private final OCEnergySimulationCore ocESC;
    private final UUIDCommodityMap activeToPassiveMap;
    private final UUIDCommodityMap passiveToActiveMap;

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
    public EnergyProblemDataContainer(List<InterdependentProblemPart<?, ?>> allProblemParts,
                                      List<InterdependentProblemPart<?, ?>> allActivePPs,
                                      List<InterdependentProblemPart<?, ?>> allPassivePPs,
                                      List<InterdependentProblemPart<?, ?>> allActiveNeedsInputPPs,
                                      OCEnergySimulationCore ocESC,
                                      UUIDCommodityMap activeToPassiveMap,
                                      UUIDCommodityMap passiveToActiveMap) {
        Objects.requireNonNull(allProblemParts);
        Objects.requireNonNull(allActivePPs);
        Objects.requireNonNull(allPassivePPs);
        Objects.requireNonNull(allActiveNeedsInputPPs);
        Objects.requireNonNull(ocESC);
        Objects.requireNonNull(activeToPassiveMap);
        Objects.requireNonNull(passiveToActiveMap);

        this.allProblemParts = allProblemParts;
        this.allActivePPs = allActivePPs;
        this.allPassivePPs = allPassivePPs;
        this.allActiveNeedsInputPPs = allActiveNeedsInputPPs;
        this.ocESC = ocESC;
        this.activeToPassiveMap = activeToPassiveMap;
        this.passiveToActiveMap = passiveToActiveMap;
    }

    /**
     * Returns the collection of all problem-parts.
     *
     * @return a list of all problem-parts
     */
    public List<InterdependentProblemPart<?, ?>> getAllProblemParts() {
        return this.allProblemParts;
    }

    /**
     * Returns the collection of all active problem-parts.
     *
     * @return a list of all active problem-parts
     */
    public List<InterdependentProblemPart<?, ?>> getAllActivePPs() {
        return this.allActivePPs;
    }

    /**
     * Returns the collection of all passive problem-parts.
     *
     * @return a list of all passive problem-parts
     */
    public List<InterdependentProblemPart<?, ?>> getAllPassivePPs() {
        return this.allPassivePPs;
    }

    /**
     * Returns the collection of all active problem-parts that need input.
     *
     * @return a list of all active problem-parts that need input
     */
    public List<InterdependentProblemPart<?, ?>> getAllActiveNeedsInputPPs() {
        return this.allActiveNeedsInputPPs;
    }

    /**
     * Returns the energy-simulation-core for use in the optimization loop.
     *
     * @return the energy-simulation-core for use in the optimization loop
     */
    public OCEnergySimulationCore getOcESC() {
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
     * Returns a deep-copy of this data-container.
     *
     * @return a deep-copy of this container
     */
    public EnergyProblemDataContainer getDeepCopy() {
        List<InterdependentProblemPart<?, ?>> allPPsCopy =
                new ArrayList<>(this.allProblemParts.size());
        List<InterdependentProblemPart<?, ?>> allActivePPsCopy =
                new ArrayList<>(this.allActivePPs.size());
        List<InterdependentProblemPart<?, ?>> allPassivePPsCopy =
                new ArrayList<>(this.allPassivePPs.size());
        List<InterdependentProblemPart<?, ?>> allActiveNeedsInputPPsCopy =
                new ArrayList<>(this.allActiveNeedsInputPPs.size());

        for (InterdependentProblemPart<?, ?> part : this.allProblemParts) {
            //we do not need to copy completely static ipps
            if (!part.isCompletelyStatic()) {
                allPPsCopy.add((InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
            }
        }

        for (InterdependentProblemPart<?, ?> part : allPPsCopy) {
            if (this.allActivePPs.stream().anyMatch(p -> p.getId() == part.getId())) {
                allActivePPsCopy.add(part);
            }
            if (this.allPassivePPs.stream().anyMatch(p -> p.getId() == part.getId())) {
                allPassivePPsCopy.add(part);
            }
            if (this.allActiveNeedsInputPPs.stream().anyMatch(p -> p.getId() == part.getId())) {
                allActiveNeedsInputPPsCopy.add(part);
            }
        }

        return new EnergyProblemDataContainer(allPPsCopy, allActivePPsCopy,
                allPassivePPsCopy, allActiveNeedsInputPPsCopy,
                (OCEnergySimulationCore) DeepCopy.copy(this.ocESC),
                new UUIDCommodityMap(this.activeToPassiveMap),
                new UUIDCommodityMap(this.passiveToActiveMap));
    }
}
