package osh.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.system.GridConfig;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.grid.EnergyGrid;
import osh.esc.grid.EnergySimulationTypes;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * EnergySimulationCore
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class OCEnergySimulationCore extends EnergySimulationCore implements Serializable {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 350474217178426943L;
    UUIDCommodityMap a2pInputStateMap;
    UUIDCommodityMap p2aInputStateMap;
    private EnergyGrid[] allGrids;
    private EnergyGrid[] thermalGrids;

    /**
     * CONSTRUCTOR
     */
    public OCEnergySimulationCore(
            List<GridConfig> grids,
            String meterUUID) throws HALManagerException {
        super(grids, meterUUID);

        this.allGrids = new EnergyGrid[this.grids.size()];
        this.allGrids = this.grids.values().toArray(this.allGrids);
        this.thermalGrids = new EnergyGrid[1];
        this.thermalGrids[0] = this.grids.get(EnergySimulationTypes.THERMAL);
    }

    /**
     * CONSTRUCTOR
     */
    public OCEnergySimulationCore(
            Map<EnergySimulationTypes, EnergyGrid> grids,
            UUID meterUUID) {
        super(grids, meterUUID);

        this.allGrids = new EnergyGrid[this.grids.size()];
        this.allGrids = this.grids.values().toArray(this.allGrids);
        this.thermalGrids = new EnergyGrid[1];
        this.thermalGrids[0] = this.grids.get(EnergySimulationTypes.THERMAL);
    }

    /**
     * CONSTRUCTOR for serialization, do NOT use!
     */
    @Deprecated
    protected OCEnergySimulationCore() {

    }

    public void initializeGrids(
            Set<UUID> allActiveNodes,
            Set<UUID> activeNeedsInputNodes,
            Set<UUID> passiveNodes,
            Object2IntOpenHashMap<UUID> uuidToIntMap,
            Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap,
            Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap) {

        Object2IntOpenHashMap<UUID> uuidToIntMapWithMeter = new Object2IntOpenHashMap<>(uuidToIntMap);
        uuidToIntMapWithMeter.put(this.meterUUID, uuidToIntMap.size());

        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMapWithMeter = new Object2ObjectOpenHashMap<>(uuidOutputMap);
        uuidOutputMapWithMeter.put(this.meterUUID, EnumSet.allOf(Commodity.class));

        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMapWithMeter = new Object2ObjectOpenHashMap<>(uuidInputMap);
        uuidInputMapWithMeter.put(this.meterUUID, EnumSet.allOf(Commodity.class));


        for (Entry<EnergySimulationTypes, EnergyGrid> grid : this.grids.entrySet()) {
            grid.getValue().initializeGrid(allActiveNodes, activeNeedsInputNodes, passiveNodes, uuidToIntMapWithMeter, uuidOutputMapWithMeter);
        }

        ObjectOpenHashSet<UUID> passiveWithMeter = new ObjectOpenHashSet<>(passiveNodes);
        passiveWithMeter.add(this.meterUUID);

        this.a2pInputStateMap = new UUIDCommodityMap(passiveWithMeter, uuidToIntMapWithMeter, uuidInputMapWithMeter, true);

        this.p2aInputStateMap = new UUIDCommodityMap(activeNeedsInputNodes, uuidToIntMap, uuidInputMap, true);
    }

    public void finalizeGrids() {
        for (Entry<EnergySimulationTypes, EnergyGrid> grid : this.grids.entrySet()) {
            grid.getValue().finalizeGrid();
        }
    }

    private UUIDCommodityMap getA2PInputStateMap() {

        this.a2pInputStateMap.clearInnerStates();
        return this.a2pInputStateMap;

    }

    private UUIDCommodityMap getP2AInputStateMap() {

        this.p2aInputStateMap.clearInnerStates();
        return this.p2aInputStateMap;
    }

    public void doActiveToPassiveExchange(
            UUIDCommodityMap activeCommodityStates,
            List<InterdependentProblemPart<?, ?>> passiveParts,
            Set<UUID> passiveUUIDs,
            AncillaryMeterState ancillaryMeterState) {

        // input states
        UUIDCommodityMap totalInputStates = this.getA2PInputStateMap();

        // ancillary commodities input states
        ancillaryMeterState.clear();

        for (EnergyGrid grid : this.allGrids) {
            grid.doActiveToPassiveCalculation(
                    passiveUUIDs,
                    activeCommodityStates,
                    totalInputStates,
                    ancillaryMeterState);
        }

        // inform subjects about states
        for (InterdependentProblemPart<?, ?> _simSubject : passiveParts) {

            AncillaryMeterState clonedAncillaryMeterState = null;
            LimitedCommodityStateMap simSubjState = null;

            if (_simSubject.isReactsToInputStates()) {
                simSubjState = totalInputStates.get(_simSubject.getId());
            }
            // clone ancillaryMeter if needed
            if (_simSubject.isNeedsAncillaryMeterState()) {
                clonedAncillaryMeterState = ancillaryMeterState.clone();
            }

            _simSubject.setCommodityInputStates(simSubjState, clonedAncillaryMeterState);
        }
    }

    public void doPassiveToActiveExchange(
            AncillaryMeterState ancillaryMeterState,
            List<InterdependentProblemPart<?, ?>> activeParts,
            Set<UUID> activeNodes,
            UUIDCommodityMap passiveStates) {

        // input states
        UUIDCommodityMap totalInputStates = this.getP2AInputStateMap();

        //dont do calculation for electrical grid atm, because only voltage is exchanged which has no influence
        //TODO: as soon as voltage etc. becomes important uncomment first row
//		for (EnergyGrid grid : allGrids) {
        for (EnergyGrid grid : this.thermalGrids) {
            grid.doPassiveToActiveCalculation(
                    activeNodes,
                    passiveStates,
                    totalInputStates);
        }


        // inform subjects about states
        for (InterdependentProblemPart<?, ?> _simSubject : activeParts) {
            LimitedCommodityStateMap simSubjState = null;
            AncillaryMeterState clonedAncillaryMeterState = null;

            if (_simSubject.isReactsToInputStates()) {
                simSubjState = totalInputStates.get(_simSubject.getId());
            }

            // clone AncillaryMeter AncillaryCommodities if needed
            if (_simSubject.isNeedsAncillaryMeterState()) {
                clonedAncillaryMeterState = ancillaryMeterState.clone();
            }

            _simSubject.setCommodityInputStates(simSubjState, clonedAncillaryMeterState);
        }
    }

    public void splitActivePassive(
            Set<UUID> allParts,
            Set<UUID> activeParts,
            Set<UUID> passiveParts) {
        Set<UUID> allActive = new HashSet<>();
        Set<UUID> allPassive = new HashSet<>();

        for (EnergyGrid grid : this.grids.values()) {
            allActive.addAll(grid.getActiveUUIDs());
            allPassive.addAll(grid.getPassiveUUIDs());
        }

        // sanity check
        if (!Collections.disjoint(allActive, allPassive))
            throw new IllegalArgumentException("Some Parts are categorized as active and passive");

        activeParts.addAll(allParts);
        passiveParts.addAll(allParts);

        activeParts.removeAll(allPassive);
        passiveParts.removeAll(allActive);

        allParts.removeAll(allActive);
        allParts.removeAll(allPassive);

        // sanity check
        if (!allParts.isEmpty())
            throw new IllegalArgumentException("Some Parts could not be categorized as active or passive");
    }
}
