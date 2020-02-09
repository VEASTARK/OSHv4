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

    private static final long serialVersionUID = 8909519723717425836L;

    /**
     * No-arg constructor for serialization.
     */
    @Deprecated
    protected BaseloadIPP() {
        super();
    }

    /**
     * Constructs this baseload problem-part.
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param deviceType the type of device that is represented by this problem-part
     * @param baseload the predicted baseload power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public BaseloadIPP(
            UUID deviceId,
            boolean toBeScheduled,
            ZonedDateTime timestamp,
            DeviceTypes deviceType,
            SparseLoadProfile baseload,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                toBeScheduled,
                timestamp,
                deviceType,
                baseload,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

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
