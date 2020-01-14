package osh.mgmt.localobserver.baseload.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.esc.LimitedCommodityStateMap;

import java.util.BitSet;
import java.util.UUID;

/**
 * IPP for the Baseload
 *
 * @author Sebastian Kramer
 */

public class BaseloadIPP extends NonControllableIPP<ISolution, IPrediction> {

    /**
     *
     */
    private static final long serialVersionUID = 7545773976205881129L;
    protected LimitedCommodityStateMap[] allOutputStates;
    private SparseLoadProfile baseload;
    private SparseLoadProfile lp;
    private long maxHorizon = Long.MIN_VALUE;
    private long outputStatesCalculatedFor = Long.MIN_VALUE;


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected BaseloadIPP() {
        super();
    }

    public BaseloadIPP(
            UUID deviceId,
            IGlobalLogger logger,
            long timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            long referenceTime,
            SparseLoadProfile baseload,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                logger,
                toBeScheduled,
                false,    //does not need ancillary meter
                false,    //does not react to input states
                false, //is not static
                referenceTime,
                deviceType,
                new Commodity[]{
                        Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER
                },
                compressionType,
                compressionValue);

        this.baseload = baseload.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
    }

    @Override
    public void initializeInterdependentCalculation(long maxReferenceTime,
                                                    BitSet solution,
                                                    int stepSize,
                                                    boolean createLoadProfile,
                                                    boolean keepPrediction) {

        if (maxReferenceTime != this.getReferenceTime()) {
            this.interdependentTime = maxReferenceTime;
        } else
            this.interdependentTime = this.getReferenceTime();

        this.stepSize = stepSize;

        if (createLoadProfile)
            this.lp = this.baseload.cloneAfter(maxReferenceTime);
        else
            this.lp = null;

        if (this.outputStatesCalculatedFor != maxReferenceTime) {
            long time = maxReferenceTime;
            ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<>();

            while (time < this.maxHorizon) {
                LimitedCommodityStateMap output = null;

                double actPower = this.baseload.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
                double reactPower = this.baseload.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);
                if (actPower != 0 || reactPower != 0) {
                    output = new LimitedCommodityStateMap(this.allOutputCommodities);
                    output.setPower(Commodity.ACTIVEPOWER, actPower);

                    output.setPower(Commodity.REACTIVEPOWER, reactPower);
                }
                tempAllOutputStates.add(output);

                time += stepSize;
            }
            //add zero if optimisation goes longer then the profile
            LimitedCommodityStateMap output = new LimitedCommodityStateMap(this.allOutputCommodities);
            output.setPower(Commodity.ACTIVEPOWER, 0.0);
            output.setPower(Commodity.REACTIVEPOWER, 0.0);
            tempAllOutputStates.add(output);

            this.allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
            this.allOutputStates = tempAllOutputStates.toArray(this.allOutputStates);

            this.outputStatesCalculatedFor = maxReferenceTime;
        }

        this.setOutputStates(null);
    }

    @Override
    public void calculateNextStep() {
        int index = (int) ((this.interdependentTime - this.outputStatesCalculatedFor) / this.stepSize);
        if (index < this.allOutputStates.length) {
            this.setOutputStates(this.allOutputStates[index]);
        } else {
            this.setOutputStates(null);
        }
        this.interdependentTime += this.stepSize;
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.lp != null) {
            if (this.lp.getEndingTimeOfProfile() > 0 && this.lp.getEndingTimeOfProfile() < this.interdependentTime) {
                this.lp.setLoad(
                        Commodity.ACTIVEPOWER,
                        this.interdependentTime,
                        0);
                this.lp.setLoad(
                        Commodity.REACTIVEPOWER,
                        this.interdependentTime,
                        0);
            }
            return new Schedule(this.lp, 0.0, this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), 0.0, this.getDeviceType().toString());
        }
    }

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] BaseloadIPP";
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.maxHorizon = maxHorizon;
    }

}
