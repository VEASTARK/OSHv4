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
import osh.driver.chp.model.GenericChpModel;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Sebastian Kramer
 */
public class DachsChpIPP
        extends ControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = 7540352071581934211L;
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
    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;
    //TODO add maxRunTime
    // temperature control
    private final double hotWaterStorageMinTemp;
    private final double hotWaterStorageMaxTemp;
    private final double hysteresis;

    private double currentWaterTemperature;

    @SuppressWarnings("unused")
    private int noForcedOffs;
    @SuppressWarnings("unused")
    private int noForcedOns;


    // ### interdependent stuff ###
    /**
     * used for iteration in interdependent calculation
     */
    private long interdependentTime;

    private ArrayList<Activation> interdependentStartingTimes;
    private long interdependentTimeOfFirstBit;
    private double interdependentCervisia;
    private boolean interdependentLastState;

    private SparseLoadProfile lp;
    private boolean[] ab;

    private GenericChpModel masterModel;
    private GenericChpModel actualModel;


    /**
     * CONSTRUCTOR
     */
    public DachsChpIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
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
                logger,
                timeStamp,
                getNecessaryNumberOfBits(relativeHorizon),
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                true, //reacts to input states
                timeStamp.toEpochSecond() + relativeHorizon,
                timeStamp.toEpochSecond(),
                DeviceTypes.CHPPLANT,
                new Commodity[]{Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.NATURALGASPOWER
                },
                new Commodity[]{Commodity.HEATINGHOTWATERPOWER},
                compressionType,
                compressionValue);

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
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected DachsChpIPP() {
        super();
        this.initialState = true;
        this.minRunTime = 0;
        this.hotWaterStorageMinTemp = 0;
        this.hotWaterStorageMaxTemp = 0;
        this.hysteresis = 0;
        this.currentWaterTemperature = 0;
    }


    // ### interdependent problem part stuff ###

    private static int getNecessaryNumberOfBits(int relativeHorizon) {
        return (int) (relativeHorizon / TIME_PER_SLOT) * BITS_PER_ACTIVATION;
    }

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        this.stepSize = stepSize;
        if (createLoadProfile)
            this.lp = new SparseLoadProfile();
        else
            this.lp = null;

        // used for iteration in interdependent calculation
        this.interdependentStartingTimes = null;
        this.setOutputStates(null);
        this.interdependentInputStates = null;

        this.interdependentCervisia = 0.0;

        if (maxReferenceTime != this.getReferenceTime()) {
            this.recalculateEncoding(maxReferenceTime, this.getOptimizationHorizon());
        }
        this.interdependentTimeOfFirstBit = this.getReferenceTime();
        this.interdependentTime = this.getReferenceTime();

        this.ab = this.getActivationBits(this.interdependentTimeOfFirstBit, solution);

        this.noForcedOffs = 0;
        this.noForcedOns = 0;

        this.interdependentLastState = this.initialState;

        this.actualModel = this.masterModel.clone();
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

        int i = (int) ((this.interdependentTime - this.interdependentTimeOfFirstBit) / TIME_PER_SLOT);

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
                    this.noForcedOffs++;

                }
            } else {
                //planned state: off
                if (this.currentWaterTemperature <= this.hotWaterStorageMinTemp) {
                    // hot water too cold -> ON
                    chpOn = true;
                    this.noForcedOns++;
                } else if (this.interdependentLastState && this.currentWaterTemperature <= this.hotWaterStorageMinTemp + this.hysteresis) {
                    //hysteresis keep on
                    chpOn = true;
                    hysteresisOn = true;
                }
            }

            // either forced on or forced off
            if (chpOn != plannedState && !hysteresisOn) {
                //avoid forced on/offs
                this.interdependentCervisia = this.interdependentCervisia + this.forcedOnOffStepMultiplier * this.stepSize + this.forcedOffAdditionalCost;
            }
        }

        //ignore shutOff when minRunTime is not reached (only when not forced off)
        if (!chpOn
                && !plannedState
                && this.interdependentLastState
                && (this.interdependentTime - this.actualModel.getRunningSince()) < this.minRunTime) {
            chpOn = true;
        }

        //switched on or off
        if (chpOn != this.interdependentLastState) {
            this.actualModel.setRunning(chpOn, this.interdependentTime);
        }

        this.actualModel.calcPowerAvg(this.interdependentTime, this.interdependentTime + this.stepSize);

        int activePower = this.actualModel.getAvgActualActivePower();
        int reactivePower = this.actualModel.getAvgActualReactivePower();
        int thermalPower = this.actualModel.getAvgActualThermalPower();
        int gasPower = this.actualModel.getAvgActualGasPower();

        // set power
        if (this.lp != null) {
            this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, activePower);
            this.lp.setLoad(Commodity.REACTIVEPOWER, this.interdependentTime, reactivePower);
            this.lp.setLoad(Commodity.NATURALGASPOWER, this.interdependentTime, gasPower);
            this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, thermalPower);
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
        this.interdependentTime += this.stepSize;
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.lp != null) {
            return new Schedule(
                    this.lp.getCompressedProfile(
                            this.compressionType,
                            this.compressionValue,
                            this.compressionValue),
                    this.interdependentCervisia,
                    this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
        }
    }

    // ### best guess schedule without interdependencies ###

//	@Override
//	public Schedule getSchedule(BitSet solution) {
//		SparseLoadProfile pr = new SparseLoadProfile();
//		double cervisia = 0.0;
//		
//		long timeoffirstbit = getReferenceTime();
//		boolean laststate;
//		boolean activationbits[] = getActivationBits(timeoffirstbit, solution);
//		
//		laststate = initialState;
//
//		for (int i = 0; i < activationbits.length; i++) {
//			boolean chpOn = activationbits[i];
//			long timeStartSlot = timeoffirstbit + i * TIME_PER_SLOT;
//
//			if (chpOn) {
//				// the later the better AND the less the better
//				cervisia += 0.001 * (activationbits.length - i); 
//			}
//
//			if (chpOn == true && laststate == false) {
//				pr.setLoad(Commodity.ACTIVEPOWER, timeStartSlot, typicalActivePower);
//				pr.setLoad(Commodity.NATURALGASPOWER, timeStartSlot, typicalGasPower);
//				laststate = true;
//				// fixed costs per start
//				// costs to turn on the CHP 
//				// (not the variable costs for letting the CHP run) (random value)
//				cervisia += fixedCostPerStart;
//			} 
//			else if (chpOn == false && laststate == true) {
//				pr.setLoad(Commodity.ACTIVEPOWER, timeStartSlot, 0);
//				pr.setLoad(Commodity.NATURALGASPOWER, timeStartSlot, 0);
//				laststate = false;
//			}
//		}
//		
//		if (laststate == true) {
//			pr.setLoad(Commodity.ACTIVEPOWER, this.getOptimizationHorizon(), 0);
//			pr.setLoad(Commodity.NATURALGASPOWER, this.getOptimizationHorizon(), 0);
//		}
//		
//		return new Schedule(pr, cervisia, this.getDeviceType().toString());
//	}

    @Override
    public ISolution transformToFinalInterdependentPhenotype(BitSet solution) {

        boolean[] ab = this.getActivationBits(this.interdependentTimeOfFirstBit, solution);

        this.interdependentStartingTimes = new ArrayList<>();
        long timeOfFirstBit = this.getReferenceTime();
        Activation currentActivation = null;

        for (int i = 0; i < ab.length; i++) {
            if (ab[i]) {
                // turn on
                if (currentActivation == null) {
                    currentActivation = new Activation();
                    currentActivation.startTime = timeOfFirstBit + i * TIME_PER_SLOT;
                    currentActivation.duration = TIME_PER_SLOT;
                } else {
                    currentActivation.duration += TIME_PER_SLOT;
                }
            } else {
                // turn off
                if (currentActivation != null) {
                    this.interdependentStartingTimes.add(currentActivation);
                    currentActivation = null;
                }
            }
        }

        if (currentActivation != null) {
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
    public ActivationList transformToPhenotype(BitSet solution) {
        ArrayList<Activation> startTimes = new ArrayList<>();
        long timeOfFirstBit = this.getReferenceTime();

        boolean[] activationBits = this.getActivationBits(timeOfFirstBit, solution);
        Activation currentActivation = null;

        for (int i = 0; i < activationBits.length; i++) {
            if (activationBits[i]) {
                // turn on
                if (currentActivation == null) {
                    currentActivation = new Activation();
                    currentActivation.startTime = timeOfFirstBit + i * TIME_PER_SLOT;
                    currentActivation.duration = TIME_PER_SLOT;
                } else {
                    currentActivation.duration += TIME_PER_SLOT;
                }
            } else {
                // turn off
                if (currentActivation != null) {
                    startTimes.add(currentActivation);
                    currentActivation = null;
                }
            }
        }

        if (currentActivation != null) {
            startTimes.add(currentActivation);
        }

        ActivationList chpPhenotype = new ActivationList();
        chpPhenotype.setList(startTimes);
        return chpPhenotype;
    }

    // ### helper stuff ###

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        this.setOptimizationHorizon(maxHorizon);
        this.setBitCount(this.getNecessaryNumberOfBits());
    }

    private boolean[] getActivationBits(
            long now,
            BitSet solution) {

        int bitCount = this.getNecessaryNumberOfBits();
        boolean[] ret = new boolean[bitCount / BITS_PER_ACTIVATION];

        boolean lastState = this.initialState;
        long runningFor = 0;

        if (this.initialState) {
            runningFor = this.masterModel.getRunningForAtTimestamp(now);
        }

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
            // end automaton

            // enforce minimum operating time
            if (lastState
                    && !chpOn
                    && runningFor < this.minRunTime) {
                chpOn = true;
            }
            // enforce maximum operating time
            //(TODO max time)

            if (chpOn) {
                runningFor += TIME_PER_SLOT;
            } else {
                runningFor = 0;
            }

            lastState = chpOn;

            ret[i / BITS_PER_ACTIVATION] = chpOn;
        }
        return ret;
    }

    private int getNecessaryNumberOfBits() {
        return Math.round((float) (this.getOptimizationHorizon() - this.getReferenceTime()) / TIME_PER_SLOT) * BITS_PER_ACTIVATION;
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "DachsChpIPP [" + this.getReferenceTime() + "] [" + this.getOptimizationHorizon() + "]";
    }

    @Override
    public String solutionToString(BitSet bits) {
        boolean[] ab = this.getActivationBits(this.getReferenceTime(), bits);
        return "[" + this.getReferenceTime() + "] [" + this.getOptimizationHorizon() + "] " + Arrays.toString(ab);
    }
}