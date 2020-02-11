package constructsimulation.configuration.OC;

/**
 * Static container for configuration of the cost-calculation (optimization objectives) in the house to construct.
 *
 * @author Sebastian Kramer
 */
public class CostConfig {

    /*
     * Optimization Objective<br>
     * AncillaryCommodity PowerPriceSignals (eps)<br>
     * <br>
     * 0: "ACTIVEPOWEREXTERNAL
     * 		+ NATURALGASPOWEREXTERNAL" : <br>
     * > sum of all activePowers * ACTIVEPOWEREXTERNAL-Price<br>
     * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
     * <br>
     * 1: "ACTIVEPOWEREXTERNAL
     * 		+ PVACTIVEPOWERFEEDIN
     * 		+ NATURALGASPOWEREXTERNAL" : <br>
     * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
     * > if (sum of all activePowers < 0) -> Math.max(pvPower,(sum of all activePowers)) * PVACTIVEPOWERFEEDIN<br>
     * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
     * <br>
     * 2: "ACTIVEPOWEREXTERNAL
     * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
     * 		+ NATURALGASPOWEREXTERNAL"<br>
     * > sum of all activePowers except PV * ACTIVEPOWEREXTERNAL-Price<br>
     * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
     * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
     * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
     * <br>
     * 3: "ACTIVEPOWEREXTERNAL
     * 		+ PVACTIVEPOWERFEEDIN
     * 		+ CHPACTIVEPOWERFEEDIN
     * 		+ NATURALGASPOWEREXTERNAL" : <br>
     * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
     * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
     * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
     * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
     * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
     * <br>
     * 4: "ACTIVEPOWEREXTERNAL
     * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
     * 		+ CHPACTIVEPOWERFEEDIN + CHPACTIVEPOWERAUTOCONSUMPTION
     * 		+ NATURALGASPOWEREXTERNAL"<br>
     * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
     * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
     * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
     * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
     * > chpPowerAutoConsumption * CHPACTIVEPOWERAUTOCONSUMPTION<br>
     * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
     * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
     * <br> */
    public static final int epsOptimizationObjective = 4;

    /*
     * Optimization Objective<br>
     * PowerLimitSignals (pls)<br>
     * determines which LBS-constraint violations are being priced<br>
     * 0: none<br>
     * 1: additional costs (overLimitFactor * ACTIVEPOWEREXTERNAL-price) for ACTIVEPOWEREXTERNAL limit violations<br>
     * 2: additional costs (overLimitFactor * POWEREXTERNAL-price) for ACTIVEPOWEREXTERNAL and REACTIVEPOWEREXTERNAL limit violations<br>
     * 3: ... */
    public static final int plsOptimizationObjective = 1;

    //currently not in use TODO: reactivePower pricing...
    public static final int varOptimizationObjective = 0;

    /*
     * TODO: merge with previous
     * Optimization Objective<br>
     * LBS<br>
     * determines additional costs if electricity consumption is above/below power limit<br>
     * 0: no additional costs
     * !0: additional costs */
    public static final double overLimitFactor = 1.0;
}
