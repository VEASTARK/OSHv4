package osh.core.interfaces;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser, Sebastian Kramer
 */
public interface IOSHStatus {

    String getRunID();

    String getConfigurationID();

    String getLogDir();

    UUID getHhUUID();


    boolean isSimulation();

    boolean isRunningVirtual();

    boolean getShowSolverDebugMessages();

    boolean hasGUI();

}
