package osh.esc.grid;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public interface EnergyGrid {

    // optimization, speeds up grid calculation by considering only relations
    // for every future calculation for the given UUIDs

    /**
     * Initialize grid by loading all relations into lists
     */
    void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                        Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap, Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap);

    /**
     * Finalize grid by unloading all relations
     */
    void finalizeGrid();

    /**
     * Simulation: <br>
     * Do grid calculation and update states
     */
    void doCalculation(
            Map<UUID, LimitedCommodityStateMap> commodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
//			UUIDEnumMap totalInputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> totalAncillaryInputStates);
            AncillaryMeterState ancillaryMeterState);

    /**
     * O/C-Simulation: <br>
     * Do active to passive part update
     */
    void doActiveToPassiveCalculation(
            Set<UUID> passiveNodes,
//			Map<UUID, EnumMap<Commodity, RealCommodityState>> activeStates,
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> totalAncillaryInputStates);
            AncillaryMeterState ancillaryMeterState);

    /**
     * O/C-Simulation: <br>
     * Do passive to active part update
     */
    void doPassiveToActiveCalculation(
            Set<UUID> activeNodes,
//			Map<UUID, EnumMap<Commodity, RealCommodityState>> passiveStates,
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates);

    /**
     * Get virtual meters
     *
     * @return Virtual meters
     */
    Set<UUID> getMeterUUIDs();

    /**
     * Get UUIDs of active IPPs
     *
     * @return UUIDs of active IPPs
     */
    Set<UUID> getActiveUUIDs();

    /**
     * Get UUIDs of passive IPPs
     *
     * @return UUIDs of passive IPPs
     */
    Set<UUID> getPassiveUUIDs();

}
