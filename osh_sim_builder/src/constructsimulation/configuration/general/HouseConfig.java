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
    public static final UUID hhUUID = UUID.randomUUID();
    public static final RunningType runningType = RunningType.SIMULATION;

    public static final Long mainRandomSeed = 0xd1ce5bL;
    public static final Long optimizationMainRandomSeed = 0xd1ce5bL;

    public static final String defaultLogPath = "logs";

    public static final GridConfigurationType gridType = GridConfigurationType.SIMULATION;
}
