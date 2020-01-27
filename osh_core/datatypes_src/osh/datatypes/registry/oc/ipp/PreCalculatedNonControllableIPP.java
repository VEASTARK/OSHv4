package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.esc.LimitedCommodityStateMap;

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
     *
     */
    private static final long serialVersionUID = 6115879812569415975L;

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
     * @param logger the global logger
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param referenceTime the starting-time this problem-part represents at the moment
     * @param deviceType the type of device that is represented by this problem-part
     * @param allOutputCommodities all possible commodities that can be emitted by this problem-part
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public PreCalculatedNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            long referenceTime,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                toBeScheduled,
                false,
                false,
                false, //is not static
                referenceTime,
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue);
    }

    /**
     * No-arg constructor for serialization.
     */
    @Deprecated
    protected PreCalculatedNonControllableIPP() {
        super();
    }


    @Override
    public void calculateNextStep() {
        int index = (int) ((this.getInterdependentTime() - this.outputStatesCalculatedFor) / this.getStepSize());
        if (index >= this.allOutputStates.length) {
            this.setOutputStates(null);
        } else {
            this.setOutputStates(this.allOutputStates[index]);
        }
        this.incrementInterdependentTime();
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.maxHorizon = maxHorizon;
    }
}
