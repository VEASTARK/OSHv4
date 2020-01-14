package constructsimulation.datatypes;

import osh.configuration.oc.GAConfiguration;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class OCConfigurationWrapper {

    public final long optimizationMainRandomSeed;

    public final String globalObserverClassName;
    public final String globalControllerClassName;

    public final int epsOptimizationObjective;
    public final int plsOptimizationObjective;
    public final int varOptimizationObjective;

    public final double upperOverlimitFactor;
    public final double lowerOverlimitFactor;

    public String h0Filename;

    public final GAConfiguration gaConfiguration;

    public final int stepSize;

    public final UUID hotWaterTankUUID;

    public OCConfigurationWrapper(
            long optimizationMainRandomSeed,

            String globalObserverClassName,
            String globalControllerClassName,

            int epsOptimizationObjective,

            int plsOptimizationObjective,
            int varOptimizationObjective,
            double upperOverlimitFactor,
            double lowerOverlimitFactor,

            GAConfiguration gaConfiguration,
            int stepSize,
            UUID hotWaterTankUUID
    ) {

        this.optimizationMainRandomSeed = optimizationMainRandomSeed;

        this.globalObserverClassName = globalObserverClassName;
        this.globalControllerClassName = globalControllerClassName;

        this.epsOptimizationObjective = epsOptimizationObjective;

        this.plsOptimizationObjective = plsOptimizationObjective;
        this.varOptimizationObjective = varOptimizationObjective;
        this.upperOverlimitFactor = upperOverlimitFactor;
        this.lowerOverlimitFactor = lowerOverlimitFactor;


        this.gaConfiguration = gaConfiguration;

        this.stepSize = stepSize;

        this.hotWaterTankUUID = hotWaterTankUUID;
    }
}
