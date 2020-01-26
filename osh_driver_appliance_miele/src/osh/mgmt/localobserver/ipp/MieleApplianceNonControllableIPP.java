package osh.mgmt.localobserver.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

import java.util.UUID;

/**
 * Represents a specific, fully predicted problem-part for a miele household device.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class MieleApplianceNonControllableIPP extends PredictedNonControllableIPP {

    private static final long serialVersionUID = -6645742166303833918L;

    /**
     * No-arg constructor for serialization.
     */
    @Deprecated
    protected MieleApplianceNonControllableIPP() {
        super();
    }

    /**
     * Constructs this miele device problem-part.

     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param logger the global logger
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param referenceTime the starting-time this problem-part represents at the moment
     * @param powerPrediction the predicted heating power profile
     * @param deviceType the type of device that is represented by this problem-part
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public MieleApplianceNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            long referenceTime,
            DeviceTypes deviceType,
            SparseLoadProfile powerPrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                toBeScheduled,
                referenceTime,
                deviceType,
                powerPrediction,
                new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                compressionType,
                compressionValue);
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
