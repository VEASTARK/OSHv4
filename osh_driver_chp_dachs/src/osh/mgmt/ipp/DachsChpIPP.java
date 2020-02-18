package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.BinaryBiStateVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.RealSimulatedBiStateTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableType;
import osh.datatypes.time.Activation;
import osh.datatypes.time.ActivationList;
import osh.driver.chp.model.GenericChpModel;
import osh.utils.time.TimeConversion;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Represents a problem-part for a controllable dachs combinded-heating-plant (chp).
 *
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Sebastian Kramer
 */
public class DachsChpIPP
        extends ControllableIPP<ISolution, IPrediction> {

    /**
     * slot length in [s]
     */
    private final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
    private final static int BITS_PER_ACTIVATION = 4;
    private final boolean initialState;
    /**
     * minimum running time of CHP in seconds
     */
    private final int minRunTime;
    // fixed costs per start, i.e., costs to turn on the CHP
    // (not the variable costs for letting the CHP run)
    private final double fixedCostPerStart;
    private final double forcedOnOffStepMultiplier;
    private final int forcedOffAdditionalCost;
    private final double chpOnCervisiaStepSizeMultiplier;
    //TODO add maxRunTime
    // temperature control
    private final double hotWaterStorageMinTemp;
    private final double hotWaterStorageMaxTemp;
    private final double hysteresis;

    private double currentWaterTemperature;

    private ArrayList<Activation> interdependentStartingTimes;
    private long interdependentTimeOfFirstBit;
    private boolean interdependentLastState;

    private boolean[] ab;

    private final GenericChpModel masterModel;
    private GenericChpModel actualModel;

    /**
     * Constructs this controllable chp-ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param initialState the initial operating state of the chp
     * @param minRunTime the minimum time the chp needs to stay on
     * @param chpModel a model of the chp
     * @param relativeHorizon the relative optimization horizon
     * @param hotWaterStorageMinTemp the minimum hot-water temperature needed to kept
     * @param hotWaterStorageMaxTemp the maximum hot-water temperature allowed
     * @param hysteresis the temperature above the minimum temeperature the chp will be kept on to reach
     * @param currentWaterTemperature the current hot-water temperature
     * @param fixedCostPerStart the additional cervisia costs per start of the chp
     * @param forcedOnOffStepMultiplier the additional cervisia costs per forced on/off per step-size
     * @param forcedOffAdditionalCost the additional cervisia costs per forced off
     * @param chpOnCervisiaStepSizeMultiplier the additional cervisia costs when on per step-size
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public DachsChpIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            boolean initialState,
            int minRunTime,
            GenericChpModel chpModel,
            int relativeHorizon,
            double hotWaterStorageMinTemp,
            double hotWaterStorageMaxTemp,
            double hysteresis,
            double currentWaterTemperature,
            double fixedCostPerStart,
            double forcedOnOffStepMultiplier,
            int forcedOffAdditionalCost,
            double chpOnCervisiaStepSizeMultiplier,
            LoadProfileCompressionTypes compressionType,
            int compressionValue
    ) {
        super(deviceId,
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                timestamp.toEpochSecond() + relativeHorizon,
                DeviceTypes.CHPPLANT,
                EnumSet.of(Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER),
                EnumSet.of(Commodity.HEATINGHOTWATERPOWER),
                compressionType,
                compressionValue,
                new BinaryBiStateVariableTranslator(BITS_PER_ACTIVATION),
                new RealSimulatedBiStateTranslator(BITS_PER_ACTIVATION));

        this.initialState = initialState;
        this.minRunTime = minRunTime;

        this.hotWaterStorageMinTemp = hotWaterStorageMinTemp;
        this.hotWaterStorageMaxTemp = hotWaterStorageMaxTemp;
        this.hysteresis = hysteresis;
        this.currentWaterTemperature = currentWaterTemperature;

        this.masterModel = chpModel;

        this.fixedCostPerStart = fixedCostPerStart;
        this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
        this.forcedOffAdditionalCost = forcedOffAdditionalCost;
        this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;

        this.updateSolutionInformation(this.getReferenceTime(), this.getOptimizationHorizon());
    }

    /**
     * Limited copy-constructor that constructs a copy of the given controllable chp-ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the controllable chp-ipp to copy
     */
    public DachsChpIPP(DachsChpIPP other) {
        super(other);
        this.initialState = other.initialState;
        this.minRunTime = other.minRunTime;

        this.hotWaterStorageMinTemp = other.hotWaterStorageMinTemp;
        this.hotWaterStorageMaxTemp = other.hotWaterStorageMaxTemp;
        this.hysteresis = other.hysteresis;
        this.currentWaterTemperature = other.currentWaterTemperature;

        this.fixedCostPerStart = other.fixedCostPerStart;
        this.forcedOnOffStepMultiplier = other.forcedOnOffStepMultiplier;
        this.forcedOffAdditionalCost = other.forcedOffAdditionalCost;
        this.chpOnCervisiaStepSizeMultiplier = other.chpOnCervisiaStepSizeMultiplier;

        this.interdependentStartingTimes = null;
        this.interdependentTimeOfFirstBit = other.interdependentTimeOfFirstBit;
        this.interdependentLastState = other.interdependentLastState;

        this.ab = null;

        this.masterModel = other.masterModel.clone();
        this.actualModel = null;
    }

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        this.interdependentStartingTimes = null;
        this.interdependentTimeOfFirstBit = this.getReferenceTime();

        this.interdependentLastState = this.initialState;

        this.actualModel = this.masterModel.clone();
    }

    private void updateSolutionInformation(long referenceTime, long maxHorizon) {
        //TODO: change to Math.ceil as soon as backwards compatibility is broken by another update
        int slots = (int) Math.round(((double) (maxHorizon - referenceTime)) / ((float) TIME_PER_SLOT));
        double[][] boundarys = new double[slots][];

        for (int i = 0; i < slots; i++) {
            boundarys[i] = new double[]{0, 2};
        }

        this.solutionHandler.updateVariableInformation(VariableType.LONG, slots, boundarys);
    }

    @Override
    protected void interpretNewSolution() {
        this.ab = this.getActivationBits(this.getReferenceTime(), this.currentSolution);
    }

    @Override
    public void calculateNextStep() {

        this.currentWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);


        // ### interdependent logic (repair functionality) ###
        // hot water temperature control (min, max)
        // i.e., CHP control (forced on/off)
        boolean chpOn = false;
        boolean plannedState = false;
        boolean hysteresisOn = false;

        int i = (int) ((this.getInterdependentTime() - this.interdependentTimeOfFirstBit) / TIME_PER_SLOT);

        if (i < this.ab.length) {
            chpOn = this.ab[i];
            plannedState = chpOn;
        }

        // temperature control
        {
            if (chpOn) {
                //planned state: on
                if (this.currentWaterTemperature > this.hotWaterStorageMaxTemp) {
                    // hot water too hot -> OFF
                    chpOn = false;

                }
            } else {
                //planned state: off
                if (this.currentWaterTemperature <= this.hotWaterStorageMinTemp) {
                    // hot water too cold -> ON
                    chpOn = true;
                } else if (this.interdependentLastState && this.currentWaterTemperature <= this.hotWaterStorageMinTemp + this.hysteresis) {
                    //hysteresis keep on
                    chpOn = true;
                    hysteresisOn = true;
                }
            }

            // either forced on or forced off
            if (chpOn != plannedState && !hysteresisOn) {
                //avoid forced on/offs
                this.addInterdependentCervisia(this.forcedOnOffStepMultiplier * this.getStepSize() + this.forcedOffAdditionalCost);
            }
        }

        //ignore shutOff when minRunTime is not reached (only when not forced off)
        if (!chpOn
                && !plannedState
                && this.interdependentLastState
                && (this.getInterdependentTime() - this.actualModel.getRunningSince()) < this.minRunTime) {
            chpOn = true;
        }

        //switched on or off
        if (chpOn != this.interdependentLastState) {
            this.actualModel.setRunning(chpOn, this.getInterdependentTime());
        }

        this.actualModel.calcPowerAvg(this.getInterdependentTime(), this.getInterdependentTime() + this.getStepSize());

        int activePower = this.actualModel.getAvgActualActivePower();
        int reactivePower = this.actualModel.getAvgActualReactivePower();
        int thermalPower = this.actualModel.getAvgActualThermalPower();
        int gasPower = this.actualModel.getAvgActualGasPower();

        // set power
        if (this.getLoadProfile() != null) {
            this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), activePower);
            this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), reactivePower);
            this.getLoadProfile().setLoad(Commodity.NATURALGASPOWER, this.getInterdependentTime(), gasPower);
            this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), thermalPower);
        }

        boolean hasValues = false;

        if (activePower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.ACTIVEPOWER);
        }

        if (reactivePower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.REACTIVEPOWER, reactivePower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.REACTIVEPOWER);
        }

        if (thermalPower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.HEATINGHOTWATERPOWER, thermalPower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.HEATINGHOTWATERPOWER);
        }

        if (gasPower != 0) {
            this.internalInterdependentOutputStates.setPower(Commodity.NATURALGASPOWER, gasPower);
            hasValues = true;
        } else {
            this.internalInterdependentOutputStates.resetCommodity(Commodity.NATURALGASPOWER);
        }

        if (hasValues) {
            this.setOutputStates(this.internalInterdependentOutputStates);
        } else {
            this.setOutputStates(null);
        }

        this.interdependentLastState = chpOn;
        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), 0);
            this.getLoadProfile().setLoad(Commodity.REACTIVEPOWER, this.getInterdependentTime(), 0);
            this.getLoadProfile().setLoad(Commodity.NATURALGASPOWER, this.getInterdependentTime(), 0);
            this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), 0);
            this.getLoadProfile().setEndingTimeOfProfile(this.getInterdependentTime() + 1);

            return new Schedule(
                    this.getLoadProfile().getCompressedProfile(
                            this.compressionType,
                            this.compressionValue,
                            this.compressionValue),
                    this.getInterdependentCervisia(),
                    this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype() {

        boolean[] ab = this.getActivationBits(this.interdependentTimeOfFirstBit, this.currentSolution);

        this.interdependentStartingTimes = new ArrayList<>();
        long timeOfFirstBit = this.getReferenceTime();
        Activation currentActivation = null;

        long duration = 0;
        for (int i = 0; i < ab.length; i++) {
            if (ab[i]) {
                // turn on
                if (currentActivation == null) {
                    currentActivation = new Activation();
                    currentActivation.startTime =
                            TimeConversion.convertUnixTimeToZonedDateTime(timeOfFirstBit + i * TIME_PER_SLOT);
                    duration = TIME_PER_SLOT;
                } else {
                    duration += TIME_PER_SLOT;
                }
            } else {
                // turn off
                if (currentActivation != null) {
                    currentActivation.duration = Duration.ofSeconds(duration);
                    duration = 0;
                    this.interdependentStartingTimes.add(currentActivation);
                    currentActivation = null;
                }
            }
        }

        if (currentActivation != null) {
            currentActivation.duration = Duration.ofSeconds(duration);
            this.interdependentStartingTimes.add(currentActivation);
        }

        if (this.interdependentStartingTimes != null) {
            ActivationList chpPhenotype = new ActivationList();
            chpPhenotype.setList(this.interdependentStartingTimes);
            return chpPhenotype;
        } else {
            return null;
        }
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        if (currentTime != this.getReferenceTime() || maxHorizon != this.getOptimizationHorizon()) {
            this.setReferenceTime(currentTime);
            this.setOptimizationHorizon(maxHorizon);
            this.updateSolutionInformation(this.getReferenceTime(), this.getOptimizationHorizon());
        }
    }

    private boolean[] getActivationBits(
            long now,
            DecodedSolutionWrapper solution) {

        long[] solutionArray = solution.getLongArray();
        boolean[] ret = new boolean[solutionArray.length];

        long runningFor = 0;
        boolean currentState = this.initialState;

        if (this.initialState) {
            runningFor = this.masterModel.getRunningForAtTimestamp(now);
        }

        for (int i = 0; i < solutionArray.length; i++) {

            if (solutionArray[i] == 2
                    || (solutionArray[i] == 0 && !currentState)
                    || (solutionArray[i] == 1 && currentState)) {
                ret[i] = currentState;
            } else {
                //TODO: implement maxRuntime
                if (solutionArray[i] == 1 && !currentState) {
                    ret[i] = true;
                }
                //check minRuntime
                else {
                    ret[i] = runningFor < this.minRunTime;
                }
            }

            currentState = ret[i];

            if (ret[i]) {
                runningFor += TIME_PER_SLOT;
            } else {
                runningFor = 0;
            }
        }

        return ret;
    }

    private int getNecessaryNumberOfBits() {
        return Math.round((float) (this.getOptimizationHorizon() - this.getReferenceTime()) / TIME_PER_SLOT) * BITS_PER_ACTIVATION;
    }

    @Override
    public String problemToString() {
        return "DachsChpIPP [" + this.getReferenceTime() + "] [" + this.getOptimizationHorizon() + "]";
    }

    @Override
    public DachsChpIPP getClone() {
        return new DachsChpIPP(this);
    }
}