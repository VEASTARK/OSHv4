package osh.datatypes.logger;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.eal.hal.exchange.HALExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class LoggerEpsPlsHALExchange extends HALExchange {

    private EnumMap<AncillaryCommodity, PriceSignal> ps;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param ps
     * @param pwrLimit
     */
    public LoggerEpsPlsHALExchange(
            UUID sender,
            ZonedDateTime timestamp,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        super(sender, timestamp);

        this.ps = ps;
        this.pwrLimit = pwrLimit;
    }


    public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
        return this.ps;
    }

    public void setPs(EnumMap<AncillaryCommodity, PriceSignal> ps) {
        this.ps = ps;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPwrLimit() {
        return this.pwrLimit;
    }

    public void setPwrLimit(EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        this.pwrLimit = pwrLimit;
    }

}
