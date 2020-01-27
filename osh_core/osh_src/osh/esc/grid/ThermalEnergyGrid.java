package osh.esc.grid;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.grid.GridLayout;
import osh.configuration.grid.LayoutConnection;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;
import osh.esc.grid.carrier.Thermal;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ThermalEnergyGrid implements EnergyGrid, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6032713739337800610L;

    private final Set<EnergySourceSink> sourceSinkList = new HashSet<>();

    private final Set<UUID> meterUUIDs = new ObjectOpenHashSet<>();

    private final List<EnergyRelation<Thermal>> relationList = new ObjectArrayList<>();
    private final Set<UUID> activeUUIDs = new ObjectOpenHashSet<>();
    private final Set<UUID> passiveUUIDs = new ObjectOpenHashSet<>();
    private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;
    private InitializedEnergyRelation[] initializedImprovedPassiveToActiveArray;
    private boolean hasBeenInitialized;
    private boolean isSingular;
    private int singularMeter = -1;

    public ThermalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

        Object unmarshalled = XMLSerialization.file2Unmarshal(layoutFilePath, GridLayout.class);

        if (unmarshalled instanceof GridLayout) {
            GridLayout layout = (GridLayout) unmarshalled;

            for (LayoutConnection conn : layout.getConnections()) {
                EnergySourceSink act = new EnergySourceSink(UUID.fromString(conn.getActiveEntityUUID()));
                EnergySourceSink pass = new EnergySourceSink(UUID.fromString(conn.getPassiveEntityUUID()));

                this.activeUUIDs.add(UUID.fromString(conn.getActiveEntityUUID()));
                this.passiveUUIDs.add(UUID.fromString(conn.getPassiveEntityUUID()));

                this.sourceSinkList.add(act);
                this.sourceSinkList.add(pass);

                this.relationList.add(new EnergyRelation<>(act, pass,
                        new Thermal(Commodity.fromString(conn.getActiveToPassiveCommodity())),
                        new Thermal(Commodity.fromString(conn.getPassiveToActiveCommodity()))));
            }

            for (String uuid : layout.getMeterUUIDs()) {
                this.meterUUIDs.add(UUID.fromString(uuid));
            }

        } else
            throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

        //sanity
        if (!Collections.disjoint(this.activeUUIDs, this.passiveUUIDs))
            throw new IllegalArgumentException("Same UUID is active and passive");
    }

    /**
     * only for serialisation, do not use normally
     */
    @Deprecated
    protected ThermalEnergyGrid() {
        // NOTHING
    }

    @Override
    public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                               Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap) {

        List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList = new ObjectArrayList<>();
        List<InitializedEnergyRelation> initializedImprovedPassiveToActiveList = new ObjectArrayList<>();
        Map<UUID, InitializedEnergyRelation> tempA2PHelpMap = new Object2ObjectOpenHashMap<>();
        Map<UUID, InitializedEnergyRelation> tempP2AHelpMap = new Object2ObjectOpenHashMap<>();

        for (EnergyRelation<Thermal> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            boolean activeTypeNI = activeNeedsInputNodes.contains(activeId);
            boolean activeType = allActiveNodes.contains(activeId);
            boolean passiveType = passiveNodes.contains(passiveId);
            boolean isMeter = this.meterUUIDs.contains(passiveId);

            //if both exist and an exchange should be made add to the respective lists
            if (activeType && (passiveType || isMeter) && uuidOutputMap.get(activeId).contains(rel.getActiveToPassive().getCommodity())) {

                InitializedEnergyRelation relNew = tempA2PHelpMap.get(activeId);

                if (relNew == null) {
                    relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(activeId), new ObjectArrayList<>());
                    tempA2PHelpMap.put(activeId, relNew);
                }

                relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
            }
            if (activeTypeNI && passiveType && uuidOutputMap.get(passiveId).contains(rel.getPassiveToActive().getCommodity())) {

                InitializedEnergyRelation relNew = tempP2AHelpMap.get(passiveId);

                if (relNew == null) {
                    relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(passiveId), new ObjectArrayList<>());
                    tempP2AHelpMap.put(passiveId, relNew);
                }

                relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(activeId), rel.getPassiveToActive().getCommodity()));
            }
        }

        initializedImprovedActiveToPassiveList.addAll(tempA2PHelpMap.values());
        initializedImprovedActiveToPassiveList.forEach(InitializedEnergyRelation::transformToArrayTargets);

        this.initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
        this.initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(this.initializedImprovedActiveToPassiveArray);

        initializedImprovedPassiveToActiveList.addAll(tempP2AHelpMap.values());
        initializedImprovedPassiveToActiveList.forEach(InitializedEnergyRelation::transformToArrayTargets);

        this.initializedImprovedPassiveToActiveArray = new InitializedEnergyRelation[initializedImprovedPassiveToActiveList.size()];
        this.initializedImprovedPassiveToActiveArray = initializedImprovedPassiveToActiveList.toArray(this.initializedImprovedPassiveToActiveArray);

        this.hasBeenInitialized = true;

        if (this.meterUUIDs.size() == 1) {
            this.isSingular = true;
            this.singularMeter = uuidToIntMap.getInt(this.meterUUIDs.iterator().next());
        }
    }

    @Override
    public void finalizeGrid() {
        this.hasBeenInitialized = false;

        this.isSingular = false;
        this.singularMeter = -1;
    }

    @Override
    public void doCalculation(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        for (EnergyRelation<Thermal> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            if (localCommodityStates.containsKey(activeId)
                    || localCommodityStates.containsKey(passiveId)) {

                Commodity activeCommodity = rel.getActiveToPassive().getCommodity();
                Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

                LimitedCommodityStateMap activeLocalCommodities = localCommodityStates.get(activeId);
                LimitedCommodityStateMap passiveLocalCommodities = localCommodityStates.get(passiveId);

                boolean hasActive = activeLocalCommodities != null && activeLocalCommodities.containsCommodity(activeCommodity);
                boolean hasPassive = passiveLocalCommodities != null && passiveLocalCommodities.containsCommodity(passiveCommodity);

                if (!hasActive && !hasPassive) {
                    continue;
                }

                // update active part...
                if (hasPassive) {
                    // Active Part has no input state power

                    LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
                    if (activeMap == null) {
                        activeMap = new LimitedCommodityStateMap();
                        totalInputStates.put(activeId, activeMap);
                    }

                    this.updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
                    //TODO mass flow


                    // do not consider power: active part determines it's own power
                }

                // update passive part...
                if (hasActive) {
                    LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
                    if (passiveMap == null) {
                        passiveMap = new LimitedCommodityStateMap();
                        totalInputStates.put(passiveId, passiveMap);
                    }

                    this.updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
                }
            }
        }
        // calculate ancillary states
        this.calculateMeter(totalInputStates, ancillaryMeterState);
    }

    private void calculateMeter(
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        if (this.hasBeenInitialized && this.isSingular) {
            this.calculateInitializedMeter(totalInputStates, ancillaryMeterState);
        } else {
            this.calculateMeterAll(totalInputStates, ancillaryMeterState);
        }
    }

    private void calculateInitializedMeter(
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        if (ancillaryMeterState != null) {
            // ancillary THERMAL calculation
            ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL, totalInputStates.get(this.singularMeter).getPower(Commodity.NATURALGASPOWER));
        }
    }

    private void calculateMeter(
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        if (ancillaryMeterState != null) {
            for (UUID meter : this.meterUUIDs) {
                // ancillary THERMAL calculation
                LimitedCommodityStateMap calculatedMeterState = totalInputStates.get(meter);

                // Gas Power
                {
                    if (calculatedMeterState == null) {
//						System.out.println("Probably no heating device in configuration!");
                    } else {
                        ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL,
                                calculatedMeterState.getPower(Commodity.NATURALGASPOWER));
                    }
                }
            }
        }
    }

    private void calculateMeterAll(
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        if (ancillaryMeterState != null) {
            for (UUID meter : this.meterUUIDs) {
                // ancillary THERMAL calculation
                LimitedCommodityStateMap calculatedMeterState = totalInputStates.get(meter);

                // Gas Power
                {
                    if (calculatedMeterState == null) {
//						System.out.println("Probably no heating device in configuration!");
                    } else if (calculatedMeterState.containsCommodity(Commodity.NATURALGASPOWER)) {

                        ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL,
                                calculatedMeterState.getPower(Commodity.NATURALGASPOWER));
                    }
                }
            }
        }
    }

    @Override
    public Set<UUID> getMeterUUIDs() {
        return this.meterUUIDs;
    }

    @Override
    public Set<UUID> getActiveUUIDs() {
        return this.activeUUIDs;
    }

    @Override
    public Set<UUID> getPassiveUUIDs() {
        return this.passiveUUIDs;
    }

    @Override
    public void doActiveToPassiveCalculation(
            Set<UUID> passiveNodes,
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        if (this.hasBeenInitialized) {
            this.doInitializedActiveToPassiveGridCalculation(passiveNodes, activeStates, totalInputStates, ancillaryMeterState);
        } else {

            for (EnergyRelation<Thermal> rel : this.relationList) {

                UUID activeId = rel.getActiveEntity().getDeviceUuid();
                UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

                int activeType = 2;
                int passiveType = 2;

                if (!this.hasBeenInitialized) {
                    activeType = activeStates.containsKey(activeId) ? 2 : 0;
                    passiveType = passiveNodes.contains(passiveId) ? 2 : 0;

                    if (this.meterUUIDs.contains(passiveId))
                        passiveType = 2;
                    if (this.meterUUIDs.contains(activeId))
                        activeType = 2;
                }

                //if sum of types > 2 both exists and an exchange should be made
                if (activeType + passiveType > 2) {

                    Commodity activeCommodity = rel.getActiveToPassive().getCommodity();

                    LimitedCommodityStateMap activeLocalCommodities = activeStates.get(activeId);

                    if (activeLocalCommodities == null || !activeLocalCommodities.containsCommodity(activeCommodity)) {
                        continue;
                    }

                    // update passive part...
                    LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
                    if (passiveMap == null) {
                        passiveMap = new LimitedCommodityStateMap();
                        totalInputStates.put(passiveId, passiveMap);
                    }
                    this.updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
                }
            }

            this.calculateMeter(totalInputStates, ancillaryMeterState);
        }
    }

    private void doInitializedActiveToPassiveGridCalculation(
            Set<UUID> passiveNodes,
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        for (InitializedEnergyRelation rel : this.initializedImprovedActiveToPassiveArray) {

            LimitedCommodityStateMap activeLocalCommodities = activeStates.get(rel.getSourceId());

            if (activeLocalCommodities != null) {


                for (InitializedEnergyRelationTarget target : rel.getTargets()) {

                    // update passive part...
                    if (activeLocalCommodities.containsCommodity(target.getCommodity())) {
                        this.updatePassivePart(totalInputStates.get(target.getTargetID()), activeLocalCommodities, target.getCommodity());
                    }
                }
            }
        }
        this.calculateInitializedMeter(totalInputStates, ancillaryMeterState);
    }

    @Override
    public void doPassiveToActiveCalculation(
            Set<UUID> activeNodes,
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates) {

        if (this.hasBeenInitialized) {
            this.doInitializedPassiveToActiveGridCalculation(passiveStates, activeNodes, totalInputStates);
        } else {

            for (EnergyRelation<Thermal> rel : this.relationList) {

                UUID activeId = rel.getActiveEntity().getDeviceUuid();
                UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

                int activeType = activeNodes.contains(activeId) ? 2 : 0;
                int passiveType = passiveStates.containsKey(passiveId) ? 2 : 0;

                if (this.meterUUIDs.contains(passiveId))
                    passiveType = 2;
                if (this.meterUUIDs.contains(activeId))
                    activeType = 2;

                //if sum of types > 2 both exists and an exchange should be made
                if (activeType + passiveType > 2) {

                    Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

                    LimitedCommodityStateMap passiveLocalCommodities = passiveStates.get(passiveId);

                    if (passiveLocalCommodities == null
                            || !passiveLocalCommodities.containsCommodity(passiveCommodity)) {
                        continue;
                    }

                    // update active part...

                    // Active Part has no input state power
                    LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
                    if (activeMap == null) {
                        activeMap = new LimitedCommodityStateMap();
                        totalInputStates.put(activeId, activeMap);
                    }
                    this.updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
                    //TODO mass flow
                    // do not consider power: active part determines it's own power
                }
            }
        }
    }

    private void doInitializedPassiveToActiveGridCalculation(
            UUIDCommodityMap passiveStates,
            Set<UUID> activeNodes,
            UUIDCommodityMap totalInputStates) {

        for (InitializedEnergyRelation rel : this.initializedImprovedPassiveToActiveArray) {

            LimitedCommodityStateMap passiveMap = passiveStates.get(rel.getSourceId());

            if (passiveMap != null) {


                for (InitializedEnergyRelationTarget target : rel.getTargets()) {

                    // update passive part...
                    if (passiveMap.containsCommodity(target.getCommodity())) {
                        this.updateActivePart(totalInputStates.get(target.getTargetID()), passiveMap, target.getCommodity());
                    }
                }
            }
        }
    }

    private void updatePassivePart(
            LimitedCommodityStateMap passiveMap,
            LimitedCommodityStateMap activeMap,
            Commodity activeCommodity) {
        passiveMap.setOrAddPower(activeCommodity, activeMap.getPowerWithoutCheck(activeCommodity));
        passiveMap.setTemperature(activeCommodity, activeMap.getTemperatureWithoutCheck(activeCommodity));
    }

    private void updateActivePart(
            LimitedCommodityStateMap activeMap,
            LimitedCommodityStateMap passiveMap,
            Commodity passiveCommodity) {

        activeMap.setTemperature(passiveCommodity, passiveMap.getTemperatureWithoutCheck(passiveCommodity));
    }
}
