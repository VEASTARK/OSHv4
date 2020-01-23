package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class ThermalDemandNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = -4181010416941022236L;

    // ### interdependent stuff ###

    /**
     * used for iteration in interdependent calculation (ancillary time in the future)
     */
    protected long interdependentTime;
    /**
     * running times of chiller
     */
    protected double interdependentCervisia;

    protected LimitedCommodityStateMap[] allOutputStates;

    protected long outputStatesCalculatedFor = Long.MIN_VALUE;
    protected long maxHorizon = Long.MIN_VALUE;

    protected SparseLoadProfile lp;


    /**
     * CONSTRUCTOR
     */
    public ThermalDemandNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            ZonedDateTime referenceTime,
            DeviceTypes deviceType,
            Commodity[] allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                toBeScheduled,
                needsAncillaryMeterState,
                reactsToInputStates,
                false, //is not static
                referenceTime,
                deviceType,
                allOutputCommodities,
                compressionType,
                compressionValue);
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected ThermalDemandNonControllableIPP() {
        super();
    }


    @Override
    public void calculateNextStep() {
        int index = (int) ((this.interdependentTime - this.outputStatesCalculatedFor) / this.stepSize);
        if (index >= this.allOutputStates.length) {
            this.setOutputStates(null);
        } else {
            this.setOutputStates(this.allOutputStates[index]);
        }
        this.interdependentTime += this.stepSize;
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.maxHorizon = maxHorizon;
    }
}
