package osh.mgmt.ipp.thermal;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PredictedNonControllableIPP;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a specific, fully predicted problem-part for the hot-water demand.
 *
 * @author Ingo Mauser, Jan Mueller, Sebastian Kramer
 */
public class ThermalDemandNonControllableIPP extends PredictedNonControllableIPP {

    private static final long serialVersionUID = -1011574853269626608L;

    /**
     * No-arg constructor for serialization.
     */
    @Deprecated
    protected ThermalDemandNonControllableIPP() {
        super();
    }

    /**
     * Constructs this hot-water-demand problem-part.
     *
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param referenceTime the starting-time this problem-part represents at the moment
     * @param deviceType the type of device that is represented by this problem-part
     * @param powerPrediction the predicted heating power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public ThermalDemandNonControllableIPP(
            UUID deviceId,
            boolean toBeScheduled,
            ZonedDateTime referenceTime,
            DeviceTypes deviceType,
            SparseLoadProfile powerPrediction,
            Commodity usedCommodity,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                toBeScheduled,
                referenceTime,
                deviceType,
                powerPrediction,
                EnumSet.of(usedCommodity),
                compressionType,
                compressionValue);
    }

    public ThermalDemandNonControllableIPP(ThermalDemandNonControllableIPP other) {
        super(other);
    }

    @Override
    public ThermalDemandNonControllableIPP getClone() {
        return new ThermalDemandNonControllableIPP(this);
    }

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] ThermalDemandNonControllableIPP";
    }
}