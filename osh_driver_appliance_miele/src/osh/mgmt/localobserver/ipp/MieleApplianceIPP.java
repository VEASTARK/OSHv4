package osh.mgmt.localobserver.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableType;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a problem-part for a controllable miele device.
 *
 * @author Ingo Mauser
 */
public class MieleApplianceIPP extends ControllableIPP<ISolution, IPrediction> {

    private long earliestStartTime;
    private final long latestStartTime;
    private final boolean predicted;

    private final SparseLoadProfile profile;

    private LimitedCommodityStateMap[] allOutputStates;
    private long outputStatesCalculatedFor;

    /**
     * Constructs this controllable miele-ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param earliestStartTime the earliest starting-time of the device
     * @param latestStartTime the latest starting-time of the device
     * @param profile the profile of the device to be turned on
     * @param predicted flag if this is a real planned run or a predicted one
     * @param optimizationHorizon the optimization horizon
     * @param deviceType type of device represented by this problem-part
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public MieleApplianceIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            long earliestStartTime,
            long latestStartTime,
            SparseLoadProfile profile,
            boolean predicted,
            long optimizationHorizon,
            DeviceTypes deviceType,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter
                false, //does not react to input states
                optimizationHorizon,
                deviceType,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);

        Objects.requireNonNull(profile);

        this.earliestStartTime = earliestStartTime;
        this.latestStartTime = latestStartTime;
        this.profile = profile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
        this.predicted = predicted;

        this.updateSolutionInformation(this.getReferenceTime(), this.getOptimizationHorizon());
    }

    /**
     * Limited copy-constructor that constructs a copy of the given controllable miele-ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the controllable miele-ipp to copy
     */
    public MieleApplianceIPP(MieleApplianceIPP other) {
        super(other);

        this.earliestStartTime = other.earliestStartTime;
        this.latestStartTime = other.latestStartTime;
        this.profile = other.profile;
        this.predicted = other.predicted;
        this.allOutputStates = other.allOutputStates;
        this.outputStatesCalculatedFor = other.outputStatesCalculatedFor;
    }

    @Override
    public void initializeInterdependentCalculation(long interdependentStartingTime,
                                                    int stepSize, boolean createLoadProfile,
                                                    boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        SparseLoadProfile lp = this.profile.cloneWithOffset(this.earliestStartTime + this.getStartOffset(this.currentSolution));

        long time = interdependentStartingTime;
        ObjectArrayList<LimitedCommodityStateMap> tempOutputStates = new ObjectArrayList<>();

        while (time < lp.getEndingTimeOfProfile()) {
            LimitedCommodityStateMap output = null;
            double activePower = lp.getAverageLoadFromTill(Commodity.ACTIVEPOWER, time, time + stepSize);
            double reactivePower = lp.getAverageLoadFromTill(Commodity.REACTIVEPOWER, time, time + stepSize);

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

        this.outputStatesCalculatedFor = interdependentStartingTime;

        if (this.getLoadProfile() != null) {
            this.setLoadProfile(lp);
        }
    }

    private void updateSolutionInformation(long referenceTime, long maxHorizon) {

        long maxOffset = this.latestStartTime - this.earliestStartTime;

        this.solutionHandler.updateVariableInformation(VariableType.LONG, 1, new double[][]{{0, maxOffset}});
    }

    @Override
    protected void interpretNewSolution() {
        //do nothing, solution will be interpreted in initializeInterdependentCalculation
    }

    @Override
    public void calculateNextStep() {
        int index = (int) ((this.getInterdependentTime() - this.outputStatesCalculatedFor) / this.getStepSize());
        if (index < this.allOutputStates.length) {
            this.setOutputStates(this.allOutputStates[index]);
        } else {
            this.setOutputStates(null);
        }
        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(this.getLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype() {
        return new MieleSolution(this.getStartTime(this.currentSolution), this.predicted);
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        if (currentTime != this.getReferenceTime() || maxHorizon != this.getOptimizationHorizon()) {
            this.setReferenceTime(currentTime);
            this.setOptimizationHorizon(maxHorizon);
            if (this.earliestStartTime < currentTime) {
                this.earliestStartTime = Math.min(currentTime, this.latestStartTime);
            }
            this.updateSolutionInformation(currentTime, this.getOptimizationHorizon());
        }
    }

    @Override
    public String problemToString() {
        return "MieleIPP Profile: " + this.profile.toStringShort() + " DoF:" + this.earliestStartTime + "-" + this.latestStartTime
                + "(" + (this.latestStartTime - this.earliestStartTime) + ")" + (this.predicted ? " (predicted)" : "");
    }

    @Override
    public MieleApplianceIPP getClone() {
        return new MieleApplianceIPP(this);
    }

    public long getStartTime(DecodedSolutionWrapper solution) {
        return this.earliestStartTime + this.getStartOffset(solution);
    }

    private long getStartOffset(DecodedSolutionWrapper solution) {
        if (solution != null && solution.getLongArray() != null && solution.getLongArray().length == 1) {
            return solution.getLongArray()[0];
        } else {
            return 0;
        }
    }
}
