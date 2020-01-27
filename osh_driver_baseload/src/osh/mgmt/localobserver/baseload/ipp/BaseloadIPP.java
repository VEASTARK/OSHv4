package osh.mgmt.localobserver.baseload.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

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
     *  @param deviceId the identifier of the devide that is represented by this problem-part
     * @param logger the global logger
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param referenceTime the starting-time this problem-part represents at the moment
     * @param deviceType the type of device that is represented by this problem-part
     * @param baseload the predicted baseload power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public BaseloadIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            long referenceTime,
            DeviceTypes deviceType,
            SparseLoadProfile baseload,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                logger,
                toBeScheduled,
                referenceTime,
                deviceType,
                baseload,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);
    }

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] BaseloadIPP";
    }
}
