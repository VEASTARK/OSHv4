package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class DevicesPowerStateExchange extends StateExchange {

    private final HashMap<UUID, EnumMap<Commodity, Double>> powerStates;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public DevicesPowerStateExchange(UUID sender, ZonedDateTime timestamp) {
        this(sender, timestamp, new HashMap<>());
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param powerStates
     */
    public DevicesPowerStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            HashMap<UUID, EnumMap<Commodity, Double>> powerStates) {
        super(sender, timestamp);

        this.powerStates = powerStates;
    }


    public void addPowerState(UUID uuid, Commodity commodity, double value) {

        EnumMap<Commodity, Double> existingMap = this.powerStates.get(uuid);

        if (existingMap == null) {
            // Create new map and add
            EnumMap<Commodity, Double> newMap = new EnumMap<>(Commodity.class);
            this.powerStates.put(uuid, newMap);
            existingMap = newMap;
        }

        existingMap.put(commodity, value);
    }

    public Double getPowerState(UUID uuid, Commodity commodity) {

        Double returnValue = null;

        EnumMap<Commodity, Double> existingMap = this.powerStates.get(uuid);
        if (existingMap != null) {
            returnValue = existingMap.get(commodity);
        }

        return returnValue;
    }

    public HashMap<UUID, EnumMap<Commodity, Double>> getPowerStateMap() {
        return this.powerStates;
    }


    @Override
    public DevicesPowerStateExchange clone() {
        DevicesPowerStateExchange cloned = new DevicesPowerStateExchange(this.getSender(), this.getTimestamp());

        for (Entry<UUID, EnumMap<Commodity, Double>> e : this.powerStates.entrySet()) {
            UUID uuid = e.getKey();
            EnumMap<Commodity, Double> innerMap = e.getValue();

            for (Entry<Commodity, Double> f : innerMap.entrySet()) {
                cloned.addPowerState(uuid, f.getKey(), f.getValue());
            }
        }

        return cloned;
    }

}
