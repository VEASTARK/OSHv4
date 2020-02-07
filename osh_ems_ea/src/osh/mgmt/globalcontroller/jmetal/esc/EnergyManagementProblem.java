package osh.mgmt.globalcontroller.jmetal.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.BinaryEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
import osh.esc.OCEnergySimulationCore;
import osh.esc.UUIDCommodityMap;
import osh.mgmt.globalcontroller.jmetal.IFitness;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Represents the optimization problem of the OSH simulation for use in JMetal algorithms.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class EnergyManagementProblem extends Problem {

    private static final long serialVersionUID = -4271677958277281538L;

    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    private final long ignoreLoadProfileBefore;
    private final long ignoreLoadProfileAfter;
    private long maxReferenceTime;
    private long maxOptimizationHorizon;

    private final int stepSize;
    private final IFitness fitnessFunction;
    private final SolutionDistributor distributor;

    private final EnergyProblemDataContainer baseDataContainer;

    //multithreading
    private boolean multiThreadingInitialized;
    private EnergyProblemDataContainer masterDataContainer;
    private ConcurrentLinkedQueue<EnergyProblemDataContainer> multiThreadedQueue;


    /**
     * Genererates a new Problem with the given constituents.
     *
     * @param problemParts all problem-parts of this problem
     * @param ocESC the energy-simulation-core to be used for the optimization loop
     * @param distributor the solution distributor for this problem
     * @param priceSignals the valid price-signals for this problem
     * @param powerLimitSignals the valid power-limit-signals for this problem
     * @param ignoreLoadProfileBefore the point in time before which all should be ignored for this optimization
     * @param ignoreLoadProfileAfter the point in time after which all should be ignored for this optimization
     * @param randomGenerator a generator for random numbers for use in this problem
     * @param fitnessFunction the function determining which fitness an evaluated solution has
     * @param stepSize the size of the time-steps to be used for the evaluation of solutions
     */
    public EnergyManagementProblem(
            List<InterdependentProblemPart<?, ?>> problemParts,
            OCEnergySimulationCore ocESC,
            SolutionDistributor distributor,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals,
            long ignoreLoadProfileBefore,
            long ignoreLoadProfileAfter,
            OSHRandomGenerator randomGenerator,
            IFitness fitnessFunction,
            int stepSize) {
        super(new PseudoRandom(randomGenerator));

        this.distributor = distributor;
        this.priceSignals = priceSignals;
        this.powerLimitSignals = powerLimitSignals;
        this.ignoreLoadProfileBefore = ignoreLoadProfileBefore;
        this.ignoreLoadProfileAfter = ignoreLoadProfileAfter;
        this.stepSize = stepSize;
        this.fitnessFunction = fitnessFunction;

        //mapping of uuid to problem-part id, needed for the construction of UUIDCommodityMaps
        Object2IntOpenHashMap<UUID> uuidIntMap = new Object2IntOpenHashMap<>();
        uuidIntMap.defaultReturnValue(-1);

        Set<UUID> allUUIDs = new HashSet<>();

        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap = new Object2ObjectOpenHashMap<>(problemParts.size());
        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap = new Object2ObjectOpenHashMap<>(problemParts.size());

        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (uuidIntMap.put(part.getUUID(), part.getId()) != -1) {
                throw new IllegalArgumentException("multiple IPPs with same UUID");
            }
            if (!part.isCompletelyStatic()) {
                allUUIDs.add(part.getUUID());
                uuidOutputMap.put(part.getUUID(), part.getAllOutputCommodities());
                uuidInputMap.put(part.getUUID(), part.getAllInputCommodities());
            }
        }

        Set<UUID> activeUUIDs = new HashSet<>();
        Set<UUID> activeNeedsInputUUIDs = new HashSet<>();
        Set<UUID> passiveUUIDs = new HashSet<>();
        //splitting all parts into active and passive parts to enable two-step exchange
        ocESC.splitActivePassive(allUUIDs, activeUUIDs, passiveUUIDs);

        List<InterdependentProblemPart<?, ?>> allActivePPs = new ArrayList<>();
        List<InterdependentProblemPart<?, ?>> allPassivePPs = new ArrayList<>();
        List<InterdependentProblemPart<?, ?>> allActiveNeedsInputPPs = new ArrayList<>();

        //calculating all sub-collections and the maxReferenceTime used
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (activeUUIDs.contains(part.getUUID())) {
                allActivePPs.add(part);
                if (part.isReactsToInputStates()) {
                    activeNeedsInputUUIDs.add(part.getUUID());
                    allActiveNeedsInputPPs.add(part);
                }
            } else if (passiveUUIDs.contains(part.getUUID())) {
                allPassivePPs.add(part);
            } else {
                if (!part.isCompletelyStatic())
                    throw new IllegalArgumentException("part is neither active nor passive");
                //we can ignore fully static parts for the calculation
                activeUUIDs.remove(part.getUUID());
                passiveUUIDs.remove(part.getUUID());
            }

            this.maxReferenceTime = Math.max(this.maxReferenceTime, part.getReferenceTime());
            if (part instanceof ControllableIPP<?, ?>) {
                this.maxOptimizationHorizon = Math.max(part.getOptimizationHorizon(), this.maxOptimizationHorizon);
            }
        }

        //to keep backwards-cpmpatibility we need to reorder the parts slightly as a different order will lead to
        // slight differences due to floating-point error TODO: remove with next backwards-compatibility breaking
        // update and order by part-id (not uuid)
        List<InterdependentProblemPart<?, ?>> allIPPsReordered = new ArrayList<>(problemParts.size());
        allIPPsReordered.addAll(allActiveNeedsInputPPs);
        allIPPsReordered.addAll(allActivePPs.stream().filter(p -> !allActiveNeedsInputPPs.contains(p)).collect(Collectors.toList()));
        allIPPsReordered.addAll(allPassivePPs);
        allIPPsReordered.addAll(problemParts.stream().filter(p -> !allIPPsReordered.contains(p)).collect(Collectors.toList()));

        ocESC.initializeGrids(activeUUIDs, activeNeedsInputUUIDs, passiveUUIDs,
                uuidIntMap, uuidOutputMap, uuidInputMap);

        UUIDCommodityMap baseActiveToPassiveMap = new UUIDCommodityMap(activeUUIDs, uuidIntMap, true);
        UUIDCommodityMap basePassiveToActiveMap = new UUIDCommodityMap(passiveUUIDs, uuidIntMap, true);

        this.baseDataContainer = new EnergyProblemDataContainer(allIPPsReordered, allActivePPs, allPassivePPs,
                allActiveNeedsInputPPs, ocESC, baseActiveToPassiveMap, basePassiveToActiveMap);

        this.numberOfVariables_ = 1;
        this.numberOfObjectives_ = 1;
        this.numberOfConstraints_ = 0;
        this.problemName_ = "osh";
        this.solutionType_ = new BinarySolutionType(this);

        this.length_ = new int[this.numberOfVariables_];
        this.length_[0] =
                ((BinaryEncodedVariableInformation) distributor.getVariableInformation(VariableEncoding.BINARY)).getBitCount();
    }

    /**
     * Prepares this problem for use in a multi-threaded environment
     */
    public void initializeMultithreading() {
        if (!this.multiThreadingInitialized) {
            this.multiThreadingInitialized = true;
            this.multiThreadedQueue = new ConcurrentLinkedQueue<>();

            //set logger to null so that deep copy does not try to copy it
            IGlobalLogger temp = null;
            for (InterdependentProblemPart<?, ?> part : this.baseDataContainer.getAllProblemParts()) {
                temp = part.logger;
                part.logger = null;

                part.initializeInterdependentCalculation(this.maxReferenceTime, this.stepSize, false, false);
                part.prepareForDeepCopy();
            }

            this.masterDataContainer = this.baseDataContainer.getDeepCopy();

            for (InterdependentProblemPart<?, ?> part : this.baseDataContainer.getAllProblemParts()) {
                //restore logger to base data
                part.logger = temp;
            }
        }
    }

    /**
     * Requests a copy of all relevant data to be used for the evaluation of a solution.
     *
     * @return a container of all relevant data
     */
    private EnergyProblemDataContainer requestDataCopy() {
        if (!this.multiThreadingInitialized) return this.baseDataContainer;

        EnergyProblemDataContainer dataContainer = this.multiThreadedQueue.poll();

        if (dataContainer != null) return dataContainer;
        else return this.masterDataContainer.getDeepCopy();
    }

    /**
     * Frees the given copy of all relevant data to be used by another evaluation.
     *
     * @param dataContainer a container of all relevant data
     */
    private void freeDataCopy(EnergyProblemDataContainer dataContainer) {
        if (this.multiThreadingInitialized) {
            this.multiThreadedQueue.add(dataContainer);
        }
    }

    /**
     * Returns all grids to a state before they were adjusted for this specific problem.
     */
    public void finalizeGrids() {
        this.baseDataContainer.getOcESC().finalizeGrids();
        this.multiThreadingInitialized = false;
        this.multiThreadedQueue = null;
    }

    /**
     * Resets the state of the multithreaded initialisation and evaluates the given solution with additional logging.
     *
     * @param solution the solution to be evaluated
     * @param log flag if additional logging should be done
     */
    public void evaluateFinalTime(Solution solution, boolean log) {
        this.evaluateFinalTime(solution, log, new AncillaryCommodityLoadProfile());
    }

    /**
     * Resets the state of the multithreaded initialisation and evaluates the given solution with additional logging
     * and stores the resulting calculates loads in the given load profile.
     *
     * @param solution the solution to be evaluated
     * @param log flag if additional logging should be done
     * @param ancillaryMeter the load profile in which the resulting calculated load will be entered
     */
    public void evaluateFinalTime(Solution solution, boolean log, AncillaryCommodityLoadProfile ancillaryMeter) {
        this.multiThreadingInitialized = false;
        this.evaluate(solution, log, true, ancillaryMeter);
        this.finalizeGrids();
    }

    @Override
    public void evaluate(Solution solution) {
        this.evaluate(solution, false, false, new AncillaryCommodityLoadProfile());
    }

    /**
     * Evaluates the given solution with additonal logging depending on the given flags.
     *
     * @param solution the solution to be evaluated
     * @param log flag if additional logging should be done
     * @param keepPrediction flag if problem-parts should log their prediction about the future
     * @param ancillaryMeter the load profile in which the resulting calculated load will be entered
     */
    private void evaluate(Solution solution, boolean log, boolean keepPrediction, AncillaryCommodityLoadProfile ancillaryMeter) {

        EnergyProblemDataContainer dataContainer = this.requestDataCopy();

        List<InterdependentProblemPart<?, ?>> allIPPs = dataContainer.getAllProblemParts();
        List<InterdependentProblemPart<?, ?>> allActive = dataContainer.getAllActivePPs();
        List<InterdependentProblemPart<?, ?>> allPassive = dataContainer.getAllPassivePPs();
        List<InterdependentProblemPart<?, ?>> allActiveNeedsInput = dataContainer.getAllActiveNeedsInputPPs();
        OCEnergySimulationCore ocESC = dataContainer.getOcESC();
        UUIDCommodityMap activeToPassiveMap = dataContainer.getActiveToPassiveMap();
        UUIDCommodityMap passiveToActiveMap = dataContainer.getPassiveToActiveMap();

        //clear past information that may be still contained in the maps
        activeToPassiveMap.clearInnerStates();
        passiveToActiveMap.clearInnerStates();

        //distribute the solution to be evaluated
        this.distributor.distributeSolution(solution, allIPPs);

        //reset all ipps to their initial state
        for (InterdependentProblemPart<?, ?> part : allIPPs) {
            part.initializeInterdependentCalculation(
                    this.maxReferenceTime,
                    this.stepSize,
                    log,
                    keepPrediction
            );
        }

        //initialize sequential entering of data
        ancillaryMeter.initSequential();

        //let all passive states calculate their first state
        for (InterdependentProblemPart<?, ?> part : allPassive) {
            part.calculateNextStep();
            passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
        }

        //dummy AncillaryMeterState, we don't know the state of the ancillaryMeter at the start, thus set all to zero
        AncillaryMeterState meterState = new AncillaryMeterState();

        //send the first passive state to active nodes
        ocESC.doPassiveToActiveExchange(meterState, allActiveNeedsInput, passiveToActiveMap);

        // iterate
        for (long t = this.maxReferenceTime; t < this.maxOptimizationHorizon + this.stepSize; t += this.stepSize) {

            //let all active states calculate their next step
            for (InterdependentProblemPart<?, ?> part : allActive) {
                part.calculateNextStep();
                activeToPassiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            //send active state to passive nodes, save meter state
            ocESC.doActiveToPassiveExchange(activeToPassiveMap, allPassive, meterState);

            //send loads to the ancillary meter profile
            ancillaryMeter.setLoadSequential(meterState, t);

            //let all passive states calculate their next step
            for (InterdependentProblemPart<?, ?> part : allPassive) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            //send new passive states to active nodes
            ocESC.doPassiveToActiveExchange(meterState, allActiveNeedsInput, passiveToActiveMap);

        }

        //mark that no more data will be added and the profile is finalized
        ancillaryMeter.endSequential();
        ancillaryMeter.setEndingTimeOfProfile(this.maxOptimizationHorizon);


        // calculate variable fitness depending on price signals...
        double fitness = this.fitnessFunction.getFitnessValue(
                this.ignoreLoadProfileBefore,
                this.ignoreLoadProfileAfter,
                ancillaryMeter,
                this.priceSignals,
                this.powerLimitSignals
        );

        // add lukewarm cervisia (i.e. additional fixed costs...)
        for (InterdependentProblemPart<?, ?> problempart : allIPPs) {
            problempart.finalizeInterdependentCervisia();
            double add = problempart.getInterdependentCervisia();
            fitness += add;

            if (log && !((Binary) solution.getDecisionVariables()[0]).bits_.get(0) && add != 0) {
                OSH_gGAMultiThread.logCervisia(problempart.getDeviceType(), add);
            }
        }

        solution.setObjective(0, fitness);
        solution.setFitness(fitness);

        //free the used data for another evaluation
        this.freeDataCopy(dataContainer);
    }
}