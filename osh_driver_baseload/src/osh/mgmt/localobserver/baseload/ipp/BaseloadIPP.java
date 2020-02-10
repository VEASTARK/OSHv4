package osh.mgmt.localobserver.baseload.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a specific problem-part for the baseload.
 *
 * @author Sebastian Kramer
 */
public class BaseloadIPP extends PredictedNonControllableIPP {

    /**
     * Constructs this baseload problem-part.
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param deviceType the type of device that is represented by this problem-part
     * @param baseload the predicted baseload power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public BaseloadIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            SparseLoadProfile baseload,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                timestamp,
                toBeScheduled,
                deviceType,
                baseload,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given baseload-ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the baseload-ipp to copy
     */
    public BaseloadIPP(BaseloadIPP other) {
        super(other);
    }

    @Override
    public BaseloadIPP getClone() {
        return new BaseloadIPP(this);
    }

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] BaseloadIPP";
    }
}
