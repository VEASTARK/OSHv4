package osh.esc.grid;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
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
import osh.esc.ArrayUtils;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;
import osh.esc.grid.carrier.ElectricalConnection;
import osh.utils.functions.PrimitiveOperators;
import osh.utils.functions.QuadConsumer;
import osh.utils.functions.SerializableConsumer;
import osh.utils.functions.SerializableOperator;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ElectricalEnergyGrid implements IEnergyGrid {

    //meter
    private final UUID meterUUID;
    private int meterId;
    //energy-relations
    private final List<EnergyRelation<ElectricalConnection>> relationList;
    private final Set<UUID> activeUUIDs;
    private final Set<UUID> passiveUUIDs;

    //device information
    private final EnumMap<GridDeviceType, Set<UUID>> devicesByType;
    private final EnumMap<GridDeviceType, IntSet> devicesByTypeAsId;

    //initialized and improved (shortened) energy relations
    private InitializedEnergyRelation[] initializedImprovedActiveToPassiveArray;

    //information about devices
    private boolean isSingular;
    private int singularPvDevice = -1;
    private int singularChpDevice = -1;
    private int singularBatDevice = -1;
    private boolean hasBeenInitialized;
    private boolean hasBat = true, hasPV = true, hasCHP = true;

    private SerializableConsumer<UUIDCommodityMap> calcPowers;
    private PrimitiveOperators.SerializableToDoubleFunction<UUIDCommodityMap> pvPowerFunction;
    private PrimitiveOperators.SerializableToDoubleFunction<UUIDCommodityMap> chpPowerFunction;
    private PrimitiveOperators.SerializableToDoubleFunction<UUIDCommodityMap> batPowerFunction;
    private QuadConsumer<AncillaryMeterState, double[], double[], double[]> populateAdditionalStates;
    private QuadConsumer<AncillaryMeterState, double[], double[], double[]> populatePvStates;
    private QuadConsumer<AncillaryMeterState, double[], double[], double[]> populateChpStates;
    private QuadConsumer<AncillaryMeterState, double[], double[], double[]> populateBatStates;
    private SerializableOperator calcPowerShares;

    //re-useable arrays to prevent excessive memory operations
    private final double[] productionPowers = new double[4];
    private final double[] productionShares = new double[3];
    private final double[] mainPowers = new double[4];

    /**
     * Constructs this electrical grid with the given layout.
     *
     * @param layoutFilePath path to the layout of the grid to be constructed
     *
     * @throws JAXBException when the layout cannot be interpreted
     * @throws FileNotFoundException when the layout cannot be found at the given path
     */
    public ElectricalEnergyGrid(String layoutFilePath) throws JAXBException, FileNotFoundException {

        this.relationList = new ObjectArrayList<>();
        this.activeUUIDs = new ObjectOpenHashSet<>();
        this.passiveUUIDs = new ObjectOpenHashSet<>();
        this.devicesByType = new EnumMap<>(GridDeviceType.class);
        this.devicesByTypeAsId = new EnumMap<>(GridDeviceType.class);

        Object unmarshalled = XMLSerialization.file2Unmarshal(layoutFilePath, GridLayout.class);

        if (unmarshalled instanceof GridLayout) {
            GridLayout layout = (GridLayout) unmarshalled;

            for (LayoutConnection conn : layout.getConnections()) {
                EnergySourceSink act = new EnergySourceSink(UUID.fromString(conn.getActiveEntityUUID()));
                EnergySourceSink pass = new EnergySourceSink(UUID.fromString(conn.getPassiveEntityUUID()));

                this.activeUUIDs.add(UUID.fromString(conn.getActiveEntityUUID()));
                this.passiveUUIDs.add(UUID.fromString(conn.getPassiveEntityUUID()));

                this.relationList.add(new EnergyRelation<>(act, pass,
                        new ElectricalConnection(Commodity.fromString(conn.getActiveToPassiveCommodity())),
                        new ElectricalConnection(Commodity.fromString(conn.getPassiveToActiveCommodity()))));
            }

            this.meterUUID = UUID.fromString(layout.getSuperMeterUUID());

            this.devicesByType.put(GridDeviceType.PV, new ObjectOpenHashSet<>());
            this.devicesByType.put(GridDeviceType.CHP, new ObjectOpenHashSet<>());
            this.devicesByType.put(GridDeviceType.BATTERY, new ObjectOpenHashSet<>());

            for (DevicePerMeter dev : layout.getDeviceMeterMap()) {

                UUID device = UUID.fromString(dev.getDeviceUUID());

                Set<UUID> devicesByType = this.devicesByType.get(GridDeviceType.fromString(dev.getDeviceType()));
                devicesByType.add(device);
            }

        } else
            throw new IllegalArgumentException("layoutFile not instance of GridLayout-class (should not be possible)");

        //sanity check
        if (!Collections.disjoint(this.activeUUIDs, this.passiveUUIDs))
            throw new IllegalArgumentException("Same UUID is active and passive");
    }

    public ElectricalEnergyGrid(ElectricalEnergyGrid other) {
        this.meterUUID = other.meterUUID;
        this.meterId = other.meterId;

        this.relationList = other.relationList;
        this.activeUUIDs = other.activeUUIDs;
        this.passiveUUIDs = other.passiveUUIDs;

        this.devicesByType = other.devicesByType;
        this.devicesByTypeAsId = other.devicesByTypeAsId;

        this.initializedImprovedActiveToPassiveArray = other.initializedImprovedActiveToPassiveArray;

        this.isSingular = other.isSingular;
        this.singularPvDevice = other.singularPvDevice;
        this.singularChpDevice = other.singularChpDevice;
        this.singularBatDevice = other.singularBatDevice;
        this.hasBeenInitialized = other.hasBeenInitialized;
        this.hasBat = other.hasBat;
        this.hasPV = other.hasPV;
        this.hasCHP = other.hasCHP;

        ArrayUtils.fillArrayDouble(this.productionPowers, 0.0);
        ArrayUtils.fillArrayDouble(this.mainPowers, 0.0);
        ArrayUtils.fillArrayDouble(this.productionShares, 0.0);

        this.initializeFunction();
    }


    @Override
    public void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                               Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap,
                               Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap) {


        final List<InitializedEnergyRelation> initializedImprovedActiveToPassiveList = new ObjectArrayList<>();
        Map<UUID, List<InitializedEnergyRelationTarget>> tempHelpMap = new Object2ObjectOpenHashMap<>();

        for (EnergyRelation<ElectricalConnection> rel : this.relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            boolean activeType = allActiveNodes.contains(activeId);
            boolean passiveType = passiveNodes.contains(passiveId) || passiveId.equals(this.meterUUID);

            //if both exist and the device really puts out the commodity add to the respective lists
            if (activeType && passiveType
                    && uuidOutputMap.get(activeId).contains(rel.getActiveToPassive().getCommodity())
                    && uuidInputMap.get(passiveId).contains(rel.getActiveToPassive().getCommodity())) {
                List<InitializedEnergyRelationTarget> targets = tempHelpMap.computeIfAbsent(activeId,
                        k -> new ObjectArrayList<>());

                targets.add(new InitializedEnergyRelationTarget(uuidToIntMap.getInt(passiveId), rel.getActiveToPassive().getCommodity()));
            }
        }

        tempHelpMap.forEach((k, v) -> initializedImprovedActiveToPassiveList.add(new InitializedEnergyRelation(uuidToIntMap.getInt(k), v)));

        this.initializedImprovedActiveToPassiveArray = new InitializedEnergyRelation[initializedImprovedActiveToPassiveList.size()];
        this.initializedImprovedActiveToPassiveArray = initializedImprovedActiveToPassiveList.toArray(this.initializedImprovedActiveToPassiveArray);

        //check if there is at most one of every special device
        if (this.devicesByType.get(GridDeviceType.PV).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains
                (e))).count() <= 1
                && this.devicesByType.get(GridDeviceType.CHP).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1
                && this.devicesByType.get(GridDeviceType.BATTERY).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).count() <= 1) {

            this.isSingular = true;
            this.singularPvDevice = uuidToIntMap.getInt(this.devicesByType.get(GridDeviceType.PV).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
            this.singularChpDevice = uuidToIntMap.getInt(this.devicesByType.get(GridDeviceType.CHP).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
            this.singularBatDevice = uuidToIntMap.getInt(this.devicesByType.get(GridDeviceType.BATTERY).stream().filter(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e))).findFirst().orElse(null));
        } else {

            IntOpenHashSet pvSet = new IntOpenHashSet();
            for (UUID pv : this.devicesByType.get(GridDeviceType.PV)) {
                pvSet.add(uuidToIntMap.getInt(pv));
            }
            this.devicesByTypeAsId.put(GridDeviceType.PV, pvSet);

            IntOpenHashSet chpSet = new IntOpenHashSet();
            for (UUID chp : this.devicesByType.get(GridDeviceType.CHP)) {
                chpSet.add(uuidToIntMap.getInt(chp));
            }
            this.devicesByTypeAsId.put(GridDeviceType.CHP, chpSet);

            IntOpenHashSet batSet = new IntOpenHashSet();
            for (UUID bat : this.devicesByType.get(GridDeviceType.BATTERY)) {
                batSet.add(uuidToIntMap.getInt(bat));
            }
            this.devicesByTypeAsId.put(GridDeviceType.BATTERY, batSet);
        }

        this.meterId = uuidToIntMap.getInt(this.meterUUID);

        this.hasPV = this.devicesByType.get(GridDeviceType.PV).stream().anyMatch(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e)));
        this.hasCHP = this.devicesByType.get(GridDeviceType.CHP).stream().anyMatch(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e)));
        this.hasBat = this.devicesByType.get(GridDeviceType.BATTERY).stream().anyMatch(e -> (allActiveNodes.contains(e) || passiveNodes.contains(e)));

        this.initializeFunction();

        this.hasBeenInitialized = true;
    }

    private void initializeFunction() {
        this.calcPowerShares = () -> { };

        if (!this.hasPV && !this.hasCHP && !this.hasBat) {
            this.calcPowers = (a) -> { };
            this.populateAdditionalStates = (a, b, c, d) -> { };
        } else {

            this.populateAdditionalStates = this::populateAdditionalStates;
            this.calcPowers = this::calcPowers;

            if (this.hasPV) {
                this.pvPowerFunction = this.isSingular ? this::calcPowerPVSingular : this::calcPowerPVMulti;
                this.populatePvStates = this::populatePVStates;
            } else {
                this.pvPowerFunction = this::doNothing;
                this.populatePvStates = this::doNothing;
            }

            if (this.hasCHP) {
                this.chpPowerFunction = this.isSingular ? this::calcPowerCHPSingular : this::calcPowerCHPMulti;
                this.populateChpStates = this::populateChpStates;
            } else {
                this.chpPowerFunction = this::doNothing;
                this.populateChpStates = this::doNothing;
            }

            if (this.hasBat) {
                this.batPowerFunction = this.isSingular ? this::calcPowerBatSingular : this::calcPowerBatMulti;
                this.populateBatStates = this::populateBatStates;
            } else {
                this.batPowerFunction = this::doNothing;
                this.populateBatStates = this::doNothing;
            }

            if (this.hasPV && !this.hasCHP && !this.hasBat) {
                this.productionShares[0] = 1.0;
                this.productionShares[1] = 0.0;
                this.productionShares[2] = 0.0;
            } else if (!this.hasPV && this.hasCHP && !this.hasBat) {
                this.productionShares[0] = 0.0;
                this.productionShares[1] = 1.0;
                this.productionShares[2] = 0.0;
            } else if (!this.hasPV && !this.hasCHP) {
                this.productionShares[0] = 0.0;
                this.productionShares[1] = 0.0;
                this.productionShares[2] = 1.0;
            } else {
                this.calcPowerShares = this::calcAllPowerShares;
            }
        }
    }

    @Override
    public void finalizeGrid() {
        this.hasBeenInitialized = false;
        this.devicesByTypeAsId.clear();

        this.meterId = -1;
        this.singularPvDevice = -1;
        this.singularChpDevice = -1;
        this.singularBatDevice = -1;

        this.isSingular = false;
        this.hasBat = true;
        this.hasPV = true;
        this.hasCHP = true;

        this.calcPowerShares = this::calcAllPowerShares;

        this.pvPowerFunction = this::calcPowerPVMulti;
        this.populatePvStates = this::populatePVStates;

        this.chpPowerFunction = this::calcPowerCHPMulti;
        this.populateChpStates = this::populateChpStates;

        this.batPowerFunction = this::calcPowerBatMulti;
        this.populateBatStates = this::populateBatStates;

        this.calcPowers = this::calcPowers;
        this.populateAdditionalStates = this::populateAdditionalStates;
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

            if (activeLocalCommodities == null) {
                continue;
            }

            for (InitializedEnergyRelationTarget target : rel.getTargets()) {

                // update passive part... (if possible

                if (activeLocalCommodities.containsCommodity(target.getCommodity())) {
                    this.updatePassivePart(totalInputStates.get(target.getTargetID()), activeLocalCommodities,
                            target.getCommodity());
                }
            }
        }

        this.calculateMeter(activeStates, totalInputStates, ancillaryMeterState);
    }

    @Override
    public void doPassiveToActiveCalculation(
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates) {

        //nothing for electrical devices
    }

    //TODO: if at any time we will have multiple pvs/chps/batteries we have to adjust this so
    // it does not rely on having only a single one of these devices for the sped-up methods
    private void calculateMeter(
            UUIDCommodityMap localCommodityStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState) {
        // calculate ancillary states

        if (ancillaryMeterState != null) {
            this.populateMeter(localCommodityStates, totalInputStates, ancillaryMeterState);
        }
    }

    private void calculateMeter(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState) {

        Objects.requireNonNull(ancillaryMeterState);

        double pvPower = 0;
        double chpPower = 0;
        double batteryPower = 0;

        for (UUID pv : this.devicesByType.get(GridDeviceType.PV)) {
            LimitedCommodityStateMap pvMap = localCommodityStates.get(pv);
            pvPower += pvMap != null ? pvMap.getPower(Commodity.ACTIVEPOWER) : 0;
        }

        for (UUID chp : this.devicesByType.get(GridDeviceType.CHP)) {
            LimitedCommodityStateMap chpMap = localCommodityStates.get(chp);
            chpPower += chpMap != null ? chpMap.getPower(Commodity.ACTIVEPOWER) : 0;
        }

        for (UUID battery : this.devicesByType.get(GridDeviceType.BATTERY)) {
            LimitedCommodityStateMap batteryMap = localCommodityStates.get(battery);
            batteryPower += batteryMap != null ? batteryMap.getPower(Commodity.ACTIVEPOWER) : 0;
        }

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.meterUUID);

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

    /*
     * 0 = pvPower
     * 1 = chpPower
     * 2 = batPower
     * 3 = total Production Power
     */
    private void calcPowers(UUIDCommodityMap localCommodityStates) {
        this.productionPowers[0] = this.pvPowerFunction.applyAsDouble(localCommodityStates);
        this.productionPowers[1] = this.chpPowerFunction.applyAsDouble(localCommodityStates);
        this.productionPowers[2] = this.batPowerFunction.applyAsDouble(localCommodityStates);
        this.productionPowers[3] = (this.productionPowers[0] < 0 ? this.productionPowers[0] : 0.0) +
                (this.productionPowers[1] < 0 ? this.productionPowers[1] : 0.0) + (this.productionPowers[2] < 0 ?
                this.productionPowers[2] : 0.0);
    }

    /*
     * 0 = pvShare
     * 1 = chpShare
     * 2 = batShare
     */
    private void calcAllPowerShares() {
        if (this.productionPowers[3] < 0) {
            this.productionShares[0] = this.productionPowers[0] < 0 ? this.productionPowers[0] / this.productionPowers[3] : 0.0;
            this.productionShares[1] = this.productionPowers[1] < 0 ? this.productionPowers[1] / this.productionPowers[3] : 0.0;
            this.productionShares[2] = this.productionPowers[2] < 0 ? this.productionPowers[2] / this.productionPowers[3] : 0.0;
        }
    }

    private void populateMeter(UUIDCommodityMap localCommodityStates, UUIDCommodityMap totalInputStates, AncillaryMeterState meterState) {

        this.calcPowers.accept(localCommodityStates);
        this.calcPowerShares.accept();

        LimitedCommodityStateMap meterMap = totalInputStates.get(this.meterId);

        this.mainPowers[0] = meterMap.getPower(Commodity.ACTIVEPOWER);
        this.mainPowers[1] = meterMap.getPower(Commodity.REACTIVEPOWER);
        meterState.setPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL, this.mainPowers[0]);
        meterState.setPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL, this.mainPowers[1]);

        this.populateAdditionalStates.accept(meterState, this.mainPowers, this.productionPowers, this.productionShares);
    }

    private void populateAdditionalStates(AncillaryMeterState meterState, double[] meterPowers,
                                          double[] productionPowers, double[] productionShares) {
        this.populatePvStates.accept(meterState, meterPowers, productionPowers, productionShares);
        this.populateChpStates.accept(meterState, meterPowers, productionPowers, productionShares);
        this.populateBatStates.accept(meterState, meterPowers, productionPowers, productionShares);
    }

    private double doNothing(UUIDCommodityMap localCommodityStates) {
        return 0.0;
    }

    private void doNothing(AncillaryMeterState meterState, double[] meterPowers,
                           double[] productionPowers, double[] productionShares) {
    }

    private double calcPowerPVMulti(UUIDCommodityMap localCommodityStates) {
        double power = 0.0;
        for (int val : this.devicesByTypeAsId.get(GridDeviceType.PV)) {
            LimitedCommodityStateMap map = localCommodityStates.get(val);
            power += map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
        }
        return power;
    }

    private double calcPowerPVSingular(UUIDCommodityMap localCommodityStates) {
        LimitedCommodityStateMap map = localCommodityStates.get(this.singularPvDevice);
        return map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
    }

    private double calcPowerCHPMulti(UUIDCommodityMap localCommodityStates) {
        double power = 0.0;
        for (int val : this.devicesByTypeAsId.get(GridDeviceType.CHP)) {
            LimitedCommodityStateMap map = localCommodityStates.get(val);
            power += map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
        }
        return power;
    }

    private double calcPowerCHPSingular(UUIDCommodityMap localCommodityStates) {
        LimitedCommodityStateMap map = localCommodityStates.get(this.singularChpDevice);
        return map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
    }

    private double calcPowerBatMulti(UUIDCommodityMap localCommodityStates) {
        double power = 0.0;
        for (int val : this.devicesByTypeAsId.get(GridDeviceType.BATTERY)) {
            LimitedCommodityStateMap map = localCommodityStates.get(val);
            power += map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
        }
        return power;
    }

    private double calcPowerBatSingular(UUIDCommodityMap localCommodityStates) {
        LimitedCommodityStateMap map = localCommodityStates.get(this.singularBatDevice);
        return map != null ? map.getPower(Commodity.ACTIVEPOWER) : 0;
    }

    private void populatePVStates(AncillaryMeterState meterState, double[] meterPowers,
                                    double[] productionPowers, double[] productionShares) {
        //only do sth if production of pv
        if (productionPowers[0] < 0) {
            double pvInternal = productionPowers[0];

            //feed in to super
            if (meterPowers[0] < 0) {
                double pvSuper = (int) Math.round(productionShares[0] * meterPowers[0]);
                pvInternal -= pvSuper;
                meterState.setPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvSuper);
            }

            meterState.setPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
        }
    }

    private void populateChpStates(AncillaryMeterState meterState, double[] meterPowers,
                                     double[] productionPowers, double[] productionShares) {
        //only do sth if production of chp
        if (productionPowers[1] < 0) {
            double chpInternal = productionPowers[1];

            //feed in to super
            if (meterPowers[0] < 0) {
                double chpSuper = (int) Math.round(productionShares[1] * meterPowers[0]);
                chpInternal -= chpSuper;
                meterState.setPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpSuper);
            }

            meterState.setPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
        }
    }

    private void populateBatStates(AncillaryMeterState meterState, double[] meterPowers,
                                     double[] productionPowers, double[] productionShares) {
        //feed in of bat
        if (productionPowers[2] < 0) {
            double batInternal = productionPowers[2];

            //feed in to super
            if (meterPowers[0] < 0) {
                double batSuper = (int) Math.round(productionShares[2] * meterPowers[0]);
                batInternal -= batSuper;
                meterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, batSuper);
            }

            meterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, batInternal);
        }
        //consumption of bat
        else {
            meterState.setPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, productionPowers[2]);
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
    public ElectricalEnergyGrid getClone() {
        return new ElectricalEnergyGrid(this);
    }
}
