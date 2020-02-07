package osh.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.system.GridConfig;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.grid.EnergySimulationTypes;
import osh.esc.grid.IEnergyGrid;

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
    private IEnergyGrid[] allGrids;
    private IEnergyGrid[] thermalGrids;

    /**
     * CONSTRUCTOR
     */
    public OCEnergySimulationCore(
            List<GridConfig> grids,
            String meterUUID) throws HALManagerException {
        super(grids, meterUUID);

        this.allGrids = new IEnergyGrid[this.grids.size()];
        this.allGrids = this.grids.values().toArray(this.allGrids);
        this.thermalGrids = new IEnergyGrid[1];
        this.thermalGrids[0] = this.grids.get(EnergySimulationTypes.THERMAL);
    }

    /**
     * CONSTRUCTOR
     */
    public OCEnergySimulationCore(
            Map<EnergySimulationTypes, IEnergyGrid> grids,
            UUID meterUUID) {
        super(grids, meterUUID);

        this.allGrids = new IEnergyGrid[this.grids.size()];
        this.allGrids = this.grids.values().toArray(this.allGrids);
        this.thermalGrids = new IEnergyGrid[1];
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


        for (Entry<EnergySimulationTypes, IEnergyGrid> grid : this.grids.entrySet()) {
            grid.getValue().initializeGrid(allActiveNodes, activeNeedsInputNodes, passiveNodes, uuidToIntMapWithMeter
                    , uuidOutputMapWithMeter, uuidInputMapWithMeter);
        }

        ObjectOpenHashSet<UUID> passiveWithMeter = new ObjectOpenHashSet<>(passiveNodes);
        passiveWithMeter.add(this.meterUUID);

        this.a2pInputStateMap = new UUIDCommodityMap(passiveWithMeter, uuidToIntMapWithMeter, uuidInputMapWithMeter, true);

        this.p2aInputStateMap = new UUIDCommodityMap(activeNeedsInputNodes, uuidToIntMap, uuidInputMap, true);
    }

    public void finalizeGrids() {
        for (Entry<EnergySimulationTypes, IEnergyGrid> grid : this.grids.entrySet()) {
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
            InterdependentProblemPart<?, ?>[] passiveParts,
            AncillaryMeterState ancillaryMeterState) {

        // input states
        UUIDCommodityMap totalInputStates = this.getA2PInputStateMap();

        //reset meter
        ancillaryMeterState.clear();

        for (IEnergyGrid grid : this.allGrids) {
            grid.doActiveToPassiveCalculation(
                    activeCommodityStates,
                    totalInputStates,
                    ancillaryMeterState);
        }

        // inform subjects about states
        for (InterdependentProblemPart<?, ?> _simSubject : passiveParts) {
            LimitedCommodityStateMap simSubjState = null;

            if (_simSubject.isReactsToInputStates()) {
                simSubjState = totalInputStates.get(_simSubject.getId());
            }

            _simSubject.setCommodityInputStates(simSubjState, ancillaryMeterState);
        }
    }

    public void doPassiveToActiveExchange(
            AncillaryMeterState ancillaryMeterState,
            InterdependentProblemPart<?, ?>[] activeParts,
            UUIDCommodityMap passiveStates) {

        // input states
        UUIDCommodityMap totalInputStates = this.getP2AInputStateMap();

        //dont do calculation for electrical grid atm, because only voltage is exchanged which has no influence
        //TODO: as soon as voltage etc. becomes important uncomment first row
//		for (EnergyGrid grid : allGrids) {
        for (IEnergyGrid grid : this.thermalGrids) {
            grid.doPassiveToActiveCalculation(
                    passiveStates,
                    totalInputStates);
        }


        // inform subjects about states
        for (InterdependentProblemPart<?, ?> _simSubject : activeParts) {
            LimitedCommodityStateMap simSubjState = null;

            if (_simSubject.isReactsToInputStates()) {
                simSubjState = totalInputStates.get(_simSubject.getId());
            }

            _simSubject.setCommodityInputStates(simSubjState, ancillaryMeterState);
        }
    }

    public void splitActivePassive(
            Set<UUID> allParts,
            Set<UUID> activeParts,
            Set<UUID> passiveParts) {
        Set<UUID> allActive = new HashSet<>();
        Set<UUID> allPassive = new HashSet<>();

        for (IEnergyGrid grid : this.grids.values()) {
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
