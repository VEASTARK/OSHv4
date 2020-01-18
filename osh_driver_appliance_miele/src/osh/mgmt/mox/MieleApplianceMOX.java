package osh.mgmt.mox;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.en50523.EN50523DeviceState;

/**
 * @author Sebastian Kramer
 */
public class MieleApplianceMOX implements IModelOfObservationExchange {

    /**
     * SparseLoadProfile containing different profile with different commodities<br>
     * IMPORATANT: RELATIVE TIMES!
     */
    private final SparseLoadProfile currentProfile;
    private final EN50523DeviceState currentState;

    private final long profileStarted;
    private final long programmedAt;

    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;

    public MieleApplianceMOX(SparseLoadProfile currentProfile, EN50523DeviceState currentState,
                             long profileStarted, long programmedAt, LoadProfileCompressionTypes compressionType,
                             int compressionValue) {
        super();
        this.currentProfile = currentProfile;
        this.currentState = currentState;
        this.profileStarted = profileStarted;
        this.programmedAt = programmedAt;
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }

    public SparseLoadProfile getCurrentProfile() {
        return this.currentProfile;
    }

    public EN50523DeviceState getCurrentState() {
        return this.currentState;
    }

    public long getProfileStarted() {
        return this.profileStarted;
    }

    public long getProgrammedAt() {
        return this.programmedAt;
    }

    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }

    public int getCompressionValue() {
        return this.compressionValue;
    }


}
