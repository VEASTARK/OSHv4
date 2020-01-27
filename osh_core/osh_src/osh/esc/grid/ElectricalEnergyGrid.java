package osh.esc.grid;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import osh.configuration.grid.DevicePerMeter;
import osh.configuration.grid.GridLayout;
import osh.configuration.grid.LayoutConnection;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;
import osh.esc.grid.carrier.Electrical;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ElectricalEnergyGrid implements EnergyGrid, Serializable {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 420822007199349391L;

    private final Set<EnergySourceSink> sourceSinkList = new ObjectOpenHashSet<>();

    private final Set<UUID> meterUUIDs = new ObjectOpenHashSet<>();

    private final List<EnergyRelation<Electrical>> relationList = new ObjectArrayList<>();
    private final Set<UUID> activeUUIDs = new ObjectOpenHashSet<>();
    private final Set<UUID> passiveUUIDs = new ObjectOpenHashSet<>();
    private final Map<UUID, Map<String, Set<UUID>>> devicesByTypePerMeter = new Object2ObjectOpenHashMap<>();
    private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;
    private List<EnergyRelation<Electrical>> initializedPassiveToActiveRelationList = new ObjectArrayList<>();
    private boolean isSingular;
    private int singularMeter = -1;
    private int singularPvDevice = -1;
    private int singularChpDevice = -1;
    private int singularBatDevice = -1;
    private boolean hasBeenInitialized;

    //not needed at the moment, but could be useful in the future
    //	private final Map<UUID, Set<UUID>> devicesPerMeter = new HashMap<UUID, Set<UUID>>();
    private boolean hasBat = true, hasPV = true, hasCHP = true;

    public ElectricalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

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
                        new Electrical(Commodity.fromString(conn.getActiveToPassiveCommodity())),
                        new Electrical(Commodity.fromString(conn.getPassiveToActiveCommodity()))));
            }

            for (String uuid : layout.getMeterUUIDs()) {
                this.meterUUIDs.add(UUID.fromString(uuid));
            }

            for (DevicePerMeter dev : layout.getDeviceMeterMap()) {

                UUID meter = UUID.fromString(dev.getMeterUUID());
                UUID device = UUID.fromString(dev.getDeviceUUID());

                //				Set<UUID> deviceSet = devicesPerMeter.get(meter);
                //				if (deviceSet == null) {
                //					deviceSet = new HashSet<UUID>();
                //					devicesPerMeter.put(meter, deviceSet);
                //				}
                //				deviceSet.add(device);

                Map<String, Set<UUID>> deviceTypeMap = this.devicesByTypePerMeter.get(meter);
                if (deviceTypeMap == null) {
                    deviceTypeMap = new Object2ObjectOpenHashMap<>();
                    this.devicesByTypePerMeter.put(meter, deviceTypeMap);
                }
                Set<UUID> devicesByType = deviceTypeMap.get(dev.getDeviceType());
                if (devicesByType == null) {
                    devicesByType = new ObjectOpenHashSet<>();
                    deviceTypeMap.put(dev.getDeviceType(), devicesByType);
                }
                devicesByType.add(device);
            }

        } else
            throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

        //sanity check
        if (!Collections.disjoint(this.activeUUIDs, this.passiveUUIDs))
            throw new IllegalArgumentException("Same UUID is active and passive");
    }

    /**
     * only for serialisation, do not use normally
     */
    @Deprecated
    protected ElectricalEnergyGrid() {
    }

    @Override
    public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                               Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap) {


        this.initializedPassiveToActiveRelationList = new ObjectArrayList<>();
        List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList;
        Map<UUID, InitializedEnergyRelation> tempHelpMap = new Object2ObjectOpenHashMap<>();

        for (EnergyRelation<Electrical> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            boolean activeTypeNI = activeNeedsInputNodes.contains(activeId);
            boolean activeType = allActiveNodes.contains(activeId);
            boolean passiveType = passiveNodes.contains(passiveId);
            boolean isMeter = this.meterUUIDs.contains(passiveId);

            //if both exist and the device really puts out the commodity add to the respective lists
            if (activeType && (passiveType || isMeter) && uuidOutputMap.get(activeId).contains(rel.getActiveToPassive().getCommodity())) {
                InitializedEnergyRelation relNew = tempHelpMap.get(activeId);

                if (relNew == null) {
                    relNew = new InitializedEnergyRelation(uuidToIntMap.getInt(activeId), new ObjectArrayList<>());
                    tempHelpMap.put(activeId, relNew);
                }

                relNew.addEnergyTarget(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
            }
            if (activeTypeNI && passiveType)
                this.initializedPassiveToActiveRelationList.add(rel);
        }

        initializedImprovedActiveToPassiveList = new ObjectArrayList<>(tempHelpMap.values());
        initializedImprovedActiveToPassiveList.forEach(InitializedEnergyRelation::transformToArrayTargets);

        this.initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
        this.initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(this.initializedImprovedActiveToPassiveArray);

        if (this.meterUUIDs.size() == 1) {
            UUID singularMeterUUID = this.meterUUIDs.iterator().next();
            this.singularMeter = uuidToIntMap.getInt(singularMeterUUID);

            if (this.devicesByTypePerMeter.get(singularMeterUUID).get("pv").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1
                    && this.devicesByTypePerMeter.get(singularMeterUUID).get("chp").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1
                    && this.devicesByTypePerMeter.get(singularMeterUUID).get("battery").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1) {

                this.isSingular = true;
                this.singularPvDevice = uuidToIntMap.getInt(this.devicesByTypePerMeter.get(singularMeterUUID).get("pv").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
                this.singularChpDevice = uuidToIntMap.getInt(this.devicesByTypePerMeter.get(singularMeterUUID).get("chp").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
                this.singularBatDevice = uuidToIntMap.getInt(this.devicesByTypePerMeter.get(singularMeterUUID).get("battery").stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));

            }
        }

        this.hasBat = false;
        this.hasPV = false;
        this.hasCHP = false;

        for (UUID meter : this.meterUUIDs) {

            Map<String, Set<UUID>> meterDevices = this.devicesByTypePerMeter.get(meter);

            for (UUID pv : meterDevices.get("pv")) {
                if (allActiveNodes.contains(pv) || passiveNodes.contains(pv)) {
                    this.hasPV = true;
                    break;
                }
            }

            for (UUID chp : meterDevices.get("chp")) {
                if (allActiveNodes.contains(chp) || passiveNodes.contains(chp)) {
                    this.hasCHP = true;
                    break;
                }
            }

            for (UUID battery : meterDevices.get("battery")) {
                if (allActiveNodes.contains(battery) || passiveNodes.contains(battery)) {
                    this.hasBat = true;
                    break;
                }
            }
        }

        this.hasBeenInitialized = true;
    }

    @Override
    public void finalizeGrid() {
        this.hasBeenInitialized = false;
        this.initializedPassiveToActiveRelationList = null;

        this.singularMeter = -1;
        this.singularPvDevice = -1;
        this.singularChpDevice = -1;
        this.singularBatDevice = -1;

        this.isSingular = false;
        this.hasBat = true;
        this.hasPV = true;
        this.hasCHP = true;
    }

    @Override
    public void doCalculation(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        for (EnergyRelation<Electrical> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            if (localCommodityStates.containsKey(activeId)
                    || localCommodityStates.containsKey(passiveId)) {

                Commodity activeCommodity = rel.getActiveToPassive().getCommodity();
                Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

                LimitedCommodityStateMap activeLocalCommodities = localCommodityStates.get(activeId);
                LimitedCommodityStateMap passiveLocalCommodities = localCommodityStates.get(passiveId);

                if (!activeLocalCommodities.containsCommodity(activeCommodity)) {
                    continue;
                }

                // update active part...
                {
                    // Active Part has no input state power

                    LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
                    if (activeMap == null) {
                        activeMap = new LimitedCommodityStateMap();
                        totalInputStates.put(activeId, activeMap);
                    }

                    this.updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);

                    // do not consider power: active part determines it's own power
                }

                // update passive part...
                {
                    LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
                    if (passiveMap == null) {
                        passiveMap = new LimitedCommodityStateMap();
                        totalInputStates.put(passiveId, passiveMap);
                    }

                    this.updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
                }
            }
        }

        this.calculateMeter(localCommodityStates, totalInputStates, ancillaryMeterState);
    }

    @Override
    public void doActiveToPassiveCalculation(
            Set<UUID> passiveNodes,
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {


        if (this.hasBeenInitialized) {
            this.doInitializedActiveToPassiveCalculation(activeStates, totalInputStates, ancillaryMeterState);
        } else {

            for (EnergyRelation<Electrical> rel : this.relationList) {

                UUID activeId = rel.getActiveEntity().getDeviceUuid();
                UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

                boolean doSthActive = true;
                boolean doSthPassive = true;

                if (!this.hasBeenInitialized) {
                    doSthActive = activeStates.containsKey(activeId);
                    doSthPassive = passiveNodes.contains(passiveId);

                    if (!doSthPassive && this.meterUUIDs.contains(passiveId))
                        doSthPassive = true;
                    if (!doSthActive && this.meterUUIDs.contains(activeId))
                        doSthActive = true;
                }

                //if sum of types > 2 both exists and an exchange should be made
                if (doSthActive && doSthPassive) {

                    Commodity activeCommodity = rel.getActiveToPassive().getCommodity();

                    LimitedCommodityStateMap activeLocalCommodities = activeStates.get(activeId);

                    if (!activeLocalCommodities.containsCommodity(activeCommodity))
                        continue;

                    // update passive part...
                    LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
                    if (passiveMap == null) {
//						passiveMap = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
                        passiveMap = new LimitedCommodityStateMap();
                        totalInputStates.put(passiveId, passiveMap);
                    }
                    this.updatePassivePart(passiveMap, activeLocalCommodities, activeCommodity);
                }
            }

            this.calculateMeter(activeStates, totalInputStates, ancillaryMeterState);
        }
    }

    @Override
    public void doPassiveToActiveCalculation(
            Set<UUID> activeNodes,
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates) {


        if (this.hasBeenInitialized) {
            this.doInitializedPassiveToActiveCalculation(activeNodes, passiveStates, totalInputStates);
        } else {
            for (EnergyRelation<Electrical> rel : this.relationList) {

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

                    if (!passiveLocalCommodities.containsCommodity(passiveCommodity))
                        continue;

                    // update active part...

                    // Active Part has no input state power
                    LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
                    if (activeMap == null) {
                        activeMap = new LimitedCommodityStateMap();
                        totalInputStates.put(activeId, activeMap);
                    }
                    this.updateActivePart(activeMap, passiveLocalCommodities, passiveCommodity);
                    // do not consider power: active part determines it's own power
                }
            }
        }
    }

    private void doInitializedActiveToPassiveCalculation(
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        for (InitializedEnergyRelation rel : this.initializedImprovedActiveToPassiveArray) {

            LimitedCommodityStateMap activeLocalCommodities = activeStates.get(rel.getSourceId());

            if (activeLocalCommodities == null) {
                continue;
            }

            for (InitializedEnergyRelationTarget target : rel.getTargets()) {

                // update passive part... (if possible

                if (activeLocalCommodities.containsCommodity(target.getCommodity())) {
                    this.updatePassivePart(totalInputStates.get(target.getTargetID()), activeLocalCommodities, target.getCommodity());
                }
            }
        }

        this.calculateMeter(activeStates, totalInputStates, ancillaryMeterState);
    }

    private void doInitializedPassiveToActiveCalculation(
            Set<UUID> activeNodes,
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates) {


        for (EnergyRelation<Electrical> rel : this.initializedPassiveToActiveRelationList) {

            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();
            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

            LimitedCommodityStateMap passiveLocalCommodities = passiveStates.get(passiveId);

            if (passiveLocalCommodities == null) {
                continue;
            }

            if (!passiveLocalCommodities.containsCommodity(passiveCommodity)) {
                continue;
            }

            // update active part...

            // Active Part has no input state power
            this.updateActivePart(totalInputStates.get(activeId), passiveLocalCommodities, passiveCommodity);
            // do not consider power: active part determines it's own power
        }
    }

    //TODO: if at any time we will have multiple pvs/chps/batteries we have to adjust this so
    // it does not rely on having only a single one of these devices for the sped-up methods
    private void calculateMeter(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        // calculate ancillary states

        if (ancillaryMeterState != null) {

            if (!this.hasBeenInitialized || !this.isSingular) {
                this.calculateMeterAll(localCommodityStates, totalInputStates, ancillaryMeterState);
            } else {
                if (!this.hasBat) {
                    if (this.hasCHP && this.hasPV) {
                        this.calculateMeterNoBat(localCommodityStates, totalInputStates, ancillaryMeterState);
                    } else if (!this.hasCHP && this.hasPV) {
                        this.calculateMeterNoBatNoChp(localCommodityStates, totalInputStates, ancillaryMeterState);
                    } else if (this.hasCHP) {
                        this.calculateMeterNoBatNoPV(localCommodityStates, totalInputStates, ancillaryMeterState);
                    } else {
                        this.calculateMeterNoBatNoChpNoPV(localCommodityStates, totalInputStates, ancillaryMeterState);
                    }
                } else {
                    this.calculateMeterAll(localCommodityStates, totalInputStates, ancillaryMeterState);
                }
            }
        }
    }

    private void calculateMeter(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        Objects.requireNonNull(ancillaryMeterState);

        // calculate ancillary states

        for (UUID meter : this.meterUUIDs) {
            // ancillary ELECTRICAL calculation
            double pvPower = 0;
            double chpPower = 0;
            double batteryPower = 0;

            Map<String, Set<UUID>> meterDevices = this.devicesByTypePerMeter.get(meter);

            for (UUID pv : meterDevices.get("pv")) {
                LimitedCommodityStateMap pvMap = localCommodityStates.get(pv);
                pvPower += pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            for (UUID chp : meterDevices.get("chp")) {
                LimitedCommodityStateMap chpMap = localCommodityStates.get(chp);
                chpPower += chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            for (UUID battery : meterDevices.get("battery")) {
                LimitedCommodityStateMap batteryMap = localCommodityStates.get(battery);
                batteryPower += batteryMap != null ? batteryMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            LimitedCommodityStateMap meterMap = totalInputStates.get(meter);

            double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

            // net consumption
            if (totalPower >= 0) {
                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryPower < 0 ? batteryPower : 0);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryPower >= 0 ? batteryPower : 0);
                ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }
            // net production / feed-in
            else {
                double negBatteryPower = batteryPower < 0 ? batteryPower : 0;
                double totalProduction = pvPower + chpPower + negBatteryPower;

                double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
                double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;
                double shareOfBattery = batteryPower < 0 ? batteryPower / totalProduction : 0;


                double pvExternal = (int) Math.round(shareOfPV * totalPower);
                double pvInternal = pvPower - pvExternal;

                double chpExternal = (int) Math.round(shareOfCHP * totalPower);
                double chpInternal = chpPower - chpExternal;

                double batteryExternal = 0;
                double batteryInternal = 0;
                double batteryConsumption = 0;

                if (batteryPower < 0) {
                    batteryExternal = (int) Math.round(shareOfBattery * totalPower);
                    batteryInternal = batteryPower - batteryExternal;
                } else {
                    batteryConsumption = batteryPower;
                }

                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, batteryExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryConsumption);
                ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }

            // Reactive Power
            if (meterMap.containsCommodity(Commodity.REACTIVEPOWER)) {
                ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
            }
        }
    }

    private void calculateMeterAll(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        Objects.requireNonNull(ancillaryMeterState);

        // calculate ancillary states

        for (UUID meter : this.meterUUIDs) {
            // ancillary ELECTRICAL calculation
            double pvPower = 0;
            double chpPower = 0;
            double batteryPower = 0;

            Map<String, Set<UUID>> meterDevices = this.devicesByTypePerMeter.get(meter);

            for (UUID pv : meterDevices.get("pv")) {
                LimitedCommodityStateMap pvMap = localCommodityStates.get(pv);
                pvPower += pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            for (UUID chp : meterDevices.get("chp")) {
                LimitedCommodityStateMap chpMap = localCommodityStates.get(chp);
                chpPower += chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            for (UUID battery : meterDevices.get("battery")) {
                LimitedCommodityStateMap batteryMap = localCommodityStates.get(battery);
                batteryPower += batteryMap != null ? batteryMap.getPower(Commodity.ACTIVEPOWER) : 0;
            }

            LimitedCommodityStateMap meterMap = totalInputStates.get(meter);

            double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

            // net consumption
            if (totalPower >= 0) {
                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryPower < 0 ? batteryPower : 0);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryPower >= 0 ? batteryPower : 0);
                ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }
            // net production / feed-in
            else {
                double negBatteryPower = batteryPower < 0 ? batteryPower : 0;
                double totalProduction = pvPower + chpPower + negBatteryPower;

                double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
                double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;
                double shareOfBattery = batteryPower < 0 ? batteryPower / totalProduction : 0;


                double pvExternal = (int) Math.round(shareOfPV * totalPower);
                double pvInternal = pvPower - pvExternal;

                double chpExternal = (int) Math.round(shareOfCHP * totalPower);
                double chpInternal = chpPower - chpExternal;

                double batteryExternal = 0;
                double batteryInternal = 0;
                double batteryConsumption = 0;

                if (batteryPower < 0) {
                    batteryExternal = (int) Math.round(shareOfBattery * totalPower);
                    batteryInternal = batteryPower - batteryExternal;
                } else {
                    batteryConsumption = batteryPower;
                }

                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batteryInternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, batteryExternal);
                ancillaryMeterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, batteryConsumption);
                ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }

            // Reactive Power
            if (meterMap.containsCommodity(Commodity.REACTIVEPOWER)) {
                ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
            }
        }
    }

    private void calculateMeterNoBat(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        // ancillary ELECTRICAL calculation

        LimitedCommodityStateMap pvMap = localCommodityStates.get(this.singularPvDevice);
        double pvPower = pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;

        LimitedCommodityStateMap chpMap = localCommodityStates.get(this.singularChpDevice);
        double chpPower = chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.singularMeter);

        double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

        ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);

        // net consumption
        if (totalPower >= 0) {
            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
        }
        // net production / feed-in
        else {
            double totalProduction = pvPower + chpPower;

            double shareOfPV = pvPower < 0 ? pvPower / totalProduction : 0;
            double shareOfCHP = chpPower < 0 ? chpPower / totalProduction : 0;


            double pvExternal = (int) Math.round(shareOfPV * totalPower);
            double pvInternal = pvPower - pvExternal;

            double chpExternal = (int) Math.round(shareOfCHP * totalPower);
            double chpInternal = chpPower - chpExternal;

            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
        }

        // Reactive Power
        ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
    }

    private void calculateMeterNoBatNoPV(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        LimitedCommodityStateMap chpMap = localCommodityStates.get(this.singularChpDevice);
        double chpPower = chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.singularMeter);

        double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

        ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);

        if (totalPower >= 0) {
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
        }
        // net production / feed-in
        else {
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, (chpPower - totalPower));
            ancillaryMeterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, totalPower);
        }

        ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
    }

    private void calculateMeterNoBatNoChp(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        LimitedCommodityStateMap pvMap = localCommodityStates.get(this.singularPvDevice);
        double pvPower = pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.singularMeter);

        double totalPower = meterMap.getPower(Commodity.ACTIVEPOWER);

        ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);

        if (totalPower >= 0) {
            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
        }
        // net production / feed-in
        else {
            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, (pvPower - totalPower));
            ancillaryMeterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, totalPower);
        }

        ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
    }

    private void calculateMeterNoBatNoChpNoPV(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.singularMeter);

        ancillaryMeterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.ACTIVEPOWER));
        ancillaryMeterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, meterMap.getPower(Commodity.REACTIVEPOWER));
    }

    private void updatePassivePart(
            LimitedCommodityStateMap passiveMap,
            LimitedCommodityStateMap activeMap,
            Commodity activeCommodity) {

        passiveMap.setOrAddPower(activeCommodity, activeMap.getPowerWithoutCheck(activeCommodity));
    }

    private void updateActivePart(
            LimitedCommodityStateMap activeMap,
            LimitedCommodityStateMap passiveMap,
            Commodity passiveCommodity) {

        activeMap.setPower(passiveCommodity, 0.0);

        //TODO: activate voltage exchange if it becomes relevant
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
}
