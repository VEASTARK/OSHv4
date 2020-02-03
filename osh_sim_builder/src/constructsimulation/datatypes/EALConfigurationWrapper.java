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

    public EALConfigurationWrapper(
            int numberOfPersons,
            String h0Filename,
            boolean simLogger
    ) {

        this.numberOfPersons = numberOfPersons;
        this.h0Filename = h0Filename;

        this.simLogger = simLogger;
    }
}
