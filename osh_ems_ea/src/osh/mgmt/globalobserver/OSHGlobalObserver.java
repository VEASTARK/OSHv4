package osh.mgmt.globalobserver;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalOCUnit;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIDeviceListStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IProvidesIdentity;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class OSHGlobalObserver
        extends GlobalObserver
        implements IDataRegistryListener, IProvidesIdentity {

    private final Map<UUID, InterdependentProblemPart<?, ?>> iProblempart;

    private final HashMap<UUID, EnumMap<Commodity, Double>> deviceCommodityMap = new HashMap<>();
    private EnumMap<Commodity, Double> commodityTotalPowerMap = new EnumMap<>(Commodity.class);

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param configurationParameters
     */
    public OSHGlobalObserver(
            IOSHOC osh,
            OSHParameterCollection configurationParameters) {
        super(osh, configurationParameters);

        this.iProblempart = Collections.synchronizedMap(new HashMap<>());
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

        this.getOCRegistry().subscribe(InterdependentProblemPart.class, this);

    }

    @Override
    public void onSystemShutdown() {
        //nothing
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        boolean problemPartListChanged = false;

        if (exchange instanceof InterdependentProblemPart) {
            InterdependentProblemPart<?, ?> ppex = (InterdependentProblemPart<?, ?>) exchange;
            this.iProblempart.put(exchange.getSender(), ppex);
            problemPartListChanged = true;
        }
        if (problemPartListChanged && this.getControllerBoxStatus().hasGUI()) {
            this.getOCRegistry().publish(
                    GUIDeviceListStateExchange.class, this,
                    new GUIDeviceListStateExchange(
                            this.getUUID(),
                            this.getTimeDriver().getCurrentTime(),
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
                LocalObserver lo = this.getLocalObserver(p.getUUID());
                LocalOCUnit ocUnit = lo.getAssignedOCUnit();
                type = ocUnit.getDeviceType().toString() + "(" + ocUnit.getDeviceClassification().toString() + ")";
            } catch (NullPointerException ignored) {
            }
            DeviceTableEntry e = new DeviceTableEntry(i, p.getUUID(), type, "[" + p.getTimestamp() + "] " + p.isToBeScheduled(), p.problemToString());
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
        ippList.sort(Comparator.comparing(InterdependentProblemPart::getUUID));
        return ippList;
    }

    @Override
    public UUID getUUID() {
        return this.getAssignedOCUnit().getUnitID();
    }

    private void getCurrentPowerOfDevices() {
        // get new power values
        Map<UUID, AbstractExchange> dataMap = this.getOCRegistry().getData(CommodityPowerStateExchange.class);

        Map<UUID, CommodityPowerStateExchange> powerStatesMap = new Object2ObjectOpenHashMap<>();
        dataMap.forEach((k, v) -> powerStatesMap.put(k, (CommodityPowerStateExchange) v));

        this.commodityTotalPowerMap = new EnumMap<>(Commodity.class);

        for (Entry<UUID, CommodityPowerStateExchange> e : powerStatesMap.entrySet()) {
            if (e.getKey().equals(this.getUUID())) {// ignore own CommodityPowerStateExchange
                continue;
            }
            this.updateDeviceCommodityMap(e);
        }

        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        // Export Commodity powerStates
        CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                this.getUUID(),
                now,
                DeviceTypes.OTHER);
        for (Entry<Commodity, Double> e : this.commodityTotalPowerMap.entrySet()) {
            cpse.addPowerState(e.getKey(), e.getValue());
        }

        this.getOCRegistry().publish(
                CommodityPowerStateExchange.class,
                this,
                cpse);

        // Export Device powerStates
        DevicesPowerStateExchange dpse = new DevicesPowerStateExchange(this.getUUID(), now);

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

        // save as state
        this.getOCRegistry().publish(
                DevicesPowerStateExchange.class,
                this,
                dpse);
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
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        // get current power states
        //  and also send them to logger
        this.getCurrentPowerOfDevices();
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return null;
    }
}