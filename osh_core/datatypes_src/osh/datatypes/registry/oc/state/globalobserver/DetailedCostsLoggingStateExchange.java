package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class DetailedCostsLoggingStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 2936707619450436755L;

    private final EnumMap<AncillaryCommodity, Integer> map;

    private final EnumMap<AncillaryCommodity, PriceSignal> ps;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param map
     */
    public DetailedCostsLoggingStateExchange(
            UUID sender,
            long timestamp,
            EnumMap<AncillaryCommodity, Integer> map,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        super(sender, timestamp);

        this.map = map;

        this.ps = ps;
        this.pwrLimit = pwrLimit;
    }


    public EnumMap<AncillaryCommodity, Integer> getMap() {
        return this.map;
    }

    public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
        return this.ps;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPwrLimit() {
        return this.pwrLimit;
    }


    @Override
    public StateExchange clone() {

        EnumMap<AncillaryCommodity, Integer> clonedMap = new EnumMap<>(AncillaryCommodity.class);
        for (Entry<AncillaryCommodity, Integer> e : this.map.entrySet()) {
            clonedMap.put(e.getKey(), e.getValue());
        }

        EnumMap<AncillaryCommodity, PriceSignal> newPs = new EnumMap<>(AncillaryCommodity.class);
        for (Entry<AncillaryCommodity, PriceSignal> e : this.ps.entrySet()) {
            newPs.put(e.getKey(), e.getValue().clone());
        }

        EnumMap<AncillaryCommodity, PowerLimitSignal> newPls = new EnumMap<>(AncillaryCommodity.class);
        for (Entry<AncillaryCommodity, PowerLimitSignal> e : this.pwrLimit.entrySet()) {
            newPls.put(e.getKey(), e.getValue().clone());
        }

        return new DetailedCostsLoggingStateExchange(
                this.getSender(),
                this.getTimestamp(),
                clonedMap,
                newPs,
                newPls);
    }
}
