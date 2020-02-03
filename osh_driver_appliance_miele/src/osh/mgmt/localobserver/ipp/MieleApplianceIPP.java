package osh.mgmt.localobserver.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.BitSetConverter;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class MieleApplianceIPP extends ControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = -665656608383318760L;

    private long earliestStartTime;
    private long latestStartTime;
    private boolean predicted;

    private SparseLoadProfile profile;
    private SparseLoadProfile lp;

    private LimitedCommodityStateMap[] allOutputStates;
    private long outputStatesCalculatedFor;


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected MieleApplianceIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
     */
    public MieleApplianceIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timestamp,
            long earliestStartTime,
            long latestStartTime,
            SparseLoadProfile profile,
            boolean toBeScheduled,
            boolean predicted,
            long optimizationHorizon,
            DeviceTypes deviceType,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                timestamp,
                calculateBitCount(earliestStartTime, latestStartTime),
                toBeScheduled,
                false, //does not need ancillary meter
                false, //does not react to input states
                optimizationHorizon,
                timestamp.toEpochSecond(),
                deviceType,
                new Commodity[]{Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                compressionType,
                compressionValue);

        if (profile == null) {
            throw new NullPointerException("profile is null");
        }

        this.earliestStartTime = earliestStartTime;
        this.latestStartTime = latestStartTime;
        this.profile = profile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
        this.predicted = predicted;
    }

    /**
     * returns the needed amount of bits for the EA
     *
     * @param earliestStartTime
     * @param latestStartTime
     */
    private static int calculateBitCount(
            long earliestStartTime,
            long latestStartTime) {
        if (earliestStartTime > latestStartTime) {
            return 0;
        }

        long diff = latestStartTime - earliestStartTime + 1;

        return (int) Math.ceil(Math.log(diff) / Math.log(2));
    }

    @Override
    public void initializeInterdependentCalculation(long maxReferenceTime,
                                                    BitSet solution, int stepSize, boolean createLoadProfile,
                                                    boolean keepPrediction) {

        this.interdependentTime = maxReferenceTime;
        this.stepSize = stepSize;

        this.lp = this.profile.cloneWithOffset(this.earliestStartTime + this.getStartOffset(solution));

        long time = maxReferenceTime;
        ObjectArrayList<LimitedCommodityStateMap> tempOutputStates = new ObjectArrayList<>();

        while (time < this.lp.getEndingTimeOfProfile()) {
            LimitedCommodityStateMap output = null;
            double activePower = this.lp.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
            double reactivePower = this.lp.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);

            if (activePower != 0.0 || reactivePower != 0) {
                output = new LimitedCommodityStateMap(this.allOutputCommodities);
                output.setPower(Commodity.ACTIVEPOWER, activePower);
                output.setPower(Commodity.REACTIVEPOWER, reactivePower);
            }
            tempOutputStates.add(output);
            time += stepSize;
        }
        //add zero if optimization goes longer then the profile
        LimitedCommodityStateMap output = new LimitedCommodityStateMap(this.allOutputCommodities);
        output.setPower(Commodity.ACTIVEPOWER, 0.0);
        output.setPower(Commodity.REACTIVEPOWER, 0.0);
        tempOutputStates.add(output);

        this.allOutputStates = new LimitedCommodityStateMap[tempOutputStates.size()];
        this.allOutputStates = tempOutputStates.toArray(this.allOutputStates);

        this.outputStatesCalculatedFor = maxReferenceTime;

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
    public String solutionToString(BitSet bits) {
        if (bits == null)
            return "ERROR: no solution bits";
        return "start time: " + this.getStartTime(bits);
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(this.lp, 0.0, this.getDeviceType().toString());
    }

    @Override
    public ISolution transformToPhenotype(BitSet solution) {
        return new MieleSolution(this.getStartTime(solution), this.predicted);
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype(BitSet solution) {
        return this.transformToPhenotype(solution);
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        if (this.earliestStartTime < currentTime) {
            this.earliestStartTime = Math.min(currentTime, this.latestStartTime);
            this.setBitCount(calculateBitCount(
                    this.earliestStartTime,
                    this.latestStartTime));
        }
    }

    @Override
    public String problemToString() {
        return "MieleIPP Profile: " + this.profile.toStringShort() + " DoF:" + this.earliestStartTime + "-" + this.latestStartTime
                + "(" + (this.latestStartTime - this.earliestStartTime) + ")" + (this.predicted ? " (predicted)" : "");
    }

    public long getStartTime(BitSet solution) {
        return this.earliestStartTime + this.getStartOffset(solution);
    }

    private long getStartOffset(BitSet solution) {
        long maxOffset = this.latestStartTime - this.earliestStartTime;
        return (long) Math.floor(BitSetConverter.gray2long(solution)
                / Math.pow(2, this.getBitCount()) * maxOffset);
    }
}
