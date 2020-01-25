package osh.hal.exchange;

import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exchange.HALExchange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class LoggerDevicesPowerHALExchange extends HALExchange {

    private final HashMap<UUID, EnumMap<Commodity, Double>> powerStates;


    /**
     * CONSTRUCTOR
     */
    public LoggerDevicesPowerHALExchange(
            UUID deviceID,
            long timestamp,
            HashMap<UUID, EnumMap<Commodity, Double>> powerStates) {
        super(deviceID, timestamp);

        this.powerStates = new HashMap<>();

        for (Entry<UUID, EnumMap<Commodity, Double>> e : powerStates.entrySet()) {
            EnumMap<Commodity, Double> current = new EnumMap<>(Commodity.class);
            this.powerStates.put(e.getKey(), current);

            for (Entry<Commodity, Double> f : e.getValue().entrySet()) {
                current.put(f.getKey(), f.getValue());
            }
        }
    }


    public HashMap<UUID, EnumMap<Commodity, Double>> getPowerStates() {
        return this.powerStates;
    }

}
