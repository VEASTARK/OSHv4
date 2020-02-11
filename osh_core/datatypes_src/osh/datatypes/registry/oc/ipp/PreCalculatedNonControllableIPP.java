package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Simplified IPP for devices whose loads can be fully pre-calculated.
 *
 * @author Sebastian Kramer
 */
public abstract class PreCalculatedNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    /**
     * all pre-calculated output states of this problem-part
     */
    protected LimitedCommodityStateMap[] allOutputStates;

    protected long outputStatesCalculatedFor = Long.MIN_VALUE;
    protected long maxHorizon = Long.MIN_VALUE;

    /**
     * Constructs this simplified problem-part with the given information.
     *
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param deviceType the type of device that is represented by this problem-part
     * @param allOutputCommodities all possible commodities that can be emitted by this problem-part
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public PreCalculatedNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                timestamp,
                toBeScheduled,
                false,
                false,
                false, //is not static
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given simplified problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the simplified problem-part to copy
     */
    public PreCalculatedNonControllableIPP(PreCalculatedNonControllableIPP other) {
        super(other);
        this.allOutputStates = other.allOutputStates;
        this.maxHorizon = other.maxHorizon;
        this.outputStatesCalculatedFor = other.outputStatesCalculatedFor;
    }

    @Override
    public final void calculateNextStep() {
        int index = (int) ((this.getInterdependentTime() - this.outputStatesCalculatedFor) / this.getStepSize());
        if (index >= this.allOutputStates.length) {
            this.setOutputStates(null);
        } else {
            this.setOutputStates(this.allOutputStates[index]);
        }
        this.incrementInterdependentTime();
    }

    @Override
    public final void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.maxHorizon = maxHorizon;
    }
}
