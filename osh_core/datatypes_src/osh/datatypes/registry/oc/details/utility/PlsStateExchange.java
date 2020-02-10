package osh.datatypes.registry.oc.details.utility;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PlsStateExchange extends StateExchange {

    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public PlsStateExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);

        this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
    }

    public void setPowerLimitSignal(AncillaryCommodity vc, PowerLimitSignal powerLimitSignal) {
        PowerLimitSignal copy = powerLimitSignal.clone();
        this.powerLimitSignals.put(vc, copy);
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
        return this.powerLimitSignals;
    }

    public void setPowerLimitSignals(EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

        for (Entry<AncillaryCommodity, PowerLimitSignal> e : powerLimitSignals.entrySet()) {
            this.powerLimitSignals.put(e.getKey(), e.getValue().clone());
        }
    }

    @Override
    public PlsStateExchange clone() {
        PlsStateExchange clonedX = new PlsStateExchange(this.getSender(), this.getTimestamp());
        clonedX.setPowerLimitSignals(this.powerLimitSignals);
        return clonedX;
    }
}