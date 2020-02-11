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
 * Represents a specific, fully predicted problem-part for a thermal demand.
 *
 * @author Ingo Mauser, Jan Mueller, Sebastian Kramer
 */
public class ThermalDemandNonControllableIPP extends PredictedNonControllableIPP {

    /**
     * Constructs this thermal-demand problem-part.
     *
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param deviceType the type of device that is represented by this problem-part
     * @param powerPrediction the predicted heating power profile
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public ThermalDemandNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            SparseLoadProfile powerPrediction,
            Commodity usedCommodity,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                timestamp,
                toBeScheduled,
                deviceType,
                powerPrediction,
                EnumSet.of(usedCommodity),
                compressionType,
                compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given thermal-demand problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the thermal-demand problem-part to copy
     */
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