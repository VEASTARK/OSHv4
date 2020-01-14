package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;


/**
 * @author Ingo Mauser, Florian Allerding
 */
public class DevicesPowerComExchange extends CALComExchange {

    private final HashMap<UUID, EnumMap<Commodity, Double>> powerStates;


    /**
     * CONSTRUCTOR
     */
    public DevicesPowerComExchange(UUID deviceID, Long timestamp, DevicesPowerStateExchange dpsex) {
        super(deviceID, timestamp);

        DevicesPowerStateExchange cloned = dpsex.clone();
        this.powerStates = cloned.getPowerStateMap();
    }


    public HashMap<UUID, EnumMap<Commodity, Double>> getPowerStates() {
        return this.powerStates;
    }

}
