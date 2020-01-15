package osh.mgmt.globalcontroller.jmetal.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import jmetal.util.PseudoRandom;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.esc.IOCEnergySubject;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.OCEnergySimulationCore;
import osh.esc.UUIDCommodityMap;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.utils.DeepCopy;

import java.util.*;

/**
 * Problem to be solved by solver / optimizer
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class EnergyManagementProblem extends Problem {

    private static final long serialVersionUID = 1L;
    private final int STEP_SIZE;
    final int[][] bitPositions;
    final Set<UUID> passiveUUIDs = new HashSet<>();
    final Set<UUID> activeNeedInputUUIDs = new HashSet<>();
    final Object2IntOpenHashMap<UUID> uuidIntMap;
    private final IFitness fitnessFunction;
    // active nodes, need information about commodity input states (new IPP)
    private final List<InterdependentProblemPart<?, ?>> activeNeedsInput;
    // active nodes
    private final List<InterdependentProblemPart<?, ?>> activeWorksAlone;
    // passive nodes
    private final List<InterdependentProblemPart<?, ?>> passive;
    // static PP
    private final List<InterdependentProblemPart<?, ?>> staticParts;
    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    private final long ignoreLoadProfileBefore;
    private final long ignoreLoadProfileAfter;
    private boolean multiThreading;
    private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedActiveNeedsInput;
    private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedActiveWorksAlone;
    private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedPassive;
    private ObjectArrayList<List<InterdependentProblemPart<?, ?>>> multiThreadedStatic;
    private ObjectArrayList<OCEnergySimulationCore> multiOCs;
    private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesActiveNI;
    private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesActiveWA;
    private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesPassive;
    private List<InterdependentProblemPart<?, ?>> multiThreadedMasterCopiesStatic;
    private Long maxReferenceTime;
    private Long maxOptimizationHorizon;
    private boolean keepPrediction;
    private final OCEnergySimulationCore ocEnergySimulationCore;


    /**
     * CONSTRUCTOR
     *
     */
    public EnergyManagementProblem(
            List<InterdependentProblemPart<?, ?>> problemParts,
            OCEnergySimulationCore ocESC,
            int[][] bitPositions,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            long ignoreLoadProfileAfter,
            OSHRandomGenerator randomGenerator,
            IGlobalLogger globalLogger,
            IFitness fitnessFunction,
            int STEP_SIZE) {
        super(new PseudoRandom(randomGenerator));

        this.bitPositions = bitPositions;

        this.activeNeedsInput = new ArrayList<>();
        this.activeWorksAlone = new ArrayList<>();
        this.passive = new ArrayList<>();
        this.staticParts = new ArrayList<>();

        this.priceSignals = priceSignals;
        this.powerLimitSignals = powerLimitSignals;

        this.ignoreLoadProfileBefore = ignoreLoadProfileBefore;
        this.ignoreLoadProfileAfter = ignoreLoadProfileAfter;

        this.STEP_SIZE = STEP_SIZE;

        if (ignoreLoadProfileBefore == ignoreLoadProfileAfter) {
            //TODO remove load profile from problem
        }

        // ENERGY SIMULATION

        // create EnergySimulationCore
        this.ocEnergySimulationCore = ocESC;


        this.fitnessFunction = fitnessFunction;
        int numberOfBits = 0;

        Set<UUID> allUUIDs = new HashSet<>();
        Set<UUID> activeUUIDs = new HashSet<>();
        Set<UUID> passiveUUIDs = new HashSet<>();

        this.uuidIntMap = new Object2IntOpenHashMap<>(problemParts.size());
        this.uuidIntMap.defaultReturnValue(-1);

        Object2ObjectOpenHashMap<UUID, Commodity[]> uuidOutputMap = new Object2ObjectOpenHashMap<>(problemParts.size());
        Object2ObjectOpenHashMap<UUID, Commodity[]> uuidInputMap = new Object2ObjectOpenHashMap<>(problemParts.size());

        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (this.uuidIntMap.put(part.getDeviceID(), part.getId()) != -1) {
                throw new IllegalArgumentException("multiple IPPs with same UUID");
            }
            if (!part.isCompletelyStatic()) {
                allUUIDs.add(part.getDeviceID());
                numberOfBits += part.getBitCount();
                uuidOutputMap.put(part.getDeviceID(), part.getAllOutputCommodities());
                uuidInputMap.put(part.getDeviceID(), part.getAllInputCommodities());
            }
        }

        this.ocEnergySimulationCore.splitActivePassive(allUUIDs, activeUUIDs, passiveUUIDs);

        //split parts in 4 lists
        // calc maxReferenceTime of parts
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (activeUUIDs.contains(part.getDeviceID())) {
                if (part.isReactsToInputStates()) {
                    this.activeNeedsInput.add(part);
                } else {
                    this.activeWorksAlone.add(part);
                }
            } else if (passiveUUIDs.contains(part.getDeviceID())) {
                this.passive.add(part);
            } else {
                if (part.isCompletelyStatic())
                    this.staticParts.add(part);
                else
                    throw new IllegalArgumentException("part is neither active nor passive");
            }

            if (this.maxReferenceTime == null) {
                this.maxReferenceTime = part.getReferenceTime();
            } else {
                this.maxReferenceTime = Math.max(this.maxReferenceTime, part.getReferenceTime());
            }
        }

        Set<UUID> allActive = new HashSet<>();

        for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
            allActive.add(part.getDeviceID());
            this.activeNeedInputUUIDs.add(part.getDeviceID());
        }
        for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
            allActive.add(part.getDeviceID());
        }
        for (InterdependentProblemPart<?, ?> part : this.passive) {
            this.passiveUUIDs.add(part.getDeviceID());
        }

        this.ocEnergySimulationCore.initializeGrids(allActive, this.activeNeedInputUUIDs, passiveUUIDs,
                this.uuidIntMap, uuidOutputMap, uuidInputMap);

        this.maxOptimizationHorizon = this.maxReferenceTime;

        // calc maxOptimizationHorizon
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (part instanceof ControllableIPP<?, ?>) {
                this.maxOptimizationHorizon = Math.max(part.getOptimizationHorizon(), this.maxOptimizationHorizon);
            }
        }

        this.numberOfVariables_ = 1;
        this.numberOfObjectives_ = 1;
        this.numberOfConstraints_ = 0;
        this.problemName_ = "osh";

        this.solutionType_ = new BinarySolutionType(this);

        this.length_ = new int[this.numberOfVariables_];
        this.length_[0] = numberOfBits;

    }

    public void initMultithreading() {
        this.multiThreading = true;
        this.multiThreadedActiveNeedsInput = new ObjectArrayList<>();
        this.multiThreadedActiveWorksAlone = new ObjectArrayList<>();
        this.multiThreadedPassive = new ObjectArrayList<>();
        this.multiThreadedStatic = new ObjectArrayList<>();
        this.multiOCs = new ObjectArrayList<>();

        this.multiThreadedMasterCopiesActiveNI = new ObjectArrayList<>();
        this.multiThreadedMasterCopiesActiveWA = new ObjectArrayList<>();
        this.multiThreadedMasterCopiesPassive = new ObjectArrayList<>();
        this.multiThreadedMasterCopiesStatic = new ObjectArrayList<>();

        //set logger to null so that deep copy does not try to copy it
        IGlobalLogger temp = null;
        for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
            temp = part.logger;
            part.logger = null;
            part.prepareForDeepCopy();
        }
        for (InterdependentProblemPart<?, ?> part : this.staticParts) {
            temp = part.logger;
            part.logger = null;
            part.prepareForDeepCopy();
        }
        for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
            temp = part.logger;
            part.logger = null;
            part.prepareForDeepCopy();
            if (part instanceof NonControllableIPP<?, ?>) {
                //initialize completely static IPP so we can save that time on every copy
                part.initializeInterdependentCalculation(this.maxReferenceTime, new BitSet(), this.STEP_SIZE, false, false);
            }
        }
        for (InterdependentProblemPart<?, ?> part : this.passive) {
            temp = part.logger;
            part.logger = null;
            part.prepareForDeepCopy();
            if (part instanceof NonControllableIPP<?, ?>) {
                //initialize completely static IPP so we can save that time on every copy
                part.initializeInterdependentCalculation(this.maxReferenceTime, new BitSet(), this.STEP_SIZE, false, false);
            }
        }

        //create one master copy per ProblemPart
        for (int j = 0; j < this.activeNeedsInput.size(); j++) {
            InterdependentProblemPart<?, ?> part = this.activeNeedsInput.get(j);
            this.multiThreadedMasterCopiesActiveNI.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
        }
        for (int j = 0; j < this.activeWorksAlone.size(); j++) {
            InterdependentProblemPart<?, ?> part = this.activeWorksAlone.get(j);
            this.multiThreadedMasterCopiesActiveWA.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
        }
        for (int j = 0; j < this.passive.size(); j++) {
            InterdependentProblemPart<?, ?> part = this.passive.get(j);
            this.multiThreadedMasterCopiesPassive.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
        }
        for (int j = 0; j < this.staticParts.size(); j++) {
            InterdependentProblemPart<?, ?> part = this.staticParts.get(j);
            this.multiThreadedMasterCopiesStatic.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
        }

        //restore logger
        for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
            part.logger = temp;
        }
        for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
            part.logger = temp;
        }
        for (InterdependentProblemPart<?, ?> part : this.passive) {
            part.logger = temp;
        }
        for (InterdependentProblemPart<?, ?> part : this.staticParts) {
            part.logger = temp;
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized List<InterdependentProblemPart<?, ?>>[] requestIPPCopies() {
        List<InterdependentProblemPart<?, ?>>[] ret = (ObjectArrayList<InterdependentProblemPart<?, ?>>[]) new ObjectArrayList<?>[4];

        if (!this.multiThreadedActiveNeedsInput.isEmpty()) {
            ret[0] = this.multiThreadedActiveNeedsInput.remove(0);
            ret[1] = this.multiThreadedActiveWorksAlone.remove(0);
            ret[2] = this.multiThreadedPassive.remove(0);
            ret[3] = this.multiThreadedStatic.remove(0);
        } else {
            ObjectArrayList<InterdependentProblemPart<?, ?>> niList = new ObjectArrayList<>();
            ObjectArrayList<InterdependentProblemPart<?, ?>> waList = new ObjectArrayList<>();
            ObjectArrayList<InterdependentProblemPart<?, ?>> paList = new ObjectArrayList<>();
            ObjectArrayList<InterdependentProblemPart<?, ?>> stList = new ObjectArrayList<>();

            for (int j = 0; j < this.multiThreadedMasterCopiesActiveNI.size(); j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesActiveNI.get(j);
                niList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesActiveWA.size(); j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesActiveWA.get(j);
                waList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesPassive.size(); j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesPassive.get(j);
                paList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesStatic.size(); j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesStatic.get(j);
                stList.add(j, (InterdependentProblemPart<?, ?>) DeepCopy.copy(part));
            }

            ret[0] = niList;
            ret[1] = waList;
            ret[2] = paList;
            ret[3] = stList;
        }

        return ret;
    }

    private synchronized OCEnergySimulationCore requestOCESC() {
        if (!this.multiOCs.isEmpty()) {
            return this.multiOCs.remove(0);
        } else {
            return (OCEnergySimulationCore) DeepCopy.copy(this.ocEnergySimulationCore);
        }
    }

    private synchronized void freeIPPCopies(List<InterdependentProblemPart<?, ?>> needsI, List<InterdependentProblemPart<?, ?>> worksA,
                                            List<InterdependentProblemPart<?, ?>> passive, List<InterdependentProblemPart<?, ?>> staticParts) {
        this.multiThreadedActiveNeedsInput.add(needsI);
        this.multiThreadedActiveWorksAlone.add(worksA);
        this.multiThreadedPassive.add(passive);
        this.multiThreadedStatic.add(staticParts);
    }

    private synchronized void freeOCESC(OCEnergySimulationCore ocesc) {
        this.multiOCs.add(ocesc);
    }

    public void finalizeGrids() {
        this.ocEnergySimulationCore.finalizeGrids();
    }

    public void evaluateFinalTime(Solution solution, boolean log) {
        this.multiThreading = false;
        this.keepPrediction = true;
        this.evaluate(solution, log);
        this.keepPrediction = false;
        this.finalizeGrids();
    }

    @Override
    public void evaluate(Solution solution) {
        this.evaluate(solution, false);
    }


    private void evaluate(Solution solution, boolean log) {

        List<InterdependentProblemPart<?, ?>> activeNeedsInput;
        List<InterdependentProblemPart<?, ?>> activeWorksAlone;
        List<InterdependentProblemPart<?, ?>> passive;
        List<InterdependentProblemPart<?, ?>> staticParts;
        List<InterdependentProblemPart<?, ?>> allIPPs;
        OCEnergySimulationCore ocEnergySimulationCore;

        if (this.multiThreading) {
            List<InterdependentProblemPart<?, ?>>[] list = this.requestIPPCopies();
            activeNeedsInput = list[0];
            activeWorksAlone = list[1];
            passive = list[2];
            staticParts = list[3];
            ocEnergySimulationCore = this.requestOCESC();
        } else {
            activeNeedsInput = this.activeNeedsInput;
            activeWorksAlone = this.activeWorksAlone;
            passive = this.passive;
            staticParts = this.staticParts;
            ocEnergySimulationCore = this.ocEnergySimulationCore;
        }
        allIPPs = new ObjectArrayList<>(activeNeedsInput.size() + activeWorksAlone.size() + passive.size() + staticParts.size());
        allIPPs.addAll(activeNeedsInput);
        allIPPs.addAll(activeWorksAlone);
        allIPPs.addAll(passive);
        allIPPs.addAll(staticParts);


        try {
            double fitness;

            Binary variable = (Binary) solution.getDecisionVariables()[0];

            // calculate interdependent parts

            // initialize
            for (InterdependentProblemPart<?, ?> part : allIPPs) {
                int bitPos = 0;
                try {
                    bitPos = this.bitPositions[part.getId()][0];
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                int bitposEnd = this.bitPositions[part.getId()][1];
                part.initializeInterdependentCalculation(
                        this.maxReferenceTime,
                        variable.bits_.get(bitPos, bitposEnd),
                        this.STEP_SIZE,
                        false,
                        this.keepPrediction
                );

                if ((part.getBitCount() == 0 && (bitposEnd - bitPos) != 0)
                        || (part.getBitCount() > 0 && (bitposEnd - bitPos) != part.getBitCount())) {
                    throw new IllegalArgumentException("bit-count mismatch");
                }
            }

            // go through step by step
            AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();
            ancillaryMeter.initSequential();

            ObjectArrayList<InterdependentProblemPart<?, ?>> allActive = new ObjectArrayList<>(activeNeedsInput.size() + activeWorksAlone.size());
            allActive.addAll(activeNeedsInput);
            allActive.addAll(activeWorksAlone);

            InterdependentProblemPart<?, ?>[] allActiveArray = new InterdependentProblemPart<?, ?>[allActive.size()];
            allActiveArray = allActive.toArray(allActiveArray);

            InterdependentProblemPart<?, ?>[] allActiveNIArray = new InterdependentProblemPart<?, ?>[activeNeedsInput.size()];
            allActiveNIArray = activeNeedsInput.toArray(allActiveNIArray);

            InterdependentProblemPart<?, ?>[] passiveArray = new InterdependentProblemPart<?, ?>[passive.size()];
            passiveArray = passive.toArray(passiveArray);

            // go through in steps of STEP_SIZE (in ticks)
            //init the maps for the commodity states
            UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActiveArray, this.uuidIntMap, true);

            UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passiveArray, this.uuidIntMap, true);

            long t;

            //let all passive states calculate their first state
            for (InterdependentProblemPart<?, ?> part : passiveArray) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            //dummy AncillaryMeterState, we don't know the state of the ancillaryMeter at t=t_start, thus set all to zero
            AncillaryMeterState meterState = new AncillaryMeterState();

            //send the first passive state to active nodes
            ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, this.activeNeedInputUUIDs, passiveToActiveMap);

            // iterate
            for (t = this.maxReferenceTime; t < this.maxOptimizationHorizon + this.STEP_SIZE; t += this.STEP_SIZE) {

                //let all active states calculate their next step
                for (InterdependentProblemPart<?, ?> part : allActiveArray) {
                    part.calculateNextStep();
                    activeToPassiveMap.put(part.getId(), part.getCommodityOutputStates());
                }

                //send active state to passive nodes, save meter state
                ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passiveArray, this.passiveUUIDs, meterState);

                //send loads to the ancillary meter profile
                ancillaryMeter.setLoadSequential(meterState, t);

                //let all passive states calculate their next step
                for (InterdependentProblemPart<?, ?> part : passiveArray) {
                    part.calculateNextStep();
                    passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
                }

                //send new passive states to active nodes
                ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, this.activeNeedInputUUIDs, passiveToActiveMap);

            }

            ancillaryMeter.endSequential();
            ancillaryMeter.setEndingTimeOfProfile(this.maxOptimizationHorizon);


            // calculate variable fitness depending on price signals...
            fitness = this.fitnessFunction.getFitnessValue(
                    this.ignoreLoadProfileBefore,
                    this.ignoreLoadProfileAfter,
                    ancillaryMeter,
                    this.priceSignals,
                    this.powerLimitSignals
            );

            // add lukewarm cervisia (i.e. additional fixed costs...)
            for (InterdependentProblemPart<?, ?> problempart : allIPPs) {
                double add = problempart.getFinalInterdependentSchedule().getLukewarmCervisia();
                fitness += add;

                if (log && !((Binary) solution.getDecisionVariables()[0]).bits_.get(0) && add != 0) {
                    OSH_gGAMultiThread.logCervisia(problempart.getDeviceType(), add);
                }
            }

            solution.setObjective(0, fitness); //small value is good value
            solution.setFitness(fitness);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (this.multiThreading) {
            this.freeIPPCopies(activeNeedsInput, activeWorksAlone, passive, staticParts);
            this.freeOCESC(ocEnergySimulationCore);
        }
    }

    public void evaluateWithDebuggingInformation(BitSet bits,
                                                 AncillaryCommodityLoadProfile ancillaryMeter,
                                                 TreeMap<Long, Double> predictedTankTemp,
                                                 TreeMap<Long, Double> predictedHotWaterDemand,
                                                 TreeMap<Long, Double> predictedHotWaterSupply,
                                                 List<Schedule> schedules,
                                                 boolean keepPrediction,
                                                 UUID hotWaterTankID) {

        ancillaryMeter.initSequential();

        List<InterdependentProblemPart<?, ?>> activeNeedsInput = this.activeNeedsInput;
        List<InterdependentProblemPart<?, ?>> activeWorksAlone = this.activeWorksAlone;
        List<InterdependentProblemPart<?, ?>> passive = this.passive;
        List<InterdependentProblemPart<?, ?>> staticParts = this.staticParts;

        List<InterdependentProblemPart<?, ?>> allIPPs = new ArrayList<>(activeNeedsInput);
        allIPPs.addAll(activeWorksAlone);
        allIPPs.addAll(passive);
        allIPPs.addAll(staticParts);

        try {

            // calculate interdependent parts

            // initialize
            for (InterdependentProblemPart<?, ?> part : allIPPs) {
                int bitPos = 0;
                try {
                    bitPos = this.bitPositions[part.getId()][0];
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
                int bitPosEnd = this.bitPositions[part.getId()][1];
                part.initializeInterdependentCalculation(
                        this.maxReferenceTime,
                        bits.get(bitPos, bitPosEnd),
                        this.STEP_SIZE,
                        true,
                        keepPrediction
                );
                if ((part.getBitCount() == 0 && (bitPosEnd - bitPos) != 0)
                        || (part.getBitCount() != 0 && (bitPosEnd - bitPos) != part.getBitCount())) {
                    throw new IllegalArgumentException("bit-count mismatch");
                }
            }

            List<InterdependentProblemPart<?, ?>> allActive = new LinkedList<>(activeNeedsInput);
            allActive.addAll(activeWorksAlone);

            InterdependentProblemPart<?, ?>[] allActiveArray = new InterdependentProblemPart<?, ?>[allActive.size()];
            allActiveArray = allActive.toArray(allActiveArray);

            InterdependentProblemPart<?, ?>[] allActiveNIArray = new InterdependentProblemPart<?, ?>[activeNeedsInput.size()];
            allActiveNIArray = activeNeedsInput.toArray(allActiveNIArray);

            InterdependentProblemPart<?, ?>[] passiveArray = new InterdependentProblemPart<?, ?>[passive.size()];
            passiveArray = passive.toArray(passiveArray);

            // go through in steps of STEP_SIZE (in ticks)
            long t;

            //init the maps for the commodity states
            UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActive, this.uuidIntMap);
            UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passive, this.uuidIntMap);

            //let all passive states calculate their first state
            for (InterdependentProblemPart<?, ?> part : passive) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            for (IOCEnergySubject simSub : passive) {
                LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
                if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                    if (simSub.getDeviceID().equals(hotWaterTankID)) {
                        predictedTankTemp.put(this.maxReferenceTime, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
                    }
                } else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
                    if (simSub.getDeviceID().equals(hotWaterTankID)) {
                        predictedTankTemp.put(this.maxReferenceTime, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
                    }
                }
            }

            //dummy AncillaryMeterState, we dont know the state of the ancillaryMeter at t=start, so set all to zero

            AncillaryMeterState meterState = new AncillaryMeterState();

            //send the first passive state to active nodes
            this.ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, this.activeNeedInputUUIDs, passiveToActiveMap);

            for (t = this.maxReferenceTime; t < this.maxOptimizationHorizon + this.STEP_SIZE; t += this.STEP_SIZE) {

                activeToPassiveMap.clearInnerStates();

                //let all active states calculate their next step
                for (InterdependentProblemPart<?, ?> part : allActive) {
                    part.calculateNextStep();
                    activeToPassiveMap.put(part.getDeviceID(), part.getCommodityOutputStates());
                }

                //generally setting demand to 0, will add to this if there is really a demand
                predictedHotWaterDemand.put(t, 0.0);
                predictedHotWaterSupply.put(t, 0.0);

                for (IOCEnergySubject simSub : allActive) {
                    LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();

                    if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                        double demand = outputStates.getPower(Commodity.HEATINGHOTWATERPOWER);
                        if (demand > 0.0) {
                            Double current = predictedHotWaterDemand.get(t);
                            predictedHotWaterDemand.put(t, current + demand);
                        } else if (demand < 0.0) {
                            Double current = predictedHotWaterSupply.get(t);
                            predictedHotWaterSupply.put(t, current + demand);
                        }
                    } else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
                        double demand = outputStates.getPower(Commodity.DOMESTICHOTWATERPOWER);
                        if (demand > 0.0) {
                            Double current = predictedHotWaterDemand.get(t);
                            predictedHotWaterDemand.put(t, current + demand);
                        } else if (demand < 0.0) {
                            Double current = predictedHotWaterSupply.get(t);
                            predictedHotWaterSupply.put(t, current + demand);
                        }
                    }
                }

                //send active state to passive nodes, save meterstate
                this.ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passiveArray, this.passiveUUIDs, meterState);

                ancillaryMeter.setLoadSequential(meterState, t);

                passiveToActiveMap.clearInnerStates();
                //let all passive states calculate their next step
                for (InterdependentProblemPart<?, ?> part : passive) {
                    part.calculateNextStep();
                    passiveToActiveMap.put(part.getDeviceID(), part.getCommodityOutputStates());
                }

                for (IOCEnergySubject simSub : passive) {

                    LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
                    if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                        if (simSub.getDeviceID().equals(hotWaterTankID)) {
                            predictedTankTemp.put(t + this.STEP_SIZE, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
                        }
                    } else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
                        if (simSub.getDeviceID().equals(hotWaterTankID)) {
                            predictedTankTemp.put(t + this.STEP_SIZE, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
                        }
                    }
                }

                //send new passive states to active nodes
                this.ocEnergySimulationCore.doPassiveToActiveExchange(meterState, allActiveNIArray, this.activeNeedInputUUIDs, passiveToActiveMap);
            }
            ancillaryMeter.endSequential();
            ancillaryMeter.setEndingTimeOfProfile(this.maxOptimizationHorizon);

            //add final schedules
            for (InterdependentProblemPart<?, ?> part : allIPPs) {
                schedules.add(part.getFinalInterdependentSchedule());
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
