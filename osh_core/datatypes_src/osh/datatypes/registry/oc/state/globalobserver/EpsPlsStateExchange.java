package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.StateExchange;

import java.util.EnumMap;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class EpsPlsStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 5984069610430579990L;
    private final int epsOptimizationObjective;
    private final int plsOptimizationObjective;
    private final int varOptimizationObjective;
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
            long timestamp,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit,
            int epsOptimizationObjective,
            int plsOptimizationObjective,
            int varOptimizationObjective,
            double plsUpperOverLimitFactor,
            double plsLowerOverLimitFactor,
            boolean epsPlsChanged) {
        super(sender, timestamp);

        this.ps = ps;
        this.pwrLimit = pwrLimit;
        this.epsOptimizationObjective = epsOptimizationObjective;
        this.plsOptimizationObjective = plsOptimizationObjective;
        this.varOptimizationObjective = varOptimizationObjective;
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

    public int getEpsOptimizationObjective() {
        return this.epsOptimizationObjective;
    }


    public int getPlsOptimizationObjective() {
        return this.plsOptimizationObjective;
    }


    public int getVarOptimizationObjective() {
        return this.varOptimizationObjective;
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
//		EnumMap<AncillaryCommodity,PriceSignal> newPs = new HashMap<>();
//		for (Entry<AncillaryCommodity,PriceSignal> e : ps.entrySet()) {
//			newPs.put(e.getKey(), e.getValue().clone());
//		}
//		
//		EnumMap<AncillaryCommodity,PowerLimitSignal> newPls = new HashMap<>();
//		for (Entry<AncillaryCommodity,PowerLimitSignal> e : pwrLimit.entrySet()) {
//			newPls.put(e.getKey(), e.getValue().clone());
//		}

        return new EpsPlsStateExchange(
                this.getSender(),
                this.getTimestamp(),
                this.ps,
                this.pwrLimit,
                this.epsOptimizationObjective,
                this.plsOptimizationObjective,
                this.varOptimizationObjective,
                this.plsUpperOverLimitFactor,
                this.plsLowerOverLimitFactor,
                this.epsPlsChanged);
    }

}
