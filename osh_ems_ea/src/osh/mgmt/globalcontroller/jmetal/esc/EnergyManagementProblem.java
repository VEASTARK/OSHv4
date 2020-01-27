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
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
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
    final Set<UUID> passiveUUIDs = new HashSet<>();
    final Set<UUID> activeNeedInputUUIDs = new HashSet<>();
    final Object2IntOpenHashMap<UUID> uuidIntMap;
    private final IFitness fitnessFunction;
    // active nodes, need information about commodity input states (new IPP)
    private final InterdependentProblemPart<?, ?>[] activeNeedsInput;
    // active nodes
    private final InterdependentProblemPart<?, ?>[] activeWorksAlone;
    // passive nodes
    private final InterdependentProblemPart<?, ?>[] passive;
    // static PP
    private final InterdependentProblemPart<?, ?>[] staticParts;
    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    private final long ignoreLoadProfileBefore;
    private final long ignoreLoadProfileAfter;
    private boolean multiThreading;
    private ObjectArrayList<InterdependentProblemPart<?, ?>[]> multiThreadedActiveNeedsInput;
    private ObjectArrayList<InterdependentProblemPart<?, ?>[]> multiThreadedActiveWorksAlone;
    private ObjectArrayList<InterdependentProblemPart<?, ?>[]> multiThreadedPassive;
    private ObjectArrayList<InterdependentProblemPart<?, ?>[]> multiThreadedStatic;
    private ObjectArrayList<OCEnergySimulationCore> multiOCs;
    private InterdependentProblemPart<?, ?>[] multiThreadedMasterCopiesActiveNI;
    private InterdependentProblemPart<?, ?>[] multiThreadedMasterCopiesActiveWA;
    private InterdependentProblemPart<?, ?>[] multiThreadedMasterCopiesPassive;
    private InterdependentProblemPart<?, ?>[] multiThreadedMasterCopiesStatic;
    private Long maxReferenceTime;
    private Long maxOptimizationHorizon;
    private boolean keepPrediction;
    private final OCEnergySimulationCore ocEnergySimulationCore;
    private final SolutionDistributor distributor;


    /**
     * CONSTRUCTOR
     *
     */
    public EnergyManagementProblem(
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            SolutionDistributor distributor,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            long ignoreLoadProfileAfter,
            OSHRandomGenerator randomGenerator,
            IGlobalLogger globalLogger,
            IFitness fitnessFunction,
            int STEP_SIZE) {
        super(new PseudoRandom(randomGenerator));

        List<InterdependentProblemPart<?, ?>> activeNeedsInput = new ArrayList<>();
        List<InterdependentProblemPart<?, ?>> activeWorksAlone = new ArrayList<>();
        List<InterdependentProblemPart<?, ?>> passive = new ArrayList<>();
        List<InterdependentProblemPart<?, ?>> staticParts = new ArrayList<>();

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
        this.distributor = distributor;

        this.fitnessFunction = fitnessFunction;

        Set<UUID> allUUIDs = new HashSet<>();
        Set<UUID> activeUUIDs = new HashSet<>();
        Set<UUID> passiveUUIDs = new HashSet<>();

        this.uuidIntMap = new Object2IntOpenHashMap<>(problemParts.length);
        this.uuidIntMap.defaultReturnValue(-1);

        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap = new Object2ObjectOpenHashMap<>(problemParts.length);
        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap = new Object2ObjectOpenHashMap<>(problemParts.length);

        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (this.uuidIntMap.put(part.getUUID(), part.getId()) != -1) {
                throw new IllegalArgumentException("multiple IPPs with same UUID");
            }
            if (!part.isCompletelyStatic()) {
                allUUIDs.add(part.getUUID());
                uuidOutputMap.put(part.getUUID(), part.getAllOutputCommodities());
                uuidInputMap.put(part.getUUID(), part.getAllInputCommodities());
            }
        }

        this.ocEnergySimulationCore.splitActivePassive(allUUIDs, activeUUIDs, passiveUUIDs);

        //split parts in 4 lists
        // calc maxReferenceTime of parts
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (activeUUIDs.contains(part.getUUID())) {
                if (part.isReactsToInputStates()) {
                    activeNeedsInput.add(part);
                } else {
                    activeWorksAlone.add(part);
                }
            } else if (passiveUUIDs.contains(part.getUUID())) {
                passive.add(part);
            } else {
                if (part.isCompletelyStatic())
                    staticParts.add(part);
                else
                    throw new IllegalArgumentException("part is neither active nor passive");
            }

            if (this.maxReferenceTime == null) {
                this.maxReferenceTime = part.getReferenceTime();
            } else {
                this.maxReferenceTime = Math.max(this.maxReferenceTime, part.getReferenceTime());
            }
        }

        this.activeNeedsInput = activeNeedsInput.toArray(InterdependentProblemPart<?, ?>[]::new);
        this.activeWorksAlone = activeWorksAlone.toArray(InterdependentProblemPart<?, ?>[]::new);
        this.passive = passive.toArray(InterdependentProblemPart<?, ?>[]::new);
        this.staticParts = staticParts.toArray(InterdependentProblemPart<?, ?>[]::new);

        Set<UUID> allActive = new HashSet<>();

        for (InterdependentProblemPart<?, ?> part : this.activeNeedsInput) {
            allActive.add(part.getUUID());
            this.activeNeedInputUUIDs.add(part.getUUID());
        }
        for (InterdependentProblemPart<?, ?> part : this.activeWorksAlone) {
            allActive.add(part.getUUID());
        }
        for (InterdependentProblemPart<?, ?> part : this.passive) {
            this.passiveUUIDs.add(part.getUUID());
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
        this.length_[0] =
                ((BinaryEncodedVariableInformation) distributor.getVariableInformation(VariableEncoding.BINARY)).getBitCount();

    }

    public void initMultithreading() {
        this.multiThreading = true;
        this.multiThreadedActiveNeedsInput = new ObjectArrayList<>();
        this.multiThreadedActiveWorksAlone = new ObjectArrayList<>();
        this.multiThreadedPassive = new ObjectArrayList<>();
        this.multiThreadedStatic = new ObjectArrayList<>();
        this.multiOCs = new ObjectArrayList<>();

        this.multiThreadedMasterCopiesActiveNI = new InterdependentProblemPart<?, ?>[this.activeNeedsInput.length];
        this.multiThreadedMasterCopiesActiveWA = new InterdependentProblemPart<?, ?>[this.activeWorksAlone.length];
        this.multiThreadedMasterCopiesPassive = new InterdependentProblemPart<?, ?>[this.passive.length];
        this.multiThreadedMasterCopiesStatic = new InterdependentProblemPart<?, ?>[this.staticParts.length];

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
                part.initializeInterdependentCalculation(this.maxReferenceTime, this.STEP_SIZE, false, false);
            }
        }
        for (InterdependentProblemPart<?, ?> part : this.passive) {
            temp = part.logger;
            part.logger = null;
            part.prepareForDeepCopy();
            if (part instanceof NonControllableIPP<?, ?>) {
                //initialize completely static IPP so we can save that time on every copy
                part.initializeInterdependentCalculation(this.maxReferenceTime, this.STEP_SIZE, false, false);
            }
        }

        //create one master copy per ProblemPart
        for (int j = 0; j < this.activeNeedsInput.length; j++) {
            InterdependentProblemPart<?, ?> part = this.activeNeedsInput[j];
            this.multiThreadedMasterCopiesActiveNI[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
        }
        for (int j = 0; j < this.activeWorksAlone.length; j++) {
            InterdependentProblemPart<?, ?> part = this.activeWorksAlone[j];
            this.multiThreadedMasterCopiesActiveWA[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
        }
        for (int j = 0; j < this.passive.length; j++) {
            InterdependentProblemPart<?, ?> part = this.passive[j];
            this.multiThreadedMasterCopiesPassive[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
        }
        for (int j = 0; j < this.staticParts.length; j++) {
            InterdependentProblemPart<?, ?> part = this.staticParts[j];
            this.multiThreadedMasterCopiesStatic[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
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
    private synchronized InterdependentProblemPart<?, ?>[][] requestIPPCopies() {
        InterdependentProblemPart<?, ?>[][] ret = new InterdependentProblemPart<?, ?>[4][];

        if (!this.multiThreadedActiveNeedsInput.isEmpty()) {
            ret[0] = this.multiThreadedActiveNeedsInput.remove(0);
            ret[1] = this.multiThreadedActiveWorksAlone.remove(0);
            ret[2] = this.multiThreadedPassive.remove(0);
            ret[3] = this.multiThreadedStatic.remove(0);
        } else {
            InterdependentProblemPart<?, ?>[] niList =
                    new InterdependentProblemPart<?, ?>[this.multiThreadedMasterCopiesActiveNI.length];
            InterdependentProblemPart<?, ?>[] waList = new InterdependentProblemPart<?, ?>[this.multiThreadedMasterCopiesActiveWA.length];
            InterdependentProblemPart<?, ?>[] paList = new InterdependentProblemPart<?, ?>[this.multiThreadedMasterCopiesPassive.length];
            InterdependentProblemPart<?, ?>[] stList = new InterdependentProblemPart<?, ?>[this.multiThreadedMasterCopiesStatic.length];

            for (int j = 0; j < this.multiThreadedMasterCopiesActiveNI.length; j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesActiveNI[j];
                niList[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesActiveWA.length; j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesActiveWA[j];
                waList[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesPassive.length; j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesPassive[j];
                paList[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
            }
            for (int j = 0; j < this.multiThreadedMasterCopiesStatic.length; j++) {
                InterdependentProblemPart<?, ?> part = this.multiThreadedMasterCopiesStatic[j];
                stList[j] = (InterdependentProblemPart<?, ?>) DeepCopy.copy(part);
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

    private synchronized void freeIPPCopies(InterdependentProblemPart<?, ?>[] needsI,
                                            InterdependentProblemPart<?, ?>[] worksA,
                                            InterdependentProblemPart<?, ?>[] passive,
                                            InterdependentProblemPart<?, ?>[] staticParts) {
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

        InterdependentProblemPart<?, ?>[] activeNeedsInput;
        InterdependentProblemPart<?, ?>[] activeWorksAlone;
        InterdependentProblemPart<?, ?>[] passive;
        InterdependentProblemPart<?, ?>[] staticParts;
        InterdependentProblemPart<?, ?>[] allIPPs;
        OCEnergySimulationCore ocEnergySimulationCore;

        if (this.multiThreading) {
            InterdependentProblemPart<?, ?>[][] copies = this.requestIPPCopies();
            activeNeedsInput = copies[0];
            activeWorksAlone = copies[1];
            passive = copies[2];
            staticParts = copies[3];
            ocEnergySimulationCore = this.requestOCESC();
        } else {
            activeNeedsInput = this.activeNeedsInput;
            activeWorksAlone = this.activeWorksAlone;
            passive = this.passive;
            staticParts = this.staticParts;
            ocEnergySimulationCore = this.ocEnergySimulationCore;
        }
        allIPPs = new InterdependentProblemPart<?, ?>[activeNeedsInput.length + activeWorksAlone.length + passive.length + staticParts.length];
        int index = 0;
        for (InterdependentProblemPart<?, ?> part : activeNeedsInput) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : activeWorksAlone) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : passive) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : staticParts) {
            allIPPs[index++] = part;
        }

        this.distributor.distributeSolution(solution, allIPPs);


        try {
            double fitness;

            // calculate interdependent parts

            // initialize
            for (InterdependentProblemPart<?, ?> part : allIPPs) {
                part.initializeInterdependentCalculation(
                        this.maxReferenceTime,
                        this.STEP_SIZE,
                        false,
                        this.keepPrediction
                );
            }

            // go through step by step
            AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();
            ancillaryMeter.initSequential();

            InterdependentProblemPart<?, ?>[] allActiveArray =
                    new InterdependentProblemPart<?, ?>[activeNeedsInput.length + activeWorksAlone.length];
            index = 0;
            for (InterdependentProblemPart<?, ?> part : activeNeedsInput) allActiveArray[index++] = part;
            for (InterdependentProblemPart<?, ?> part : activeWorksAlone) allActiveArray[index++] = part;


            // go through in steps of STEP_SIZE (in ticks)
            //init the maps for the commodity states
            UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActiveArray, this.uuidIntMap, true);

            UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passive, this.uuidIntMap, true);

            long t;

            //let all passive states calculate their first state
            for (InterdependentProblemPart<?, ?> part : passive) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            //dummy AncillaryMeterState, we don't know the state of the ancillaryMeter at t=t_start, thus set all to zero
            AncillaryMeterState meterState = new AncillaryMeterState();

            //send the first passive state to active nodes
            ocEnergySimulationCore.doPassiveToActiveExchange(meterState, activeNeedsInput, this.activeNeedInputUUIDs, passiveToActiveMap);

            // iterate
            for (t = this.maxReferenceTime; t < this.maxOptimizationHorizon + this.STEP_SIZE; t += this.STEP_SIZE) {

                //let all active states calculate their next step
                for (InterdependentProblemPart<?, ?> part : allActiveArray) {
                    part.calculateNextStep();
                    activeToPassiveMap.put(part.getId(), part.getCommodityOutputStates());
                }

                //send active state to passive nodes, save meter state
                ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passive, this.passiveUUIDs, meterState);

                //send loads to the ancillary meter profile
                ancillaryMeter.setLoadSequential(meterState, t);

                //let all passive states calculate their next step
                for (InterdependentProblemPart<?, ?> part : passive) {
                    part.calculateNextStep();
                    passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
                }

                //send new passive states to active nodes
                ocEnergySimulationCore.doPassiveToActiveExchange(meterState, activeNeedsInput, this.activeNeedInputUUIDs,
                        passiveToActiveMap);

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

    public void evaluateWithDebuggingInformation(Solution solution,
                                                 AncillaryCommodityLoadProfile ancillaryMeter,
                                                 TreeMap<Long, Double> predictedTankTemp,
                                                 TreeMap<Long, Double> predictedHotWaterDemand,
                                                 TreeMap<Long, Double> predictedHotWaterSupply,
                                                 List<Schedule> schedules,
                                                 boolean keepPrediction,
                                                 UUID hotWaterTankID) {

        ancillaryMeter.initSequential();

        InterdependentProblemPart<?, ?>[] activeNeedsInput = this.activeNeedsInput;
        InterdependentProblemPart<?, ?>[] activeWorksAlone = this.activeWorksAlone;
        InterdependentProblemPart<?, ?>[] passive = this.passive;
        InterdependentProblemPart<?, ?>[] staticParts = this.staticParts;

        InterdependentProblemPart<?, ?>[] allIPPs =
                new InterdependentProblemPart<?, ?>[activeNeedsInput.length + activeWorksAlone.length + passive.length + staticParts.length];
        int index = 0;
        for (InterdependentProblemPart<?, ?> part : activeNeedsInput) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : activeWorksAlone) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : passive) {
            allIPPs[index++] = part;
        }
        for (InterdependentProblemPart<?, ?> part : staticParts) {
            allIPPs[index++] = part;
        }

        try {

            this.distributor.distributeSolution(solution, allIPPs);

            // calculate interdependent parts

            // initialize
            for (InterdependentProblemPart<?, ?> part : allIPPs) {
                part.initializeInterdependentCalculation(
                        this.maxReferenceTime,
                        this.STEP_SIZE,
                        true,
                        keepPrediction
                );
            }

            InterdependentProblemPart<?, ?>[] allActiveArray =
                    new InterdependentProblemPart<?, ?>[activeNeedsInput.length + activeWorksAlone.length];
            index = 0;
            for (InterdependentProblemPart<?, ?> part : activeNeedsInput) allActiveArray[index++] = part;
            for (InterdependentProblemPart<?, ?> part : activeWorksAlone) allActiveArray[index++] = part;

            // go through in steps of STEP_SIZE (in ticks)
            long t;

            //init the maps for the commodity states
            UUIDCommodityMap activeToPassiveMap = new UUIDCommodityMap(allActiveArray, this.uuidIntMap);
            UUIDCommodityMap passiveToActiveMap = new UUIDCommodityMap(passive, this.uuidIntMap);

            //let all passive states calculate their first state
            for (InterdependentProblemPart<?, ?> part : passive) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            for (IOCEnergySubject simSub : passive) {
                LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
                if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                    if (simSub.getUUID().equals(hotWaterTankID)) {
                        predictedTankTemp.put(this.maxReferenceTime, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
                    }
                } else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
                    if (simSub.getUUID().equals(hotWaterTankID)) {
                        predictedTankTemp.put(this.maxReferenceTime, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
                    }
                }
            }

            //dummy AncillaryMeterState, we dont know the state of the ancillaryMeter at t=start, so set all to zero

            AncillaryMeterState meterState = new AncillaryMeterState();

            //send the first passive state to active nodes
            this.ocEnergySimulationCore.doPassiveToActiveExchange(meterState, activeNeedsInput, this.activeNeedInputUUIDs,
                    passiveToActiveMap);

            for (t = this.maxReferenceTime; t < this.maxOptimizationHorizon + this.STEP_SIZE; t += this.STEP_SIZE) {

                activeToPassiveMap.clearInnerStates();

                //let all active states calculate their next step
                for (InterdependentProblemPart<?, ?> part : allActiveArray) {
                    part.calculateNextStep();
                    activeToPassiveMap.put(part.getUUID(), part.getCommodityOutputStates());
                }

                //generally setting demand to 0, will add to this if there is really a demand
                predictedHotWaterDemand.put(t, 0.0);
                predictedHotWaterSupply.put(t, 0.0);

                for (IOCEnergySubject simSub : allActiveArray) {
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
                this.ocEnergySimulationCore.doActiveToPassiveExchange(activeToPassiveMap, passive, this.passiveUUIDs,
                        meterState);

                ancillaryMeter.setLoadSequential(meterState, t);

                passiveToActiveMap.clearInnerStates();
                //let all passive states calculate their next step
                for (InterdependentProblemPart<?, ?> part : passive) {
                    part.calculateNextStep();
                    passiveToActiveMap.put(part.getUUID(), part.getCommodityOutputStates());
                }

                for (IOCEnergySubject simSub : passive) {

                    LimitedCommodityStateMap outputStates = simSub.getCommodityOutputStates();
                    if (outputStates != null && outputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
                        if (simSub.getUUID().equals(hotWaterTankID)) {
                            predictedTankTemp.put(t + this.STEP_SIZE, outputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER));
                        }
                    } else if (outputStates != null && outputStates.containsCommodity(Commodity.DOMESTICHOTWATERPOWER)) {
                        if (simSub.getUUID().equals(hotWaterTankID)) {
                            predictedTankTemp.put(t + this.STEP_SIZE, outputStates.getTemperature(Commodity.DOMESTICHOTWATERPOWER));
                        }
                    }
                }

                //send new passive states to active nodes
                this.ocEnergySimulationCore.doPassiveToActiveExchange(meterState, activeNeedsInput, this.activeNeedInputUUIDs,
                        passiveToActiveMap);
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
