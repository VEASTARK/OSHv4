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
import osh.esc.grid.carrier.ThermalConnection;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ThermalEnergyGrid implements IEnergyGrid {

    //meter
    private final UUID meterUUID;
    private int meterId;
    //energy-relations
    private final List<EnergyRelation<ThermalConnection>> relationList;
    private final Set<UUID> activeUUIDs;
    private final Set<UUID> passiveUUIDs;
    //initialized and improved (shortened) energy relations
    private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;
    private InitializedEnergyRelation[] initializedImprovedPassiveToActiveArray;
    private boolean hasBeenInitialized;
    //re-useable storage to prevent excessive memory operations
    private double productionPower;

    public ThermalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

        this.relationList = new ObjectArrayList<>();
        this.activeUUIDs = new ObjectOpenHashSet<>();
        this.passiveUUIDs = new ObjectOpenHashSet<>();

        Object unmarshalled = XMLSerialization.file2Unmarshal(layoutFilePath, GridLayout.class);

        if (unmarshalled instanceof GridLayout) {
            GridLayout layout = (GridLayout) unmarshalled;

            for (LayoutConnection conn : layout.getConnections()) {
                EnergySourceSink act = new EnergySourceSink(UUID.fromString(conn.getActiveEntityUUID()));
                EnergySourceSink pass = new EnergySourceSink(UUID.fromString(conn.getPassiveEntityUUID()));

                this.activeUUIDs.add(UUID.fromString(conn.getActiveEntityUUID()));
                this.passiveUUIDs.add(UUID.fromString(conn.getPassiveEntityUUID()));

                this.relationList.add(new EnergyRelation<>(act, pass,
                        new ThermalConnection(Commodity.fromString(conn.getActiveToPassiveCommodity())),
                        new ThermalConnection(Commodity.fromString(conn.getPassiveToActiveCommodity()))));
            }
            this.meterUUID = UUID.fromString(layout.getSuperMeterUUID());

        } else
            throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

        //sanity
        if (!Collections.disjoint(this.activeUUIDs, this.passiveUUIDs))
            throw new IllegalArgumentException("Same UUID is active and passive");
    }

    public ThermalEnergyGrid(ThermalEnergyGrid other) {
        this.meterUUID = other.meterUUID;
        this.meterId = other.meterId;
        this.relationList = other.relationList;
        this.activeUUIDs = other.activeUUIDs;
        this.passiveUUIDs = other.passiveUUIDs;
        this.initializedImprovedActiveToPassiveArray = other.initializedImprovedActiveToPassiveArray;
        this.initializedImprovedPassiveToActiveArray = other.initializedImprovedPassiveToActiveArray;
        this.hasBeenInitialized = other.hasBeenInitialized;
        this.productionPower = 0;
    }

    @Override
    public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                               Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap) {

        List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList = new ObjectArrayList<>();
        List<InitializedEnergyRelation> initializedImprovedPassiveToActiveList = new ObjectArrayList<>();
        Map<UUID, List<InitializedEnergyRelationTarget>> tempA2PHelpMap = new Object2ObjectOpenHashMap<>();
        Map<UUID, List<InitializedEnergyRelationTarget>> tempP2AHelpMap = new Object2ObjectOpenHashMap<>();

        for (EnergyRelation<ThermalConnection> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            boolean activeTypeNI = activeNeedsInputNodes.contains(activeId);
            boolean activeType = allActiveNodes.contains(activeId);
            boolean passiveType = passiveNodes.contains(passiveId);

            //if both exist and an exchange should be made add to the respective lists
            if (activeType && (passiveType || passiveId.equals(this.meterUUID))
                    && uuidOutputMap.get(activeId).contains(rel.getActiveToPassive().getCommodity())
                    && uuidInputMap.get(passiveId).contains(rel.getActiveToPassive().getCommodity())) {

                List<InitializedEnergyRelationTarget> targets = tempA2PHelpMap.computeIfAbsent(activeId,
                        k -> new ObjectArrayList<>());

                targets.add(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
            }
            if (activeTypeNI && passiveType
                    && uuidOutputMap.get(activeId).contains(rel.getPassiveToActive().getCommodity())
                    && uuidInputMap.get(passiveId).contains(rel.getPassiveToActive().getCommodity())) {

                List<InitializedEnergyRelationTarget> targets = tempP2AHelpMap.computeIfAbsent(passiveId,
                        k -> new ObjectArrayList<>());

                targets.add(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(activeId), rel.getPassiveToActive().getCommodity()));
            }
        }

        tempA2PHelpMap.forEach((k, v) -> initializedImprovedActiveToPassiveList.add(new InitializedEnergyRelation(uuidToIntMap.getInt(k), v)));
        tempP2AHelpMap.forEach((k, v) -> initializedImprovedPassiveToActiveList.add(new InitializedEnergyRelation(uuidToIntMap.getInt(k), v)));

        this.initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
        this.initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(this.initializedImprovedActiveToPassiveArray);

        this.initializedImprovedPassiveToActiveArray = new InitializedEnergyRelation[initializedImprovedPassiveToActiveList.size()];
        this.initializedImprovedPassiveToActiveArray = initializedImprovedPassiveToActiveList.toArray(this.initializedImprovedPassiveToActiveArray);

        this.meterId = uuidToIntMap.getInt(this.meterUUID);

        this.hasBeenInitialized = true;
    }

    @Override
    public void finalizeGrid() {
        this.hasBeenInitialized = false;
        this.meterId = -1;
    }

    @Override
    public void doCalculation(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        GridUtils.doCalculation(
                localCommodityStates,
                totalInputStates,
                ancillaryMeterState,
                this.relationList,
                this::updateActivePart,
                this::updatePassivePart,
                this::calculateMeter);
    }

    @Override
    public void doActiveToPassiveCalculation(
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
        this.populateMeter(activeStates, totalInputStates, ancillaryMeterState);
    }

    @Override
    public void doPassiveToActiveCalculation(
            UUIDCommodityMap passiveStates,
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

    private void calculateMeter(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        if (ancillaryMeterState != null) {

            LimitedCommodityStateMap calculatedMeterState = totalInputStates.get(this.meterUUID);

            if (calculatedMeterState != null) {
                ancillaryMeterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL,
                        calculatedMeterState.getPower(Commodity.NATURALGASPOWER));
            }
        }
    }

    private void populateMeter(UUIDCommodityMap localCommodityStates, UUIDCommodityMap totalInputStates, AncillaryMeterState meterState) {

        this.calcPowers(localCommodityStates, totalInputStates.get(this.meterId));
        this.populateMainPowers(meterState, this.productionPower);
    }

    private void calcPowers(UUIDCommodityMap localCommodityStates, LimitedCommodityStateMap superMeterMap) {
        this.productionPower = superMeterMap.getPower(Commodity.NATURALGASPOWER);
    }

    private void populateMainPowers(AncillaryMeterState meterState, double meterPower) {
        if (meterPower > 0) {
            meterState.setPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL, meterPower);
        }
    }

    @Override
    public UUID getMeterUUID() {
        return this.meterUUID;
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
    public ThermalEnergyGrid getClone() {
        return new ThermalEnergyGrid(this);
    }
}
