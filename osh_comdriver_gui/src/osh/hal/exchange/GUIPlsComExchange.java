package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class GUIPlsComExchange extends CALComExchange {

    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GUIPlsComExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }


    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
        return this.powerLimitSignals;
    }

    public void setPowerLimitSignals(EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        EnumMap<AncillaryCommodity, PowerLimitSignal> clonedSignal = new EnumMap<>(AncillaryCommodity.class);
        for (Entry<AncillaryCommodity, PowerLimitSignal> e : powerLimitSignals.entrySet()) {
            clonedSignal.put(e.getKey(), e.getValue().clone());
        }
        this.powerLimitSignals = clonedSignal;
    }

}
