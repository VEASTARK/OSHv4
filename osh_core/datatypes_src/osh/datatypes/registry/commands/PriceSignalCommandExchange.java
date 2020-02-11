package osh.datatypes.registry.commands;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class PriceSignalCommandExchange extends CommandExchange {

    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param receiver
     * @param timestamp
     * @param priceSignals
     */
    public PriceSignalCommandExchange(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals) {
        super(sender, receiver, timestamp);

        this.priceSignals = priceSignals;
    }


    public EnumMap<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

    public PriceSignal getPriceSignal(AncillaryCommodity c) {
        if (this.priceSignals != null) {
            return this.priceSignals.get(c);
        } else {
            return null;
        }
    }

}
