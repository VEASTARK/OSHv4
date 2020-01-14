package osh.mgmt.mox;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.en50523.EN50523DeviceState;

import java.util.UUID;

/**
 * @author Ingo Mauser, Matthias Maerz
 */
public class GenericApplianceMOX implements IModelOfObservationExchange {

    /**
     * Required for:<br>
     * - Decision: static / dynamic profile
     */
    private final EN50523DeviceState currentState;

    private ApplianceProgramConfigurationStatus acp;
    private final UUID acpID;
    private final Long acpReferenceTime;

    private Long dof;

    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;


    /**
     * CONSTRUCTOR
     *
     */
    public GenericApplianceMOX(
            EN50523DeviceState currentState,
            ApplianceProgramConfigurationStatus acp,
            UUID acpID,
            Long acpReferenceTime,
            Long dof,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        this.currentState = currentState;

        if (acp != null) {
            this.acp = (ApplianceProgramConfigurationStatus) acp.clone();
        }

        this.acpID = acpID;
        this.acpReferenceTime = acpReferenceTime;

        this.compressionType = compressionType;
        this.compressionValue = compressionValue;

        this.dof = dof;

    }

    public EN50523DeviceState getCurrentState() {
        return this.currentState;
    }

    /**
     * != null IFF something changed
     *
     * @return
     */
    public ApplianceProgramConfigurationStatus getAcp() {
        return this.acp;
    }

    public UUID getAcpID() {
        return this.acpID;
    }

    public Long getAcpReferenceTime() {
        return this.acpReferenceTime;
    }

    public Long getDof() {
        return this.dof;
    }

    public void setDof(Long dof) {
        this.dof = dof;
    }

    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }

    public int getCompressionValue() {
        return this.compressionValue;
    }
}
