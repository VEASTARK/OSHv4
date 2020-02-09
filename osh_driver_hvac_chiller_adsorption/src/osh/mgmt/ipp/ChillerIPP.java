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
import osh.driver.chiller.AdsorptionChillerModel;
import osh.utils.time.TimeConversion;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

/**
 * @author Julian Feder, Sebastian Kramer, Ingo Mauser
 */
public class ChillerIPP extends ControllableIPP<ISolution, IPrediction> {

    /**
     * prediction horizon in seconds
     */
//	public final static int RELATIVE_HORIZON = 3 * 3600; // 3 hours
//	public final static int RELATIVE_HORIZON = 6 * 3600; // 6 hours
//	public final static int RELATIVE_HORIZON = 12 * 3600; // 12 hours
//	public final static int RELATIVE_HORIZON = 18 * 3600; // 18 hours
    public final static int RELATIVE_HORIZON = 24 * 3600; // 24 hours
    /**
     * slot length in [s]
     */
    public final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
    private static final long serialVersionUID = -515441464083361208L;
    private final static int BITS_PER_ACTIVATION = 4;

    //TODO move to config
    private static final int typicalStandbyActivePower = 10; // [W]
    private static final int typicalRunningActivePower = 420; // [W]
    /**
     * is AdChiller on at the beginning
     */
    private final boolean initialAdChillerState;
    private final Map<Long, Double> temperaturePrediction;
    // temperature control
    private static final double coldWaterStorageMinTemp = 10.0;
    private static final double coldWaterStorageMaxTemp = 15.0;
    private static final double hotWaterStorageMinTemp = 55.0;
    /**
     * delta T below maximum cold water temperature (for forced cooling)
     */
    private static final double hysteresis = 1.0;
//	private double hotWaterStorageMaxTemp = 80.0;
    private boolean initialState;
    private ArrayList<Activation> interdependentStartingTimes;

    // ### interdependent stuff ###
    /**
     * running times of chiller
     */
    private boolean interdependentLastState;
    /**
     * from cold water tank IPP
     */
    private double currentColdWaterTemperature = 12;
    /**
     * from hot water tank IPP
     */
    private double currentHotWaterTemperature = 60;
    private boolean[] activationBits;
    private int currentActivationRunningTime;

    /**
     * CONSTRUCTOR
     */
    public ChillerIPP(
            UUID deviceId,
            ZonedDateTime timeStamp,
            boolean toBeScheduled,
            boolean initialAdChillerState,
            Map<Long, Double> temperaturePrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                timeStamp,
                toBeScheduled,
                false, //needsAncillaryMeterStates
                true, //reactsToInputStates
                timeStamp.toEpochSecond() + RELATIVE_HORIZON,
                DeviceTypes.ADSORPTIONCHILLER,
                EnumSet.of(Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.COLDWATERPOWER),
                EnumSet.of(Commodity.HEATINGHOTWATERPOWER, Commodity.COLDWATERPOWER),
                compressionType,
                compressionValue,
                new BinaryBiStateVariableTranslator(BITS_PER_ACTIVATION),
                new RealSimulatedBiStateTranslator(BITS_PER_ACTIVATION));

        this.initialAdChillerState = initialAdChillerState;
        this.temperaturePrediction = temperaturePrediction;

        this.updateSolutionInformation(this.getReferenceTime(), this.getOptimizationHorizon());
    }

    public ChillerIPP(ChillerIPP other) {
        super(other);

        this.initialAdChillerState = other.initialAdChillerState;

        this.temperaturePrediction = null;

        this.initialState = other.initialState;
        this.interdependentStartingTimes = null;

        this.interdependentLastState = other.interdependentLastState;
        this.currentColdWaterTemperature = other.currentColdWaterTemperature;
        this.currentHotWaterTemperature = other.currentHotWaterTemperature;

        this.activationBits = null;
        this.currentActivationRunningTime = other.currentActivationRunningTime;
    }

    private static int getNecessaryNumberOfBits(int relativeHorizon) {
        return (int) (RELATIVE_HORIZON / TIME_PER_SLOT) * BITS_PER_ACTIVATION;
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        // used for iteration in interdependent calculation
        this.interdependentStartingTimes = null;

        this.interdependentLastState = this.initialAdChillerState;
        this.currentActivationRunningTime = 0;
    }

    private void updateSolutionInformation(long referenceTime, long maxHorizon) {

        int slots = (int) Math.ceil(((double) (maxHorizon - referenceTime)) / ((float) TIME_PER_SLOT));
        double[][] boundaries = new double[slots][];

        for (int i = 0; i < slots; i++) {
            boundaries[i] = new double[]{0, 2};
        }

        this.solutionHandler.updateVariableInformation(VariableType.LONG, slots, boundaries);
    }

    @Override
    protected void interpretNewSolution() {
        this.activationBits = this.getActivationBits(this.currentSolution);
    }

    @Override
    public void calculateNextStep() {

        // update water temperatures
        this.currentHotWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
        this.currentColdWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.COLDWATERPOWER);

        // ### interdependent logic (hysteresis control) ###
        // cold water control (min, max temperatures)
        boolean chillerNewState = this.interdependentLastState;
        boolean chillerHysteresisOn = false;
        boolean minColdWaterTankTemperatureOff = false;
        boolean minHotWaterTankTemperatureOff = false;

        // AdChiller control (forced on/off)
        if (this.interdependentLastState) {
            // cold water too cold -> off
            if (this.currentColdWaterTemperature < coldWaterStorageMinTemp) {
                minColdWaterTankTemperatureOff = true;
                chillerNewState = false;
            } else if (this.currentColdWaterTemperature >= coldWaterStorageMaxTemp - hysteresis
                    && this.currentColdWaterTemperature <= coldWaterStorageMaxTemp) {
                chillerNewState = true;
                chillerHysteresisOn = true;
            }
            // hot water too cold or hot water too hot -> off
            if (this.currentHotWaterTemperature < hotWaterStorageMinTemp) {
                minHotWaterTankTemperatureOff = true;
                chillerNewState = false;
            }
            //TODO add hot water maximum temperature control
        } else {
            if (this.currentColdWaterTemperature > coldWaterStorageMaxTemp
                    && this.currentHotWaterTemperature > hotWaterStorageMinTemp) {
                chillerHysteresisOn = true;
                chillerNewState = true;
            }
        }

        int i = (int) ((this.getInterdependentTime() - this.getReferenceTime()) / TIME_PER_SLOT);

        if (!chillerHysteresisOn
                && !minColdWaterTankTemperatureOff
                && !minHotWaterTankTemperatureOff
                && i < this.activationBits.length) {
            chillerNewState = this.activationBits[i];
        } else {
            //NOTHING (KEEP STATE)
        }

        // ### set power profiles and interdependentCervisia

        if (chillerNewState) {
            // the later the better AND the less the better
            this.addInterdependentCervisia(0.0001 * (this.activationBits.length - i));
        }


        // calculate power values
        double activePower = typicalStandbyActivePower;
        double hotWaterPower = 0;
        double coldWaterPower = 0;

        if ((chillerNewState && !this.interdependentLastState)
                || (chillerNewState && this.currentActivationRunningTime % 60 == 0)
                || (!chillerNewState && this.interdependentLastState)
                || (this.getInterdependentTime() == this.getReferenceTime())) {

            if (chillerNewState) {
                if (this.temperaturePrediction.get((this.getInterdependentTime() / 300) * 300) == null) {
                    @SuppressWarnings("unused")
                    long time = (this.getInterdependentTime() / 300) * 300;
                    @SuppressWarnings("unused")
                    int debug = 0;
                }
                long secondsFromYearStart =
                        TimeConversion.getSecondsSinceYearStart(TimeConversion.convertUnixTimeToZonedDateTime(this.getInterdependentTime()));
                double outdoorTemperature = this.temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
                activePower = typicalRunningActivePower;
                coldWaterPower = AdsorptionChillerModel.chilledWaterPower(this.currentHotWaterTemperature, outdoorTemperature);
                hotWaterPower = (-1) * coldWaterPower / AdsorptionChillerModel.cop(this.currentHotWaterTemperature, outdoorTemperature);
            }

            if (this.getLoadProfile() != null) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), (int) activePower);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), (int) hotWaterPower);
                this.getLoadProfile().setLoad(Commodity.COLDWATERPOWER, this.getInterdependentTime(), (int) coldWaterPower);
            }

            this.internalInterdependentOutputStates.setPower(Commodity.ACTIVEPOWER, activePower);
            this.internalInterdependentOutputStates.setPower(Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
            this.internalInterdependentOutputStates.setPower(Commodity.COLDWATERPOWER, coldWaterPower);


            this.setOutputStates(this.internalInterdependentOutputStates);
        } else {
            this.setOutputStates(null);
        }

        if (chillerNewState && !this.interdependentLastState) {
            // fixed costs per start, i.e., costs to turn on the CHP
            // (not the variable costs for letting the CHP run)
            this.addInterdependentCervisia(10.0);
        }

        this.interdependentLastState = chillerNewState;
        this.incrementInterdependentTime();
        if (chillerNewState) {
            this.currentActivationRunningTime += this.getStepSize();
        } else {
            this.currentActivationRunningTime = 0;
        }
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.getLoadProfile() == null) {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            if (this.getLoadProfile().getEndingTimeOfProfile() > 0) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), typicalStandbyActivePower);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.COLDWATERPOWER, this.getInterdependentTime(), 0);

                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getLoadProfile().getEndingTimeOfProfile(), typicalStandbyActivePower);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getLoadProfile().getEndingTimeOfProfile(), 0);
                this.getLoadProfile().setLoad(Commodity.COLDWATERPOWER, this.getLoadProfile().getEndingTimeOfProfile(), 0);
            }

            SparseLoadProfile slp = this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue);
            return new Schedule(slp, this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype() {

        boolean[] ab = this.getActivationBits(this.currentSolution);

        this.interdependentStartingTimes = new ArrayList<>();
        long timeOfFirstBit = this.getReferenceTime();
        Activation currentActivation = null;

        long duration = 0;
        for (int i = 0; i < ab.length; i++) {
            if (ab[i]) {
                // turn on
                if (currentActivation == null) {
                    currentActivation = new Activation();
                    currentActivation.startTime = TimeConversion.convertUnixTimeToZonedDateTime(timeOfFirstBit + i * TIME_PER_SLOT);
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
            ActivationList adChillerPhenotype = new ActivationList();
            adChillerPhenotype.setList(this.interdependentStartingTimes);
            return adChillerPhenotype;
        } else {
            return null;
        }
    }


    // ### OLD STUFF (best guess schedule, not interdependent)

    @Override
    public ActivationList transformToPhenotype(DecodedSolutionWrapper solution) {
        return null;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        if (currentTime != this.getReferenceTime() || maxHorizon != this.getOptimizationHorizon()) {
            this.setReferenceTime(currentTime);
            this.setOptimizationHorizon(maxHorizon);

            this.updateSolutionInformation(currentTime, this.getOptimizationHorizon());
        }
    }

    // HELPER STUFF

    private boolean[] getActivationBits(
            DecodedSolutionWrapper solution) {

        long[] solutionArray = solution.getLongArray();
        boolean[] ret = new boolean[solutionArray.length];

        boolean currentState = this.initialAdChillerState;

        for (int i = 0; i < solutionArray.length; i++) {

            if (solutionArray[i] == 2
                    || (solutionArray[i] == 0 && !currentState)
                    || (solutionArray[i] == 1 && currentState)) {
                ret[i] = currentState;
            } else {
                //TODO: implement maxRuntime
                //TODO minRuntime

                ret[i] = solutionArray[i] == 1 && !currentState;
            }

            currentState = ret[i];
        }

        return ret;
    }

    @Override
    public long getOptimizationHorizon() {
        return this.getReferenceTime() + RELATIVE_HORIZON;
    }

    @Override
    public String problemToString() {
        return "Chiller IPP";
    }

    @Override
    public ChillerIPP getClone() {
        return new ChillerIPP(this);
    }

    // ### to string ###

    @Override
    public String solutionToString() {
        return "Chiller IPP solution";
    }
}