package osh.mgmt.globalcontroller;

import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.GUIScheduleDebugExchange;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.Fitness;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.esc.EnergyManagementProblem;
import osh.mgmt.globalcontroller.jmetal.esc.JMetalEnergySolverGA;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IProvidesIdentity;
import osh.simulation.DatabaseLoggerThread;

import java.util.*;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth, Sebastian Kramer
 */
public class OSHGlobalControllerJMetal
        extends GlobalController
        implements IDataRegistryListener, IProvidesIdentity {

    UUID hotWaterTankID;
    private OSHGlobalObserver oshGlobalObserver;
    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    private boolean newEpsPlsReceived;
    private int epsOptimizationObjective;
    private int plsOptimizationObjective;
    private int varOptimizationObjective;
    private double upperOverlimitFactor;
    private double lowerOverlimitFactor;
    private long lastTimeSchedulingStarted;
    private OSHRandomGenerator optimizationMainRandomGenerator;
    private long optimizationMainRandomSeed;
    private GAParameters gaparameters;
    private String logDir;
    private int stepSize;
    private Boolean logGa;


    /**
     * CONSTRUCTOR
     *
     * @throws Exception
     */
    public OSHGlobalControllerJMetal(
            IOSHOC osh,
            OSHParameterCollection configurationParameters,
            GAConfiguration gaConfiguration, OCEnergySimulationCore ocESC) throws Exception {
        super(osh, configurationParameters, gaConfiguration, ocESC);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
        this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);
        try {
            this.gaparameters = new GAParameters(this.gaConfiguration);
        } catch (Exception ex) {
            this.getGlobalLogger().logError("Can't parse GAParameters, will shut down now!");
            throw ex;
        }

        try {
            this.upperOverlimitFactor = Double.parseDouble(this.configurationParameters.getParameter("upperOverlimitFactor"));
        } catch (Exception e) {
            this.upperOverlimitFactor = 1.0;
            this.getGlobalLogger().logWarning("Can't get upperOverlimitFactor, using the default value: " + this.upperOverlimitFactor);
        }

        try {
            this.lowerOverlimitFactor = Double.parseDouble(this.configurationParameters.getParameter("lowerOverlimitFactor"));
        } catch (Exception e) {
            this.lowerOverlimitFactor = 1.0;
            this.getGlobalLogger().logWarning("Can't get lowerOverlimitFactor, using the default value: " + this.lowerOverlimitFactor);
        }

        try {
            this.epsOptimizationObjective = Integer.parseInt(this.configurationParameters.getParameter("epsoptimizationobjective"));
        } catch (Exception e) {
            this.epsOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get epsOptimizationObjective, using the default value: " + this.epsOptimizationObjective);
        }

        try {
            this.plsOptimizationObjective = Integer.parseInt(this.configurationParameters.getParameter("plsoptimizationobjective"));
        } catch (Exception e) {
            this.plsOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get plsOptimizationObjective, using the default value: " + this.plsOptimizationObjective);
        }

        try {
            this.varOptimizationObjective = Integer.parseInt(this.configurationParameters.getParameter("varoptimizationobjective"));
        } catch (Exception e) {
            this.varOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get varOptimizationObjective, using the default value: " + this.varOptimizationObjective);
        }

        try {
            this.optimizationMainRandomSeed = Long.parseLong(this.configurationParameters.getParameter("optimizationMainRandomSeed"));
        } catch (Exception e) {
            this.optimizationMainRandomSeed = 0xd1ce5bL;
            this.getGlobalLogger().logError("Can't get parameter optimizationMainRandomSeed, using the default value: " + this.optimizationMainRandomSeed);
        }
        this.optimizationMainRandomGenerator = new OSHRandomGenerator(new Random(this.optimizationMainRandomSeed));

        try {
            this.stepSize = Integer.parseInt(this.configurationParameters.getParameter("stepSize"));
        } catch (Exception e) {
            this.stepSize = 60;
            this.getGlobalLogger().logError("Can't get parameter stepSize, using the default value: " + this.stepSize);
        }

        try {
            this.hotWaterTankID = UUID.fromString(this.configurationParameters.getParameter("hotWaterTankUUID"));
        } catch (Exception e) {
            this.hotWaterTankID = UUID.fromString("00000000-0000-4857-4853-000000000000");
            this.getGlobalLogger().logError("Can't get parameter hotWaterTankUUID, using the default value: " + this.hotWaterTankID);
        }

        this.logDir = this.getOSH().getOSHStatus().getLogDir();

        this.getGlobalLogger().logDebug("Optimization StepSize = " + this.stepSize);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // safety first...
        if (this.getGlobalObserver() instanceof OSHGlobalObserver) {
            this.oshGlobalObserver = (OSHGlobalObserver) this.getGlobalObserver();
        } else {
            throw new OSHException("this global controller only works with global observers of type " + OSHGlobalObserver.class.getName());
        }

        this.getOSH().getTimer().registerComponent(this, 1);
//		
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), EpsStateExchange.class, RegistryType.COM, RegistryType.OC);
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), PlsStateExchange.class, RegistryType.COM, RegistryType.OC);

        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);

//		CostChecker.init(epsOptimizationObjective, plsOptimizationObjective, varOptimizationObjective, upperOverlimitFactor, lowerOverlimitFactor);

        this.lastTimeSchedulingStarted = this.getTimer().getUnixTimeAtStart() + 60;
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        // shutting down threadpool
        OSH_gGAMultiThread.shutdown();
//		CostChecker.shutDown();
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EpsStateExchange) {
            this.newEpsPlsReceived = true;
            this.priceSignals = ((EpsStateExchange) exchange).getPriceSignals();
        } else if (exchange instanceof PlsStateExchange) {
            this.newEpsPlsReceived = true;
            this.powerLimitSignals = ((PlsStateExchange) exchange).getPowerLimitSignals();
        } else {
            this.getGlobalLogger().logError("ERROR in " + this.getClass().getCanonicalName() + ": UNKNOWN " +
                    "EventExchange from UUID " + exchange.getSender());
        }
    }


    @Override
    public void onNextTimePeriod() throws OSHException {

        long now = this.getTimer().getUnixTime();

        // check whether rescheduling is required and if so do rescheduling
        this.handleScheduling();

        // save current EPS and PLS to registry for logger
        {
            EpsPlsStateExchange epse = new EpsPlsStateExchange(
                    this.getUUID(),
                    now,
                    this.priceSignals,
                    this.powerLimitSignals,
                    this.epsOptimizationObjective,
                    this.plsOptimizationObjective,
                    this.varOptimizationObjective,
                    this.upperOverlimitFactor,
                    this.lowerOverlimitFactor,
                    this.newEpsPlsReceived);

            this.newEpsPlsReceived = false;

            this.getOCRegistry().publish(
                    EpsPlsStateExchange.class,
                    this,
                    epse);
        }

    }

    /**
     * decide if a (re-)scheduling is necessary
     *
     * @throws OSHException
     */
    private void handleScheduling() throws OSHException {

        boolean reschedulingRequired = false;

        //check if something has been changed:
        for (InterdependentProblemPart<?, ?> problemPart : this.oshGlobalObserver.getProblemParts()) {
            if (problemPart.isToBeScheduled() && problemPart.getTimestamp() >= this.lastTimeSchedulingStarted) {
                reschedulingRequired = true;
                break;
            }
        }

        if (reschedulingRequired) {
            this.lastTimeSchedulingStarted = this.getTimer().getUnixTime();
            this.startScheduling();
        }

    }

    /**
     * is triggered to
     *
     * @throws OSHException
     */
    public void startScheduling() throws OSHException {

        if (this.ocESC == null) {
            throw new OSHException("OC-EnergySimulationCore not set, optimisation impossible, crashing now");
        }

        //retrieve information of ga should log to database
        if (this.logGa == null) {
            this.logGa = DatabaseLoggerThread.isLogGA();
            if (this.logGa) {
                OSH_gGAMultiThread.initLogging();
            }
        }

        EnumMap<AncillaryCommodity, PriceSignal> tempPriceSignals = new EnumMap<>(AncillaryCommodity.class);
        EnumMap<AncillaryCommodity, PowerLimitSignal> tempPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

        //TODO: Check if necessary to synchronize full object (this)
        //TODO: Check why keySet and not entrySet

        // Cloning necessary, because of possible price signal changes during optimization
        synchronized (this.priceSignals) {
            for (Map.Entry<AncillaryCommodity, PriceSignal> entry : this.priceSignals.entrySet()) {
                tempPriceSignals.put(entry.getKey(), entry.getValue().clone());
            }
        }
        if (tempPriceSignals.isEmpty()) {
            this.getGlobalLogger().logError("No valid price signal available. Cancel scheduling!");
            return;
        }

        synchronized (this.powerLimitSignals) {
            for (Map.Entry<AncillaryCommodity, PowerLimitSignal> entry : this.powerLimitSignals.entrySet()) {
                tempPowerLimitSignals.put(entry.getKey(), entry.getValue().clone());
            }
        }
        if (tempPowerLimitSignals.isEmpty()) {
            this.getGlobalLogger().logError("No valid power limit signal available. Cancel scheduling!");
            return;
        }

//		boolean showSolverDebugMessages = getControllerBoxStatus().getShowSolverDebugMessages();
        boolean showSolverDebugMessages = true;

        OSHRandomGenerator optimisationRunRandomGenerator = new OSHRandomGenerator(new Random(this.optimizationMainRandomGenerator.getNextLong()));

        // it is a good idea to use a specific random Generator for the EA,
        // to make it comparable with other optimizers...
        JMetalEnergySolverGA solver = new JMetalEnergySolverGA(
                this.getGlobalLogger(),
                optimisationRunRandomGenerator,
                showSolverDebugMessages,
                this.gaparameters,
                this.getTimer().getUnixTime(),
                this.stepSize,
                this.logDir);

        List<InterdependentProblemPart<?, ?>> problemParts = this.oshGlobalObserver.getProblemParts();
        List<BitSet> solutions;
        SolutionWithFitness resultWithAll;

        if (!this.oshGlobalObserver.getAndResetProblempartChangedFlag()) {
            return; //nothing new, return
        }

        // debug print
        this.getGlobalLogger().logDebug("=== scheduling... ===");
        long now = this.getTimer().getUnixTime();

        int[][] bitPositions = new int[problemParts.size()][2];
        int bitPosStart = 0;
        int bitPosEnd;
        int counter = 0;
        long ignoreLoadProfileAfter = now;
        int usedBits = 0;

        long maxHorizon = now;

        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            if (problem instanceof ControllableIPP<?, ?>) {
                maxHorizon = Math.max(problem.getOptimizationHorizon(), maxHorizon);
            }
        }

        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            problem.recalculateEncoding(now, maxHorizon);
            problem.setId(counter);
            if (problem.getBitCount() > 0) {
                bitPosEnd = bitPosStart + problem.getBitCount();
            } else {
                bitPosEnd = bitPosStart;
            }
            bitPositions[counter] = new int[]{bitPosStart, bitPosEnd};
            counter++;
            bitPosStart += problem.getBitCount();
            usedBits += problem.getBitCount();
        }
        ignoreLoadProfileAfter = Math.max(ignoreLoadProfileAfter, maxHorizon);

        boolean hasGUI = this.getControllerBoxStatus().hasGUI();
        boolean isReal = !this.getControllerBoxStatus().isSimulation();


        try {
            IFitness fitnessFunction = new Fitness(
                    this.getGlobalLogger(),
                    this.epsOptimizationObjective,
                    this.plsOptimizationObjective,
                    this.varOptimizationObjective,
                    this.upperOverlimitFactor,
                    this.lowerOverlimitFactor);

            resultWithAll = solver.getSolution(
                    problemParts,
                    this.ocESC,
                    bitPositions,
                    tempPriceSignals,
                    tempPowerLimitSignals,
                    this.getTimer().getUnixTime(),
                    fitnessFunction);
            solutions = resultWithAll.getBitSet();


            if ((hasGUI || isReal) && usedBits != 0) {
                TreeMap<Long, Double> predictedTankTemp = new TreeMap<>();
                TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
                TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();
                List<Schedule> schedules = new ArrayList<>();
                AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();

                EnergyManagementProblem debugProblem = new EnergyManagementProblem(
                        problemParts, this.ocESC, bitPositions, this.priceSignals,
                        this.powerLimitSignals, now, ignoreLoadProfileAfter,
                        optimisationRunRandomGenerator, this.getGlobalLogger(), fitnessFunction, this.stepSize);

                debugProblem.evaluateWithDebuggingInformation(
                        resultWithAll.getFullSet(),
                        ancillaryMeter,
                        predictedTankTemp,
                        predictedHotWaterDemand,
                        predictedHotWaterSupply,
                        schedules,
                        true,
                        this.hotWaterTankID);

                //better be sure
                debugProblem.finalizeGrids();

                this.getOCRegistry().publish(
                        GUIHotWaterPredictionStateExchange.class,
                        this,
                        new GUIHotWaterPredictionStateExchange(this.getUUID(),
                                this.getTimer().getUnixTime(), predictedTankTemp, predictedHotWaterDemand, predictedHotWaterSupply));

                this.getOCRegistry().publish(
                        GUIAncillaryMeterStateExchange.class,
                        this,
                        new GUIAncillaryMeterStateExchange(this.getUUID(), this.getTimer().getUnixTime(), ancillaryMeter));

                //sending schedules last so the wait command has all the other things (waterPred, Ancillarymeter) first
                // Send current Schedule to GUI (via Registry to Com)
                this.getOCRegistry().publish(
                        GUIScheduleStateExchange.class,
                        this,
                        new GUIScheduleStateExchange(this.getUUID(), this.getTimer()
                                .getUnixTime(), schedules, this.stepSize));

            }
        } catch (Exception e) {
            e.printStackTrace();
            this.getGlobalLogger().logError(e);
            return;
        }


        int min = Math.min(solutions.size(), problemParts.size());
        if (solutions.size() != problemParts.size()) {
            this.getGlobalLogger().logDebug("jmetal: problem list and solution list don't have the same size");
        }

        GUIScheduleDebugExchange debug = new GUIScheduleDebugExchange(this.getUUID(), this.getTimer().getUnixTime());

        for (int i = 0; i < min; i++) {
            InterdependentProblemPart<?, ?> part = problemParts.get(i);
            LocalController lc = this.getLocalController(part.getUUID());
            BitSet bits = solutions.get(i);

            if (lc != null) {
                this.getOCRegistry().publish(
                        EASolutionCommandExchange.class,
                        part.transformToFinalInterdependentPhenotype(
                                null,
                                part.getUUID(),
                                this.getTimer().getUnixTime(),
                                bits));
            } else if (/* lc == null && */ part.getBitCount() > 0) {
                throw new NullPointerException("got a local part with used bits but without controller! (UUID: " + part.getUUID() + ")");
            }
//			this sends a prediction of the waterTemperatures to the waterTankObserver, so the waterTank can trigger a reschedule
//			when the actual temperatures are too different to the prediction
            if (part.transformToFinalInterdependentPrediction(bits) != null) {
                this.getOCRegistry().publish(
                        EAPredictionCommandExchange.class,
                        part.transformToFinalInterdependentPrediction(
                                null,
                                part.getUUID(),
                                this.getTimer().getUnixTime(),
                                bits));
            }

            if (hasGUI) {
                StringBuilder debugStr = new StringBuilder();
                debugStr.append(this.getTimer().getUnixTime()).append(";");
                debugStr.append(part.getSender()).append(";");
                debugStr.append(part.problemToString()).append(";");
                if (part instanceof ControllableIPP<?, ?>) {
                    debugStr.append(((ControllableIPP<?, ?>) part)
                            .solutionToString(bits));
                }
                debug.addString(part.getSender(), debugStr.toString());
            }
        }

        if (hasGUI && usedBits != 0)
            this.getOCRegistry().publish(GUIScheduleDebugExchange.class, debug);

        this.getGlobalLogger().logDebug("===    EA done    ===");

        //lasttimeScheduled = getTimer().getUnixTime();
    }

    @Override
    public UUID getUUID() {
        return this.getGlobalObserver().getAssignedOCUnit().getUnitID();
    }

    public OSHGlobalObserver getOshGlobalObserver() {
        return this.oshGlobalObserver;
    }

}

