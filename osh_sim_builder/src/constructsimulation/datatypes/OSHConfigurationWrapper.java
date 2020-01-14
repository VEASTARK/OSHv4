package constructsimulation.datatypes;

import java.util.UUID;

/**
 * @author Ingo Mauser, Till Schuberth
 */
public class OSHConfigurationWrapper {

    public final int numberOfPersons;

    public final long mainRandomSeed;

    public final String logPath;

    public final UUID meterUUID;
    public final UUID hhUUID;

    public OSHConfigurationWrapper(
            int numberOfPersons,

            long mainRandomSeed,

            String logPath,

            UUID meterUUID,
            UUID hhUUID
    ) {

        this.numberOfPersons = numberOfPersons;

        this.mainRandomSeed = mainRandomSeed;

        this.logPath = logPath;

        this.meterUUID = meterUUID;
        this.hhUUID = hhUUID;
    }
}
