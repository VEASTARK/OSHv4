package osh.datatypes.logger;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.eal.hal.exchange.HALExchange;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class LoggerDetailedCostsHALExchange extends HALExchange {

    private final EnumMap<AncillaryCommodity, Integer> powerValueMap;

    private final EnumMap<AncillaryCommodity, PriceSignal> ps;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public LoggerDetailedCostsHALExchange(
            UUID deviceID,
            long timestamp,
            EnumMap<AncillaryCommodity, Integer> map,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        super(deviceID, timestamp);

        this.powerValueMap = new EnumMap<>(AncillaryCommodity.class);

        for (Entry<AncillaryCommodity, Integer> e : map.entrySet()) {
            this.powerValueMap.put(e.getKey(), e.getValue());
        }

        this.ps = ps;
        this.pwrLimit = pwrLimit;
    }

    public EnumMap<AncillaryCommodity, Integer> getPowerValueMap() {
        return this.powerValueMap;
    }

    public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
        return this.ps;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPwrLimit() {
        return this.pwrLimit;
    }

}
