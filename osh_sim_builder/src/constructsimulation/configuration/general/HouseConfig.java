package constructsimulation.configuration.general;

import constructsimulation.configuration.OSH.GridConfigurationType;
import osh.configuration.system.RunningType;

import java.util.UUID;

/**
 * Represents basic configuration information about the house to construct.
 *
 * @author Sebastian Kramer
 */
public class HouseConfig {

    public static int personCount = 4;
    public static UUID hhUUID = UUID.randomUUID();
    public static RunningType runningType = RunningType.SIMULATION;

    public static Long mainRandomSeed = 0xd1ce5bL;
    public static Long optimizationMainRandomSeed = 0xd1ce5bL;

    public static String defaultLogPath = "logs";

    public static GridConfigurationType gridType = GridConfigurationType.SIMULATION;
}
