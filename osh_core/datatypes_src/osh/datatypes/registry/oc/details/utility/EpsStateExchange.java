package osh.datatypes.registry.oc.details.utility;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class EpsStateExchange extends StateExchange {

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private boolean causeScheduling;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public EpsStateExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
    }

    public EpsStateExchange(UUID sender, ZonedDateTime timestamp, boolean causeScheduling) {
        super(sender, timestamp);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
        this.causeScheduling = causeScheduling;
    }

    public void setPriceSignal(AncillaryCommodity vc, PriceSignal priceSignal) {
        PriceSignal copy = priceSignal.clone();
        this.priceSignals.put(vc, copy);
    }

    public EnumMap<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

    public void setPriceSignals(EnumMap<AncillaryCommodity, PriceSignal> priceSignals) {
        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);

        for (Entry<AncillaryCommodity, PriceSignal> e : priceSignals.entrySet()) {
            this.priceSignals.put(e.getKey(), e.getValue().clone());
        }
    }

    public boolean causeScheduling() {
        return this.causeScheduling;
    }

    @Override
    public EpsStateExchange clone() {
        EpsStateExchange clonedX = new EpsStateExchange(this.getSender(), this.getTimestamp());
        clonedX.setPriceSignals(this.priceSignals);
        clonedX.causeScheduling = this.causeScheduling;
        return clonedX;
    }
}