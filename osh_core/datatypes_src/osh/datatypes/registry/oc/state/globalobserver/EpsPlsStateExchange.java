package osh.datatypes.registry.oc.state.globalobserver;

import osh.configuration.oc.CostConfiguration;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class EpsPlsStateExchange extends StateExchange {

    private final CostConfiguration costConfiguration;
    private final double plsOverLimitFactor;
    private final boolean epsPlsChanged;
    private final EnumMap<AncillaryCommodity, PriceSignal> ps;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param ps
     * @param pwrLimit
     */
    public EpsPlsStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit,
            CostConfiguration costConfiguration,
            double plsOverLimitFactor,
            boolean epsPlsChanged) {
        super(sender, timestamp);

        this.ps = ps;
        this.pwrLimit = pwrLimit;
        this.costConfiguration = costConfiguration.createCopy();
        this.plsOverLimitFactor = plsOverLimitFactor;
        this.epsPlsChanged = epsPlsChanged;
    }


    public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
        return this.ps;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPwrLimit() {
        return this.pwrLimit;
    }

    public CostConfiguration getCostConfiguration() {
        return this.costConfiguration;
    }

    public double getPlsOverLimitFactor() {
        return this.plsOverLimitFactor;
    }

    public boolean isEpsPlsChanged() {
        return this.epsPlsChanged;
    }


    @Override
    public EpsPlsStateExchange clone() {

        // TODO cloning

        return new EpsPlsStateExchange(
                this.getSender(),
                this.getTimestamp(),
                this.ps,
                this.pwrLimit,
                this.costConfiguration,
                this.plsOverLimitFactor,
                this.epsPlsChanged);
    }

}
