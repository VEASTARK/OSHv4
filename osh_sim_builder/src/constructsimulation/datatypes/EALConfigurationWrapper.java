package constructsimulation.datatypes;

import osh.configuration.system.RunningType;

/**
 * @author Ingo Mauser, Till Schuberth
 */
public class EALConfigurationWrapper {

    public RunningType runningType;
    public final int numberOfPersons;
    public final String h0Filename;

    public final boolean simLogger;

    public final String loggerBusDriverClassName;
    public final String loggerBusManagerClassName;

    public EALConfigurationWrapper(
            int numberOfPersons,
            String h0Filename,
            boolean simLogger,
            String loggerBusDriverClassName,
            String loggerBusManagerClassName
    ) {

        this.numberOfPersons = numberOfPersons;
        this.h0Filename = h0Filename;

        this.simLogger = simLogger;
        this.loggerBusDriverClassName = loggerBusDriverClassName;
        this.loggerBusManagerClassName = loggerBusManagerClassName;
    }
}
