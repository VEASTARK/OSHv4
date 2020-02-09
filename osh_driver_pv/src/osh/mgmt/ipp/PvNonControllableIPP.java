package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a specific, fully predicted problem-part for a pv-device.
 *
 * @author Sebastian Kramer, Ingo Mauser, Till Schuberth
 */
public class PvNonControllableIPP extends PredictedNonControllableIPP {

    private static final long serialVersionUID = 4737054684215212047L;

    /**
     * No-arg constructor for serialization.
     */
    @Deprecated
    protected PvNonControllableIPP() {
        super();
    }

    /**
     * Constructs this miele device problem-part.

     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param predictedPVProfile the predicted heating power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public PvNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            SparseLoadProfile predictedPVProfile,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(deviceId,
                false,
                timestamp,
                DeviceTypes.PVSYSTEM,
                predictedPVProfile,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

    public PvNonControllableIPP(PvNonControllableIPP other) {
        super(other);
    }

    @Override
    public PvNonControllableIPP getClone() {
        return new PvNonControllableIPP(this);
    }

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] PvIPP";
    }
}
