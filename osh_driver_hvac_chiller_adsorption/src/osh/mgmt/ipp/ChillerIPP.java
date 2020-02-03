package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.time.Activation;
import osh.datatypes.time.ActivationList;
import osh.driver.chiller.AdsorptionChillerModel;
import osh.utils.time.TimeConversion;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.BitSet;
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
    private final int typicalStandbyActivePower = 10; // [W]
    private final int typicalRunningActivePower = 420; // [W]
    /**
     * is AdChiller on at the beginning
     */
    private final boolean initialAdChillerState;
    private final Map<Long, Double> temperaturePrediction;
    // temperature control
    private final double coldWaterStorageMinTemp = 10.0;
    private final double coldWaterStorageMaxTemp = 15.0;
    private final double hotWaterStorageMinTemp = 55.0;
    /**
     * delta T below maximum cold water temperature (for forced cooling)
     */
    private final double hysteresis = 1.0;
//	private double hotWaterStorageMaxTemp = 80.0;
    private boolean initialState;
    private ArrayList<Activation> interdependentStartingTimes;

    // ### interdependent stuff ###
    /**
     * used for iteration in interdependent calculation (ancillary time in the future)
     */
    private long interdependentTime;
    /**
     * running times of chiller
     */
    private double interdependentCervisia;
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
    private SparseLoadProfile loadProfile;

    /**
     * CONSTRUCTOR
     */
    public ChillerIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
            boolean toBeScheduled,
            boolean initialAdChillerState,
            Map<Long, Double> temperaturePrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                timeStamp,
                getNecessaryNumberOfBits(RELATIVE_HORIZON),
                toBeScheduled,
                false, //needsAncillaryMeterStates
                true, //reactsToInputStates
                timeStamp.toEpochSecond() + RELATIVE_HORIZON,
                timeStamp.toEpochSecond(),
                DeviceTypes.ADSORPTIONCHILLER,
                new Commodity[]{Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.COLDWATERPOWER},
                compressionType,
                compressionValue);

        this.initialAdChillerState = initialAdChillerState;
        this.temperaturePrediction = temperaturePrediction;
    }

    private static int getNecessaryNumberOfBits(int relativeHorizon) {
        return (int) (RELATIVE_HORIZON / TIME_PER_SLOT) * BITS_PER_ACTIVATION;
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        // used for iteration in interdependent calculation
        this.interdependentStartingTimes = null;
        this.setOutputStates(null);
        this.interdependentInputStates = null;

        if (createLoadProfile) {
            this.loadProfile = new SparseLoadProfile();
        } else {
            this.loadProfile = null;
        }

        this.stepSize = stepSize;

        this.interdependentCervisia = 0.0;

        if (maxReferenceTime != this.getReferenceTime()) {
            this.recalculateEncoding(maxReferenceTime, maxReferenceTime + RELATIVE_HORIZON);
        }
        this.interdependentTime = this.getReferenceTime();

        this.activationBits = this.getActivationBits(this.getReferenceTime(), solution, null);

        this.interdependentLastState = this.initialAdChillerState;

        this.currentActivationRunningTime = 0;
    }

    @Override
    public void calculateNextStep() {

        // update water temperatures
        if (this.interdependentInputStates == null) {
            this.logger.logDebug("No interdependentInputStates available.");
        }

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
            if (this.currentColdWaterTemperature < this.coldWaterStorageMinTemp) {
                minColdWaterTankTemperatureOff = true;
                chillerNewState = false;
            } else if (this.currentColdWaterTemperature >= this.coldWaterStorageMaxTemp - this.hysteresis
                    && this.currentColdWaterTemperature <= this.coldWaterStorageMaxTemp) {
                chillerNewState = true;
                chillerHysteresisOn = true;
            }
            // hot water too cold or hot water too hot -> off
            if (this.currentHotWaterTemperature < this.hotWaterStorageMinTemp) {
                minHotWaterTankTemperatureOff = true;
                chillerNewState = false;
            }
            //TODO add hot water maximum temperature control
        } else {
            if (this.currentColdWaterTemperature > this.coldWaterStorageMaxTemp
                    && this.currentHotWaterTemperature > this.hotWaterStorageMinTemp) {
                chillerHysteresisOn = true;
                chillerNewState = true;
            }
        }

        int i = (int) ((this.interdependentTime - this.getReferenceTime()) / TIME_PER_SLOT);

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
            this.interdependentCervisia += 0.0001 * (this.activationBits.length - i);
        }


        // calculate power values
        double activePower = this.typicalStandbyActivePower;
        double hotWaterPower = 0;
        double coldWaterPower = 0;

        if ((chillerNewState && !this.interdependentLastState)
                || (chillerNewState && this.currentActivationRunningTime % 60 == 0)
                || (!chillerNewState && this.interdependentLastState)
                || (this.interdependentTime == this.getReferenceTime())) {

            if (chillerNewState) {
                if (this.temperaturePrediction.get((this.interdependentTime / 300) * 300) == null) {
                    @SuppressWarnings("unused")
                    long time = (this.interdependentTime / 300) * 300;
                    @SuppressWarnings("unused")
                    int debug = 0;
                }
                long secondsFromYearStart =
                        TimeConversion.getSecondsSinceYearStart(TimeConversion.convertUnixTimeToZonedDateTime(this.interdependentTime));
                double outdoorTemperature = this.temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
                activePower = this.typicalRunningActivePower;
                coldWaterPower = AdsorptionChillerModel.chilledWaterPower(this.currentHotWaterTemperature, outdoorTemperature);
                hotWaterPower = (-1) * coldWaterPower / AdsorptionChillerModel.cop(this.currentHotWaterTemperature, outdoorTemperature);
            }

            if (this.loadProfile != null) {
                this.loadProfile.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, (int) activePower);
                this.loadProfile.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, (int) hotWaterPower);
                this.loadProfile.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, (int) coldWaterPower);
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
            this.interdependentCervisia += 10.0;
        }

        this.interdependentLastState = chillerNewState;
        this.interdependentTime += this.stepSize;
        if (chillerNewState) {
            this.currentActivationRunningTime += this.stepSize;
        } else {
            this.currentActivationRunningTime = 0;
        }
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.loadProfile == null) {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
        } else {
            if (this.loadProfile.getEndingTimeOfProfile() > 0) {
                this.loadProfile.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, this.typicalStandbyActivePower);
                this.loadProfile.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
                this.loadProfile.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, 0);
            }

            SparseLoadProfile slp = this.loadProfile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
            return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
        }
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype(BitSet solution) {

        boolean[] ab = this.getActivationBits(this.getReferenceTime(), solution, null);

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
    public ActivationList transformToPhenotype(BitSet solution) {
        return null;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.setOptimizationHorizon(maxHorizon);
        this.setBitCount(this.getNecessaryNumberOfBits());
    }

    // HELPER STUFF

    private boolean[] getActivationBits(
            long now,
            BitSet solution,
            AdditionalInfo ai) {

        if (ai != null) {
            ai.noForcedOffs = 0;
            ai.noForcedOns = 0;
        }

        int bitCount = this.getNecessaryNumberOfBits();
        boolean[] ret = new boolean[bitCount / BITS_PER_ACTIVATION];

        boolean lastState = this.initialState;

        for (int i = 0; i < bitCount; i += BITS_PER_ACTIVATION) {
            boolean chpOn;

            // automaton
            boolean anded = true, ored = false; // and / or
            for (int j = 0; j < BITS_PER_ACTIVATION; j++) {
                anded &= solution.get(i + j);
                ored |= solution.get(i + j);
            }
            if (!anded && ored) { // bits are not all equal
                chpOn = lastState; // keep last state
            } else {
                chpOn = solution.get(i); // all 1 -> on, all 0 -> off
            }

            lastState = chpOn;

            ret[i / BITS_PER_ACTIVATION] = chpOn;
        }

        return ret;
    }

    private int getNecessaryNumberOfBits() {
        return (int) (Math.ceil((double) (this.getOptimizationHorizon() - this.getReferenceTime()) / TIME_PER_SLOT) * BITS_PER_ACTIVATION);
    }

    @Override
    public String problemToString() {
        AdditionalInfo ai = new AdditionalInfo();
        this.getActivationBits(this.getReferenceTime(), new BitSet(), ai);
        return "Chiller IPP , forced ons:" + ai.noForcedOns;
    }

    // ### to string ###

    @Override
    public String solutionToString(BitSet bits) {
        return "Chiller IPP solution";
    }

    private static class AdditionalInfo {
        public int noForcedOffs;
        public int noForcedOns;
    }
}