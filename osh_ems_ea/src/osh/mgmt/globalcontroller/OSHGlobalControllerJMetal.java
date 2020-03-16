package osh.mgmt.globalcontroller;

import jmetal.metaheuristics.singleObjective.geneticAlgorithm.OSH_gGAMultiThread;
import org.uma.jmetal.solution.Solution;
import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.GAConfiguration;
import osh.core.OSHRandom;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.TemperaturePrediction;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.Fitness;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.IFitness;
import osh.mgmt.globalcontroller.jmetal.SolutionWithFitness;
import osh.mgmt.globalcontroller.jmetal.esc.EMProblemEvaluator;
import osh.mgmt.globalcontroller.jmetal.esc.JMetalEnergySolverGA;
import osh.mgmt.globalcontroller.jmetal.esc.SolutionDistributor;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IProvidesIdentity;
import osh.simulation.DatabaseLoggerThread;
import osh.utils.string.ParameterConstants;

import java.time.ZonedDateTime;
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
    private ZonedDateTime lastTimeSchedulingStarted;
    private final OSHRandom optimizationMainRandomGenerator;
    private long optimizationMainRandomSeed;
    private final GAParameters gaparameters;
    private final String logDir;
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
            this.upperOverlimitFactor =
                    Double.parseDouble(this.configurationParameters.getParameter(ParameterConstants.Optimization.upperOverlimitFactor));
        } catch (Exception e) {
            this.upperOverlimitFactor = 1.0;
            this.getGlobalLogger().logWarning("Can't get upperOverlimitFactor, using the default value: " + this.upperOverlimitFactor);
        }

        try {
            this.lowerOverlimitFactor =
                    Double.parseDouble(this.configurationParameters.getParameter(ParameterConstants.Optimization.lowerOverlimitFactor));
        } catch (Exception e) {
            this.lowerOverlimitFactor = 1.0;
            this.getGlobalLogger().logWarning("Can't get lowerOverlimitFactor, using the default value: " + this.lowerOverlimitFactor);
        }

        try {
            this.epsOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.epsObjective));
        } catch (Exception e) {
            this.epsOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get epsOptimizationObjective, using the default value: " + this.epsOptimizationObjective);
        }

        try {
            this.plsOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.plsObjective));
        } catch (Exception e) {
            this.plsOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get plsOptimizationObjective, using the default value: " + this.plsOptimizationObjective);
        }

        try {
            this.varOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.varObjective));
        } catch (Exception e) {
            this.varOptimizationObjective = 0;
            this.getGlobalLogger().logWarning("Can't get varOptimizationObjective, using the default value: " + this.varOptimizationObjective);
        }

        try {
            this.optimizationMainRandomSeed =
                    Long.parseLong(this.configurationParameters.getParameter(ParameterConstants.Optimization.optimizationRandomSeed));
        } catch (Exception e) {
            this.optimizationMainRandomSeed = 0xd1ce5bL;
            this.getGlobalLogger().logError("Can't get parameter optimizationMainRandomSeed, using the default value: " + this.optimizationMainRandomSeed);
        }
        this.optimizationMainRandomGenerator = new OSHRandom(this.optimizationMainRandomSeed);

        try {
            this.stepSize =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.stepSize));
        } catch (Exception e) {
            this.stepSize = 60;
            this.getGlobalLogger().logError("Can't get parameter stepSize, using the default value: " + this.stepSize);
        }

        try {
            this.hotWaterTankID =
                    UUID.fromString(this.configurationParameters.getParameter(ParameterConstants.Optimization.hotWaterTankUUID));
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

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
//		
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), EpsStateExchange.class, RegistryType.COM, RegistryType.OC);
//		this.getOSH().getDataBroker().registerDataReachThroughState(getUUID(), PlsStateExchange.class, RegistryType.COM, RegistryType.OC);

        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);

//		CostChecker.init(epsOptimizationObjective, plsOptimizationObjective, varOptimizationObjective, upperOverlimitFactor, lowerOverlimitFactor);

        this.lastTimeSchedulingStarted = this.getTimeDriver().getTimeAtStart().plusSeconds(60);
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
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        ZonedDateTime now = exchange.getTime();

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
    private void handleScheduling() {

        boolean reschedulingRequired = false;

        //check if something has been changed:
        for (InterdependentProblemPart<?, ?> problemPart : this.oshGlobalObserver.getProblemParts()) {
            if (problemPart.isToBeScheduled() && !problemPart.getTimestamp().isBefore(this.lastTimeSchedulingStarted)) {
                reschedulingRequired = true;
                break;
            }
        }

        if (reschedulingRequired) {
            this.lastTimeSchedulingStarted = this.getTimeDriver().getCurrentTime();
            this.startScheduling();
        }

    }

    /**
     * is triggered to
     *
     */
    public void startScheduling() {

        if (this.ocESC == null) {
            throw new RuntimeException("OC-EnergySimulationCore not set, optimisation impossible, crashing now");
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
        final long now = this.getTimeDriver().getCurrentEpochSecond();

        OSHRandom optimisationRunRandomGenerator = new OSHRandom(this.optimizationMainRandomGenerator.getNextLong());

        // it is a good idea to use a specific random Generator for the EA,
        // to make it comparable with other optimizers...
        JMetalEnergySolverGA solver = new JMetalEnergySolverGA(
                this.getGlobalLogger(),
                optimisationRunRandomGenerator,
                showSolverDebugMessages,
                this.gaparameters,
                now,
                this.stepSize,
                this.logDir);

        List<InterdependentProblemPart<?, ?>> problemPartsList = this.oshGlobalObserver.getProblemParts();
        InterdependentProblemPart<?, ?>[] problemParts = new InterdependentProblemPart<?, ?>[problemPartsList.size()];
        problemParts = problemPartsList.toArray(problemParts);

        Solution<?> solution;
        SolutionWithFitness resultWithAll;

        if (!this.oshGlobalObserver.getAndResetProblempartChangedFlag()) {
            return; //nothing new, return
        }

        // debug print
        this.getGlobalLogger().logDebug("=== scheduling... ===");

        long ignoreLoadProfileAfter = now;
        long maxHorizon = now;

        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            if (problem instanceof ControllableIPP<?, ?>) {
                maxHorizon = Math.max(problem.getOptimizationHorizon(), maxHorizon);
            }
        }

        int counter = 0;
        for (InterdependentProblemPart<?, ?> problem : problemParts) {
            problem.recalculateEncoding(now, maxHorizon);
            problem.setId(counter);
            counter++;
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
                    tempPriceSignals,
                    tempPowerLimitSignals,
                    now,
                    fitnessFunction);
            solution = resultWithAll.getSolution();

            SolutionDistributor distributor = new SolutionDistributor();
            distributor.gatherVariableInformation(problemParts);

            EMProblemEvaluator problem = new EMProblemEvaluator(
                    problemParts,
                    this.ocESC,
                    distributor,
                    this.priceSignals,
                    this.powerLimitSignals,
                    now,
                    ignoreLoadProfileAfter,
                    fitnessFunction,
                    this.stepSize);

            boolean extensiveLogging =
                    (hasGUI || isReal) && !distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables();
            AncillaryCommodityLoadProfile ancillaryMeter = new AncillaryCommodityLoadProfile();

            problem.evaluateFinalTime(solution, (this.logGa | extensiveLogging), ancillaryMeter);

            distributor.distributeSolution(solution, problemParts);


            if (extensiveLogging) {

                TreeMap<Long, Double> predictedTankTemp = new TreeMap<>();
                TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
                TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();
                List<Schedule> schedules = new ArrayList<>();

                for (InterdependentProblemPart<?, ?> part : problemParts) {
                    schedules.add(part.getFinalInterdependentSchedule());

                    //extract prediction about tank temperatures of the hot-water tank
                    if (part.getUUID().equals(this.hotWaterTankID)) {
                        @SuppressWarnings("unchecked")
                        EAPredictionCommandExchange<TemperaturePrediction> prediction = (EAPredictionCommandExchange<TemperaturePrediction>) part.transformToFinalInterdependentPrediction(
                                this.getUUID(),
                                part.getUUID(),
                                this.getTimeDriver().getCurrentTime());

                        predictedTankTemp = prediction.getPrediction().getTemperatureStates();
                    }

                    //extract information about hot-water demand and supply
                    if (part.getAllOutputCommodities().contains(Commodity.DOMESTICHOTWATERPOWER)
                            || part.getAllOutputCommodities().contains(Commodity.HEATINGHOTWATERPOWER)) {
                        SparseLoadProfile loadProfile = part.getLoadProfile();

                        for (long t = now; t < maxHorizon + this.stepSize; t += this.stepSize) {
                            int domLoad = loadProfile.getLoadAt(Commodity.DOMESTICHOTWATERPOWER, t);
                            int heatLoad = loadProfile.getLoadAt(Commodity.HEATINGHOTWATERPOWER, t);

                            predictedHotWaterDemand.putIfAbsent(t, 0.0);
                            predictedHotWaterSupply.putIfAbsent(t, 0.0);

                            if (domLoad > 0) {
                                predictedHotWaterDemand.compute(t, (k, v) -> v == null ? domLoad : v + domLoad);
                            } else if (domLoad < 0) {
                                predictedHotWaterSupply.compute(t, (k, v) -> v == null ? domLoad : v + domLoad);
                            }

                            if (heatLoad > 0) {
                                predictedHotWaterDemand.compute(t, (k, v) -> v == null ? heatLoad : v + heatLoad);
                            } else if (heatLoad < 0) {
                                predictedHotWaterSupply.compute(t, (k, v) -> v == null ? heatLoad : v + heatLoad);
                            }
                        }
                    }
                }

                this.getOCRegistry().publish(
                        GUIHotWaterPredictionStateExchange.class,
                        this,
                        new GUIHotWaterPredictionStateExchange(this.getUUID(),
                                this.getTimeDriver().getCurrentTime(), predictedTankTemp, predictedHotWaterDemand, predictedHotWaterSupply));

                this.getOCRegistry().publish(
                        GUIAncillaryMeterStateExchange.class,
                        this,
                        new GUIAncillaryMeterStateExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(), ancillaryMeter));

                //sending schedules last so the wait command has all the other things (waterPred, Ancillarymeter) first
                // Send current Schedule to GUI (via Registry to Com)
                this.getOCRegistry().publish(
                        GUIScheduleStateExchange.class,
                        this,
                        new GUIScheduleStateExchange(this.getUUID(), this.getTimeDriver()
                                .getCurrentTime(), schedules, this.stepSize));

            }
        } catch (Exception e) {
            e.printStackTrace();
            this.getGlobalLogger().logError(e);
            return;
        }


        for (InterdependentProblemPart<?, ?> part : problemParts) {
            LocalController lc = this.getLocalController(part.getUUID());

            if (lc != null) {
                this.getOCRegistry().publish(
                        EASolutionCommandExchange.class,
                        part.transformToFinalInterdependentPhenotype(
                                this.getUUID(),
                                part.getUUID(),
                                this.getTimeDriver().getCurrentTime()));
            }
//			this sends a prediction of the waterTemperatures to the waterTankObserver, so the waterTank can trigger a reschedule
//			when the actual temperatures are too different to the prediction
            if (part.transformToFinalInterdependentPrediction() != null) {
                this.getOCRegistry().publish(
                        EAPredictionCommandExchange.class,
                        part.transformToFinalInterdependentPrediction(
                                this.getUUID(),
                                part.getUUID(),
                                this.getTimeDriver().getCurrentTime()));
            }
        }

        this.getGlobalLogger().logDebug("===    EA done    ===");
    }

    @Override
    public UUID getUUID() {
        return this.getGlobalObserver().getAssignedOCUnit().getUnitID();
    }

    public OSHGlobalObserver getOshGlobalObserver() {
        return this.oshGlobalObserver;
    }

}

