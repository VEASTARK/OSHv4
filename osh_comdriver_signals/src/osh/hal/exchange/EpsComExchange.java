package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class EpsComExchange extends CALComExchange {

    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private boolean causeScheduling;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID     unique id of this exchange
     * @param timestamp    timestamp of this exchange
     * @param priceSignals price signals contained in this exchange
     */
    public EpsComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals) {
        super(deviceID, timestamp);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);

        for (Entry<AncillaryCommodity, PriceSignal> e : priceSignals.entrySet()) {
            this.priceSignals.put(e.getKey(), e.getValue().clone());
        }
    }

    public EpsComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            Map<AncillaryCommodity, PriceSignal> priceSignals,
            boolean causeScheduling) {
        super(deviceID, timestamp);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
        this.causeScheduling = causeScheduling;

        for (Entry<AncillaryCommodity, PriceSignal> e : priceSignals.entrySet()) {
            this.priceSignals.put(e.getKey(), e.getValue().clone());
        }
    }

    public boolean causeScheduling() {
        return this.causeScheduling;
    }


    public Map<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

}
