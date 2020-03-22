package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;
import osh.utils.CostConfigurationContainer;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class EpsPlsStateExchange extends StateExchange {

    private final CostConfigurationContainer costConfiguration;
    private final double plsUpperOverLimitFactor;
    private final double plsLowerOverLimitFactor;
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
            CostConfigurationContainer costConfiguration,
            double plsUpperOverLimitFactor,
            double plsLowerOverLimitFactor,
            boolean epsPlsChanged) {
        super(sender, timestamp);

        this.ps = ps;
        this.pwrLimit = pwrLimit;
        this.costConfiguration = costConfiguration.clone();
        this.plsUpperOverLimitFactor = plsUpperOverLimitFactor;
        this.plsLowerOverLimitFactor = plsLowerOverLimitFactor;
        this.epsPlsChanged = epsPlsChanged;
    }


    public EnumMap<AncillaryCommodity, PriceSignal> getPs() {
        return this.ps;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPwrLimit() {
        return this.pwrLimit;
    }

    public CostConfigurationContainer getCostConfiguration() {
        return this.costConfiguration;
    }

    public double getPlsUpperOverLimitFactor() {
        return this.plsUpperOverLimitFactor;
    }

    public double getPlsLowerOverLimitFactor() {
        return this.plsLowerOverLimitFactor;
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
                this.plsUpperOverLimitFactor,
                this.plsLowerOverLimitFactor,
                this.epsPlsChanged);
    }

}
