package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PlsComExchange extends CALComExchange {

    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals =
            new EnumMap<>(AncillaryCommodity.class);

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     * @param powerLimitSignals
     */
    public PlsComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        super(deviceID, timestamp);

        for (Entry<AncillaryCommodity, PowerLimitSignal> e : powerLimitSignals.entrySet()) {
            this.powerLimitSignals.put(e.getKey(), e.getValue().clone());
        }
    }


    public Map<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
        return this.powerLimitSignals;
    }

}
