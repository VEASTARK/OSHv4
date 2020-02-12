package osh.mgmt.localobserver.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a specific, fully predicted problem-part for a miele household device.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class MieleApplianceNonControllableIPP extends PredictedNonControllableIPP {

    /**
     * Constructs this miele device problem-part.

     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param powerPrediction the predicted heating power profile
     * @param deviceType the type of device that is represented by this problem-part
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public MieleApplianceNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            SparseLoadProfile powerPrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                deviceType,
                powerPrediction,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given miele device problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the miele device problem-part to copy
     */
    public MieleApplianceNonControllableIPP(MieleApplianceNonControllableIPP other) {
        super(other);
    }

    @Override
    public MieleApplianceNonControllableIPP getClone() {
        return new MieleApplianceNonControllableIPP(this);
    }

    @Override
    public String problemToString() {
        if (this.predictedProfile.getEndingTimeOfProfile() != 0) {
            return "miele appliance running till " + this.predictedProfile.getEndingTimeOfProfile();
        } else {
            return "miele appliance not running";
        }
    }
}
