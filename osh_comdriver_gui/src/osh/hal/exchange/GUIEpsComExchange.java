package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class GUIEpsComExchange extends CALComExchange {

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;


    /**
     * CONSTRUCTOR
     */
    public GUIEpsComExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }


    public EnumMap<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

    public void setPriceSignals(EnumMap<AncillaryCommodity, PriceSignal> priceSignals) {
        EnumMap<AncillaryCommodity, PriceSignal> clonedSignal = new EnumMap<>(AncillaryCommodity.class);
        for (Entry<AncillaryCommodity, PriceSignal> e : priceSignals.entrySet()) {
            clonedSignal.put(e.getKey(), e.getValue().clone());
        }
        this.priceSignals = clonedSignal;
    }

}
