package osh.core;

import osh.core.interfaces.IOSHStatus;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class OSHStatus implements IOSHStatus {

    boolean showSolverDebugMessages;
    private UUID hhUUID;
    private String runID;
    private String configurationID;
    private String logDir;
    private Boolean isSimulation;
    private Boolean isVirtual;
    private Boolean hasGUI;

    @Override
    public String getRunID() {
        return this.runID;
    }

    public void setRunID(String runID) {
        this.runID = runID;
    }

    @Override
    public String getConfigurationID() {
        return this.configurationID;
    }

    public void setConfigurationID(String configurationID) {
        this.configurationID = configurationID;
    }

    @Override
    public String getLogDir() {
        return this.logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    @Override
    public UUID getHhUUID() {
        return this.hhUUID;
    }

    public void setHhUUID(UUID hhUUID) {
        this.hhUUID = hhUUID;
    }

    @Override
    public boolean isSimulation() {
        return this.isSimulation;
    }

    public void setIsSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

    @Override
    public boolean isRunningVirtual() {
        return this.isVirtual;
    }

    public void setVirtual(boolean virtual) {
        this.isVirtual = virtual;
    }


    @Override
    public boolean getShowSolverDebugMessages() {
        return this.showSolverDebugMessages;
    }


    @Override
    public boolean hasGUI() {
        return this.hasGUI;
    }

    public void setIsGUI(boolean hasGUI) {
        this.hasGUI = hasGUI;
    }
}
