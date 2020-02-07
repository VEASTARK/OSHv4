package osh.esc.grid;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.UUIDCommodityMap;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Calculation interface for energy grid-exchanges.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public interface IEnergyGrid {

    /**
     * Speeds up the operation of the underlying grid by restricting it's operation to only the provided sets of
     * input and output conditions. Only to be used inside the optimization loop as the device configuration of the
     * OSH cannot change there.
     *
     * @param allActiveNodes set of all active devices
     * @param activeNeedsInputNodes sub-set of all active devices that need input
     * @param passiveNodes set of all passive devices
     * @param uuidToIntMap mapping of device uuid to temporarily assigned ids
     * @param uuidOutputMap mapping of all output commidities of a device
     * @param uuidInputMap mapping of all needed input commidities of a device
     */
    void initializeGrid(Set<UUID> allActiveNodes, Set<UUID> activeNeedsInputNodes,
                        Set<UUID> passiveNodes, Object2IntOpenHashMap<UUID> uuidToIntMap,
                        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap,
                        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap);

    /**
     * Reverts all changes done by the initialization method and reverts grid operation back to considering all
     * possible energy exchanges.
     */
    void finalizeGrid();

    /**
     * Exchanges all energy information based on the configuration of the grid.
     *
     * @param commodityStates all output energy information to be exchanged
     * @param totalInputStates map-storage for all resulting energy information from the exchange
     * @param ancillaryMeterState virtual meter
     */
    void doCalculation(
            Map<UUID, LimitedCommodityStateMap> commodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState);

    /**
     * Exchanges only energy information from active to passive devices. Only to be used inside the optimization loop.
     *
     * @param activeStates all output energy information of active devices
     * @param totalInputStates map-storage for all resulting energy information from the exchange
     * @param ancillaryMeterState virtual meter
     */
    void doActiveToPassiveCalculation(
            UUIDCommodityMap activeStates,
            UUIDCommodityMap totalInputStates,
            AncillaryMeterState ancillaryMeterState);

    /**
     * Exchanges only energy information from passive to active devices. Only to be used inside the optimization loop.
     *
     * @param passiveStates all output energy information of passive devices
     * @param totalInputStates map-storage for all resulting energy information from the exchange
     */
    void doPassiveToActiveCalculation(
            UUIDCommodityMap passiveStates,
            UUIDCommodityMap totalInputStates);

    /**
     * Returns the uuid of the virtual meter.
     *
     * @return the uuid of the virtual meter
     */
    UUID getMeterUUID();

    /**
     * Returns the uuid of all active devices.
     *
     * @return the uuid of all active devices
     */
    Set<UUID> getActiveUUIDs();

    /**
     * Returns the uuid of all passive devices.
     *
     * @return the uuid of all passive devices
     */
    Set<UUID> getPassiveUUIDs();

}
