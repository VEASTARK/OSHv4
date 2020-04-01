package osh.mgmt.globalcontroller.jmetal.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.uma.jmetal.solution.Solution;
import osh.configuration.oc.EAObjectives;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ErsatzACLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.esc.UUIDCommodityMap;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.utils.CostReturnType;
import osh.utils.costs.OptimizationCostFunction;
import osh.utils.dataStructures.Enum2DoubleMap;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents the evaluator of the optimization problem of the OSH simulation for use in JMetal algorithms.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class EMProblemEvaluator {

    private final long ignoreLoadProfileBefore;
    private final long ignoreLoadProfileAfter;
    private long maxReferenceTime;
    private long maxOptimizationHorizon;

    private final int stepSize;
    private final OptimizationCostFunction costFunction;
    private final IEALogger eaLogger;
    private final SolutionDistributor distributor;

    private final List<EAObjectives> eaObjectives;

    private final EnergyProblemDataContainer baseDataContainer;

    //multithreading
    private boolean multiThreadingInitialized;
    private EnergyProblemDataContainer masterDataContainer;
    private ConcurrentLinkedQueue<EnergyProblemDataContainer> multiThreadedQueue;


    /**
     * Genererates a new Evaluator with the given constituents.
     *
     * @param problemParts all problem-parts of this problem
     * @param ocESC the energy-simulation-core to be used for the optimization loop
     * @param distributor the solution distributor for this problem
     * @param ignoreLoadProfileBefore the point in time before which all should be ignored for this optimization
     * @param ignoreLoadProfileAfter the point in time after which all should be ignored for this optimization
     * @param costFunction the function determining which fitness an evaluated solution has
     * @param stepSize the size of the time-steps to be used for the evaluation of solutions
     * @param eaObjectives the collection of all EA-objectvies
     */
    public EMProblemEvaluator(
            InterdependentProblemPart<?, ?>[] problemParts,
            OCEnergySimulationCore ocESC,
            SolutionDistributor distributor,
            long ignoreLoadProfileBefore,
            long ignoreLoadProfileAfter,
            OptimizationCostFunction costFunction,
            IEALogger eaLogger,
            int stepSize,
            List<EAObjectives> eaObjectives) {

        this.distributor = distributor;
        this.ignoreLoadProfileBefore = ignoreLoadProfileBefore;
        this.ignoreLoadProfileAfter = ignoreLoadProfileAfter;
        this.stepSize = stepSize;
        this.costFunction = costFunction;
        this.eaLogger = eaLogger;
        this.eaObjectives = eaObjectives;

        //mapping of uuid to problem-part id, needed for the construction of UUIDCommodityMaps
        Object2IntOpenHashMap<UUID> uuidIntMap = new Object2IntOpenHashMap<>();
        uuidIntMap.defaultReturnValue(-1);

        Set<UUID> allUUIDs = new HashSet<>();

        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidOutputMap =
                new Object2ObjectOpenHashMap<>(problemParts.length);
        Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap =
                new Object2ObjectOpenHashMap<>(problemParts.length);

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

        InterdependentProblemPart<?, ?>[] allActivePPs = new InterdependentProblemPart[activeUUIDs.size()];
        InterdependentProblemPart<?, ?>[] allPassivePPs = new InterdependentProblemPart[passiveUUIDs.size()];
        List<InterdependentProblemPart<?, ?>> allActiveNeedsInputPPsList = new ArrayList<>();

        int activeIndex = 0, passiveIndex = 0;

        //calculating all sub-collections and the maxReferenceTime used
        for (InterdependentProblemPart<?, ?> part : problemParts) {
            if (activeUUIDs.contains(part.getUUID())) {
                allActivePPs[activeIndex++] = part;
                if (part.isReactsToInputStates()) {
                    activeNeedsInputUUIDs.add(part.getUUID());
                    allActiveNeedsInputPPsList.add(part);
                }
            } else if (passiveUUIDs.contains(part.getUUID())) {
                allPassivePPs[passiveIndex++] = part;
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

        InterdependentProblemPart<?, ?>[] allActiveNeedsInputPPs =
                new InterdependentProblemPart[allActiveNeedsInputPPsList.size()];
        allActiveNeedsInputPPs = allActiveNeedsInputPPsList.toArray(allActiveNeedsInputPPs);

        //sort all problem-parts to ensure the same order of execution
        Arrays.sort(problemParts, Comparator.comparingInt(InterdependentProblemPart::getId));
        Arrays.sort(allActivePPs, Comparator.comparingInt(InterdependentProblemPart::getId));
        Arrays.sort(allPassivePPs, Comparator.comparingInt(InterdependentProblemPart::getId));
        Arrays.sort(allActiveNeedsInputPPs, Comparator.comparingInt(InterdependentProblemPart::getId));

        ocESC.initializeGrids(activeUUIDs, activeNeedsInputUUIDs, passiveUUIDs,
                uuidIntMap, uuidOutputMap, uuidInputMap);

        UUIDCommodityMap baseActiveToPassiveMap = new UUIDCommodityMap(activeUUIDs, uuidIntMap, true);
        UUIDCommodityMap basePassiveToActiveMap = new UUIDCommodityMap(passiveUUIDs, uuidIntMap, true);

        int maxLength =
                (int) Math.ceil((double) (this.maxOptimizationHorizon - this.maxReferenceTime) / this.stepSize) + 2;
        ErsatzACLoadProfile ancillaryLoadProfile = new ErsatzACLoadProfile(maxLength);

        this.baseDataContainer = new EnergyProblemDataContainer(problemParts, allActivePPs, allPassivePPs,
                allActiveNeedsInputPPs, ocESC, baseActiveToPassiveMap, basePassiveToActiveMap, ancillaryLoadProfile);

    }

    /**
     * Prepares this problem for use in a multi-threaded environment
     */
    public void initializeMultithreading() {
        if (!this.multiThreadingInitialized) {
            this.multiThreadingInitialized = true;
            this.multiThreadedQueue = new ConcurrentLinkedQueue<>();

            //set logger to null so that deep copy does not try to copy it
            for (InterdependentProblemPart<?, ?> part : this.baseDataContainer.getAllProblemParts()) {
                part.initializeInterdependentCalculation(this.maxReferenceTime, this.stepSize, false, false);
            }

            this.masterDataContainer = this.baseDataContainer.getDeepCopy();
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
    public <S extends Solution<?>> void evaluateFinalTime(S solution, boolean log) {
        this.evaluateFinalTime(solution, log, null);
    }

    /**
     * Resets the state of the multithreaded initialisation and evaluates the given solution with additional logging
     * and stores the resulting calculates loads in the given load profile.
     *
     * @param solution the solution to be evaluated
     * @param log flag if additional logging should be done
     * @param ancillaryLoadProfile the load profile in which the resulting calculated load will be entered
     */
    public <S extends Solution<?>> void evaluateFinalTime(S solution, boolean log,
                                                          ErsatzACLoadProfile ancillaryLoadProfile) {
        this.multiThreadingInitialized = false;
        this.evaluate(solution, log, true, ancillaryLoadProfile);
        this.finalizeGrids();
    }

    public <S extends Solution<?>> void evaluate(S solution) {
        this.evaluate(solution, false, false, null);
    }

    /**
     * Evaluates the given solution with additonal logging depending on the given flags.
     *
     * @param solution the solution to be evaluated
     * @param log flag if additional logging should be done
     * @param keepPrediction flag if problem-parts should log their prediction about the future
     * @param loadProfile the load profile in which the resulting calculated load will be entered
     */
    private <S extends Solution<?>> void evaluate(S solution, boolean log, boolean keepPrediction,
                                                  ErsatzACLoadProfile loadProfile) {

        EnergyProblemDataContainer dataContainer = this.requestDataCopy();

        InterdependentProblemPart<?, ?>[] allIPPs = dataContainer.getAllProblemParts();
        InterdependentProblemPart<?, ?>[] allActive = dataContainer.getAllActivePPs();
        InterdependentProblemPart<?, ?>[] allPassive = dataContainer.getAllPassivePPs();
        InterdependentProblemPart<?, ?>[] allActiveNeedsInput = dataContainer.getAllActiveNeedsInputPPs();
        OCEnergySimulationCore ocESC = dataContainer.getOcESC();
        UUIDCommodityMap activeToPassiveMap = dataContainer.getActiveToPassiveMap();
        UUIDCommodityMap passiveToActiveMap = dataContainer.getPassiveToActiveMap();
        ErsatzACLoadProfile ancillaryLoadProfile = loadProfile == null ? dataContainer.getAncillaryLoadProfile() :
                loadProfile;

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
        ancillaryLoadProfile.resetSequential();

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
            ancillaryLoadProfile.setLoadSequential(meterState, t);

            //let all passive states calculate their next step
            for (InterdependentProblemPart<?, ?> part : allPassive) {
                part.calculateNextStep();
                passiveToActiveMap.put(part.getId(), part.getCommodityOutputStates());
            }

            //send new passive states to active nodes
            ocESC.doPassiveToActiveExchange(meterState, allActiveNeedsInput, passiveToActiveMap);
        }

        //mark that no more data will be added and the profile is finalized
        ancillaryLoadProfile.endSequential(this.maxOptimizationHorizon);


        Enum2DoubleMap<CostReturnType> costs = this.costFunction.calculateCosts(
                this.ignoreLoadProfileBefore,
                this.ignoreLoadProfileAfter,
                ancillaryLoadProfile);

        Enum2DoubleMap<EAObjectives> cervisia = new Enum2DoubleMap<>(EAObjectives.class);

        // add lukewarm cervisia (i.e. additional fixed costs...)
        for (InterdependentProblemPart<?, ?> problempart : allIPPs) {
            problempart.finalizeInterdependentCervisia();
            Enum2DoubleMap<EAObjectives> add = problempart.getFinalInterdependentSchedule().getLukewarmCervisia();
            cervisia.addAll(add);

            if (log) {
                this.eaLogger.logCervisia(problempart.getDeviceType(), add);
            }
        }

        for (int i = 0; i < this.eaObjectives.size(); i++) {
            EAObjectives objective = this.eaObjectives.get(i);

            switch (objective) {
                case MONEY:
                    solution.setObjective(i,
                            costs.get(CostReturnType.ELECTRICITY)
                                    + costs.get(CostReturnType.GAS)
                                    + cervisia.get(EAObjectives.MONEY));
                    break;
                case SELF_SUFFICIENCY_RATIO:
                    solution.setObjective(i, costs.get(CostReturnType.SELF_SUFFICIENCY_RATIO)
                            + cervisia.get(CostReturnType.SELF_SUFFICIENCY_RATIO));
                    break;
                case SELF_CONSUMPTION_RATIO:
                    solution.setObjective(i, costs.get(CostReturnType.SELF_CONSUMPTION_RATIO)
                            + cervisia.get(CostReturnType.SELF_CONSUMPTION_RATIO));
                    break;
            }
        }

        //free the used data for another evaluation
        this.freeDataCopy(dataContainer);
    }
}