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
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableType;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Objects;
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
                toBeScheduled,
                false, //does not need ancillary meter
                false, //does not react to input states
                optimizationHorizon,
                timestamp.toEpochSecond(),
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

    @Override
    public void initializeInterdependentCalculation(long maxReferenceTime,
                                                    int stepSize, boolean createLoadProfile,
                                                    boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        SparseLoadProfile lp = this.profile.cloneWithOffset(this.earliestStartTime + this.getStartOffset(this.currentSolution));

        long time = maxReferenceTime;
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

        this.outputStatesCalculatedFor = maxReferenceTime;

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
    public String solutionToString() {
        if (this.currentSolution == null)
            return "ERROR: no solution";
        return "start time: " +  this.getStartTime(this.currentSolution);
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(this.getLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    @Override
    public ISolution transformToPhenotype(DecodedSolutionWrapper solution) {
        return new MieleSolution(this.getStartTime(solution), this.predicted);
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype() {
        return this.transformToPhenotype(this.currentSolution);
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.setOptimizationHorizon(maxHorizon);
        if (this.earliestStartTime < currentTime) {
            this.earliestStartTime = Math.min(currentTime, this.latestStartTime);
        }
        this.updateSolutionInformation(currentTime, this.getOptimizationHorizon());
    }

    @Override
    public String problemToString() {
        return "MieleIPP Profile: " + this.profile.toStringShort() + " DoF:" + this.earliestStartTime + "-" + this.latestStartTime
                + "(" + (this.latestStartTime - this.earliestStartTime) + ")" + (this.predicted ? " (predicted)" : "");
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
