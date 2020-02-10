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

    /**
     * Constructs this pv problem-part.

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
                timestamp,
                false,
                DeviceTypes.PVSYSTEM,
                predictedPVProfile,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given pv problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the pv problem-part to copy
     */
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
