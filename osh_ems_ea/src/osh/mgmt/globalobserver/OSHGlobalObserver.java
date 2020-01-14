package osh.mgmt.globalobserver;

import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalOCUnit;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.*;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;
import osh.utils.uuid.UUIDLists;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class OSHGlobalObserver
        extends GlobalObserver
        implements IEventTypeReceiver, IHasState {

    List<UUID> totalPowerMeter;
    private final Map<UUID, InterdependentProblemPart<?, ?>> iProblempart;
    private boolean reschedule;

    private final HashMap<UUID, EnumMap<Commodity, Double>> deviceCommodityMap = new HashMap<>();
    private EnumMap<Commodity, Double> commodityTotalPowerMap = new EnumMap<>(Commodity.class);

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param configurationParameters
     * @throws OSHException
     */
    public OSHGlobalObserver(
            IOSHOC osh,
            OSHParameterCollection configurationParameters) throws OSHException {
        super(osh, configurationParameters);

        this.iProblempart = Collections.synchronizedMap(new HashMap<>());

        String strTotalPowerMeter = configurationParameters.getParameter("totalpowermeter");
        if (strTotalPowerMeter != null) {
            this.totalPowerMeter = this.parseUUIDArray(strTotalPowerMeter);
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getTimer().registerComponent(this, 1);

        this.getOCRegistry().registerStateChangeListener(InterdependentProblemPart.class, this);
        this.getOCRegistry().register(StateChangedExchange.class, this);

        this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);
    }

    @Override
    public void onSystemShutdown() {
        // finalize everything
        //currently nothing
    }

    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(
            Class<T> type, T ex) {
        boolean problemPartListChanged = false;

        if (ex instanceof StateChangedExchange) {
            StateChangedExchange sce = (StateChangedExchange) ex;

            if (sce.getType().equals(EpsStateExchange.class)) {
                EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, sce.getStatefulEntity());
                this.priceSignals = eee.getPriceSignals();
            } else if (sce.getType().equals(PlsStateExchange.class)) {
                PlsStateExchange eee = this.getOCRegistry().getState(PlsStateExchange.class, sce.getStatefulEntity());
                this.powerLimitSignals = eee.getPowerLimitSignals();
            } else if (sce.getType().equals(InterdependentProblemPart.class)) {
                UUID entity = sce.getStatefulEntity();
                InterdependentProblemPart<?, ?> ppex = (InterdependentProblemPart<?, ?>) this.getOCRegistry().getState(InterdependentProblemPart.class, entity);
                this.iProblempart.put(entity, ppex);
                if (ppex.isToBeScheduled()) {
                    this.reschedule = true;
                }
                problemPartListChanged = true;
            }
        }
        if (problemPartListChanged && this.getControllerBoxStatus().hasGUI()) {
            this.getOCRegistry().setState(
                    GUIDeviceListStateExchange.class, this,
                    new GUIDeviceListStateExchange(
                            this.getUUID(),
                            this.getTimer().getUnixTime(),
                            this.getDeviceList(this.getProblemParts())
                    )
            );
        }

    }

    private Set<DeviceTableEntry> getDeviceList(List<InterdependentProblemPart<?, ?>> problemParts) {
        Set<DeviceTableEntry> entries = new HashSet<>();
        int i = 1;
        for (InterdependentProblemPart<?, ?> p : problemParts) {
            String type = null;
            try {
                LocalObserver lo = this.getLocalObserver(p.getDeviceID());
                LocalOCUnit ocUnit = lo.getAssignedOCUnit();
                type = ocUnit.getDeviceType().toString() + "(" + ocUnit.getDeviceClassification().toString() + ")";
            } catch (NullPointerException ignored) {
            }
            DeviceTableEntry e = new DeviceTableEntry(i, p.getDeviceID(), type, p.getBitCount(), "[" + p.getTimestamp() + "] " + p.isToBeScheduled(), p.problemToString());
            entries.add(e);
            i++;
        }
        return entries;
    }

    /**
     * Collect all EAProblemParts from local observers
     */
    final public List<InterdependentProblemPart<?, ?>> getProblemParts() {
        List<InterdependentProblemPart<?, ?>> ippList = new ArrayList<>(this.iProblempart.values());
        ippList.sort(new ProblemPartComparator());
        return ippList;
    }

    public boolean getAndResetProblempartChangedFlag() {
        boolean tmp = this.reschedule;
        this.reschedule = false;
        return tmp;
    }

    @Override
    public UUID getUUID() {
        return this.getAssignedOCUnit().getUnitID();
    }

    private void getCurrentPowerOfDevices() {
        // get new power values
        Map<UUID, CommodityPowerStateExchange> powerStatesMap =
                this.getOCRegistry().getStates(CommodityPowerStateExchange.class);

//		if (powerStatesMap.size() > 2) {
//			@SuppressWarnings("unused")
//			int debug = 0;
//		}

        this.commodityTotalPowerMap = new EnumMap<>(Commodity.class);

        if (this.totalPowerMeter != null) {
            for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
                if (e.getKey().equals(this.getUUID())) {// ignore own CommodityPowerStateExchange
                    continue;
                }
                if (this.totalPowerMeter.contains(e.getKey())) {
                    this.updateDeviceCommodityMap(e);
                }
            }
        } else {
            for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
                if (e.getKey().equals(this.getUUID())) {// ignore own CommodityPowerStateExchange
                    continue;
                }
                this.updateDeviceCommodityMap(e);
            }
        }

        long now = this.getTimer().getUnixTime();

        // Export Commodity powerStates
        CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                this.getUUID(),
                now,
                DeviceTypes.OTHER);
        for (Entry<Commodity, Double> e : this.commodityTotalPowerMap.entrySet()) {
            cpse.addPowerState(e.getKey(), e.getValue());
        }

        this.getOCRegistry().setState(
                CommodityPowerStateExchange.class,
                this,
                cpse);

        // Export Device powerStates
        DevicesPowerStateExchange dpse = new DevicesPowerStateExchange(this.getUUID(), now);

        if (this.totalPowerMeter != null) {
            for (Entry<Commodity, Double> e : this.commodityTotalPowerMap.entrySet()) {
                dpse.addPowerState(this.getUUID(), e.getKey(), e.getValue());
            }
        } else { /* ( totalPowerMeter == null ) */
            for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
                if (!e.getKey().equals(this.getUUID())) {
                    for (Commodity c : Commodity.values()) {
                        Double value = (e.getValue()).getPowerState(c);
                        if (value != null) {
                            dpse.addPowerState(e.getKey(), c, value);
                        }
                    }
                } /* if (!e.getKey().equals(getUUID())) */
            } /* for */
        } /* if( totalPowerMeter != null ) */

        // save as state
        this.getOCRegistry().setState(
                DevicesPowerStateExchange.class,
                this,
                dpse);

        //
        int totalGasPower = 0;

        int totalHotWaterPowerProduction = 0;
        int totalHotWaterPowerConsumption = 0;
        int totalColdWaterPowerProduction = 0;
        int totalColdWaterPowerConsumption = 0;


        // save current ancillaryCommodity power states to registry (for logger and maybe more in the future)
        {
            EnumMap<AncillaryCommodity, Integer> vcMap = new EnumMap<>(AncillaryCommodity.class);

            EnumMap<DeviceTypes, Integer> devMapActivePower = new EnumMap<>(DeviceTypes.class);
            devMapActivePower.put(DeviceTypes.PVSYSTEM, 0);
            devMapActivePower.put(DeviceTypes.CHPPLANT, 0);
            devMapActivePower.put(DeviceTypes.ECAR, 0);
            devMapActivePower.put(DeviceTypes.OTHER, 0);

            // calc current ancillaryCommodity power state
            for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {

                // check whether UUID is global OC unit and exclude then...
                if (e.getKey().equals(this.getUUID())) {
                    continue;
                }

                // Active Power
                if (e.getValue().getDeviceType() == DeviceTypes.PVSYSTEM) {
                    Integer pvPower = devMapActivePower.get(DeviceTypes.PVSYSTEM);
                    Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
                    if (addPowerDouble != null) {
                        int additionalPower = (int) Math.round(addPowerDouble);
                        pvPower += additionalPower;
                        devMapActivePower.put(DeviceTypes.PVSYSTEM, pvPower);
                    }
                } else if (e.getValue().getDeviceType() == DeviceTypes.CHPPLANT) {
                    Integer chpPower = devMapActivePower.get(DeviceTypes.CHPPLANT);
                    Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
                    if (addPowerDouble != null) {
                        int additionalPower = (int) Math.round(addPowerDouble);
                        chpPower += additionalPower;
                        devMapActivePower.put(DeviceTypes.CHPPLANT, chpPower);
                    }
                } else if (e.getValue().getDeviceType() == DeviceTypes.ECAR) {
                    Integer ecarPower = devMapActivePower.get(DeviceTypes.ECAR);
                    Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
                    if (addPowerDouble != null) {
                        int additionalPower = (int) Math.round(addPowerDouble);
                        ecarPower += additionalPower;
                        devMapActivePower.put(DeviceTypes.ECAR, ecarPower);
                    }
                } else {
                    // all other device are OTHER for active power
                    Integer otherPower = devMapActivePower.get(DeviceTypes.OTHER);
                    Double addPowerDouble = e.getValue().getPowerState(Commodity.ACTIVEPOWER);
                    if (addPowerDouble != null) {
                        int additionalPower = (int) Math.round(addPowerDouble);
                        otherPower += additionalPower;
                        devMapActivePower.put(DeviceTypes.OTHER, otherPower);
                    }
                }

                // Gas power
                {
                    Double gasPowerDouble = e.getValue().getPowerState(Commodity.NATURALGASPOWER);
                    if (gasPowerDouble != null) {
                        int gasPower = (int) Math.round(gasPowerDouble);
                        totalGasPower += gasPower;
                    }
                }

                // Hot water power
                {
                    Double hotWaterPowerDouble = e.getValue().getPowerState(Commodity.HEATINGHOTWATERPOWER);
                    if (hotWaterPowerDouble != null) {
                        if (hotWaterPowerDouble > 0) {
                            int hotWaterPower = (int) Math.round(hotWaterPowerDouble);
                            totalHotWaterPowerConsumption += hotWaterPower;
                        } else if (hotWaterPowerDouble < 0) {
                            int hotWaterPower = (int) Math.round(hotWaterPowerDouble);
                            totalHotWaterPowerProduction += hotWaterPower;
                        }
                    }
                }

                // Cold water power
                {
                    Double coldWaterPowerDouble = e.getValue().getPowerState(Commodity.COLDWATERPOWER);
                    if (coldWaterPowerDouble != null) {
                        if (coldWaterPowerDouble > 0) {
                            int coldWaterPower = (int) Math.round(coldWaterPowerDouble);
                            totalColdWaterPowerConsumption += coldWaterPower;
                        } else if (coldWaterPowerDouble < 0) {
                            int coldWaterPower = (int) Math.round(coldWaterPowerDouble);
                            totalColdWaterPowerProduction += coldWaterPower;
                        }
                    }
                }


            }

            double shareOfPV = 0;
            double shareOfCHP = 0;

            Integer pvPower = devMapActivePower.get(DeviceTypes.PVSYSTEM);
            Integer chpPower = devMapActivePower.get(DeviceTypes.CHPPLANT);
            Integer ecarPower = devMapActivePower.get(DeviceTypes.ECAR);
            Integer otherPower = devMapActivePower.get(DeviceTypes.OTHER);

            if (pvPower == null) {
                pvPower = 0;
            }
            if (chpPower == null) {
                chpPower = 0;
            }

            if (pvPower != 0 && chpPower != 0) {
                shareOfPV = (double) pvPower / (pvPower + chpPower);
                shareOfCHP = (double) chpPower / (pvPower + chpPower);
            } else if (pvPower != 0) {
                shareOfPV = 1;
            } else if (chpPower != 0) {
                shareOfCHP = 1;
            }

            int totalPower = pvPower + chpPower + ecarPower + otherPower;

            // net consumption
            if (totalPower >= 0) {
                vcMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvPower);
                vcMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, 0);
                vcMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpPower);
                vcMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, 0);
                vcMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }
            // net production / feed-in
            else {
                int pvExternal = (int) Math.round(shareOfPV * totalPower);
                int pvInternal = pvPower - pvExternal;

                int chpExternal = (int) Math.round(shareOfCHP * totalPower);
                int chpInternal = chpPower - chpExternal;

                vcMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, pvInternal);
                vcMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, pvExternal);
                vcMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, chpInternal);
                vcMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, chpExternal);
                vcMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, totalPower);
            }

            vcMap.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, totalGasPower);

            // FIXME: external...
//			vcMap.put(AncillaryCommodity.HOTWATERPOWERCONSUMPTION, totalHotWaterPowerConsumption);
//			vcMap.put(AncillaryCommodity.HOTWATERPOWERPRODUCTION, totalHotWaterPowerProduction);
//			vcMap.put(AncillaryCommodity.COLDWATERPOWERCONSUMPTION, totalColdWaterPowerConsumption);
//			vcMap.put(AncillaryCommodity.COLDWATERPOWERPRODUCTION, totalColdWaterPowerProduction);

            AncillaryCommodityPowerStateExchange vcpse = new AncillaryCommodityPowerStateExchange(
                    this.getUUID(),
                    now,
                    vcMap);

            this.getOCRegistry().setState(
                    AncillaryCommodityPowerStateExchange.class,
                    this,
                    vcpse);

            DetailedCostsLoggingStateExchange dclse = new DetailedCostsLoggingStateExchange(
                    this.getUUID(),
                    now,
                    vcMap,
                    this.priceSignals,
                    this.powerLimitSignals);
            this.getOCRegistry().setState(
                    DetailedCostsLoggingStateExchange.class,
                    this,
                    dclse);
        }
    }

    private void updateDeviceCommodityMap(Entry<UUID, CommodityPowerStateExchange> e) {
        for (Commodity c : Commodity.values()) {
            Double value = (e.getValue()).getPowerState(c);
            if (value != null && value != 0) {
                EnumMap<Commodity, Double> deviceCommodityPowerMap = this.deviceCommodityMap.get(e.getKey());
                if (deviceCommodityPowerMap == null) {
                    deviceCommodityPowerMap = new EnumMap<>(Commodity.class);
                    this.deviceCommodityMap.put(e.getKey(), deviceCommodityPowerMap);
                }
                deviceCommodityPowerMap.put(c, value);

                // calculate new total power
                Double commodityTotalPower = this.commodityTotalPowerMap.get(c);
                if (commodityTotalPower == null) {
                    commodityTotalPower = 0.0;
                }
                commodityTotalPower += value;
                this.commodityTotalPowerMap.put(c, commodityTotalPower);
            }
        }
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // get current power states
        //  and also send them to logger
        this.getCurrentPowerOfDevices();
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return null;
    }

    // HELPER METHODS
    private List<UUID> parseUUIDArray(String parameter) throws OSHException {
        try {
            return UUIDLists.parseUUIDArray(parameter);
        } catch (IllegalArgumentException e) {
            throw new OSHException(e);
        }
    }

    private static class ProblemPartComparator implements Comparator<InterdependentProblemPart<?, ?>> {

        @Override
        public int compare(InterdependentProblemPart<?, ?> o1, InterdependentProblemPart<?, ?> o2) {
            return o1.getDeviceID().compareTo(o2.getDeviceID());
        }

    }


}