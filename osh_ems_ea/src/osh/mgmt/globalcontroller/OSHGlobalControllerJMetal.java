package osh.mgmt.globalcontroller;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.CostConfiguration;
import osh.configuration.oc.EAConfiguration;
import osh.configuration.oc.VariableEncoding;
import osh.configuration.system.ConfigurationParameter;
import osh.core.OSHRandom;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.GlobalController;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logging.general.EALogObject;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.details.energy.CostConfigurationStateExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.esc.OptimizationEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.builder.AlgorithmExecutor;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;
import osh.mgmt.globalcontroller.jmetal.esc.EnergySolver;
import osh.mgmt.globalcontroller.jmetal.esc.SolutionDistributor;
import osh.mgmt.globalobserver.OSHGlobalObserver;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IProvidesIdentity;
import osh.simulation.database.DatabaseLoggerThread;
import osh.utils.costs.OptimizationCostFunction;
import osh.utils.string.ParameterConstants;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private ZonedDateTime lastTimeSchedulingStarted;
    private final OSHRandom optimizationMainRandomGenerator;
    private final AlgorithmExecutor algorithmExecutor;
    private final String logDir;
    private int stepSize;
    private Boolean logGa;
    private final EnergySolver energySolver;


    /**
     * CONSTRUCTOR
     *
     */
    public OSHGlobalControllerJMetal(
            IOSHOC osh,
            OSHParameterCollection configurationParameters,
            EAConfiguration eaConfiguration,
            CostConfiguration costConfiguration,
            OptimizationEnergySimulationCore ocESC) {
        super(osh, configurationParameters, eaConfiguration, costConfiguration, ocESC);

        this.priceSignals = new EnumMap<>(AncillaryCommodity.class);
        this.powerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

        //set to true if single threaded execution is desired
        if (false) {
            ConfigurationParameter singleT = new ConfigurationParameter();
            singleT.setParameterName(ParameterConstants.EA_ALGORITHM.singleThreaded);
            singleT.setParameterValue("" + true);
            singleT.setParameterType(Boolean.class.getName());
            this.eaConfiguration.getAlgorithms().forEach(c ->c.getAlgorithmParameters().add(singleT));
        }

        long optimizationMainRandomSeed;
        try {
            optimizationMainRandomSeed =
                    Long.parseLong(this.configurationParameters.getParameter(ParameterConstants.Optimization.optimizationRandomSeed));
        } catch (Exception e) {
            optimizationMainRandomSeed = 0xd1ce5bL;
            this.getGlobalLogger().logError("Can't get parameter optimizationMainRandomSeed, using the default value: " + optimizationMainRandomSeed);
        }
        this.optimizationMainRandomGenerator = new OSHRandom(optimizationMainRandomSeed);

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
        this.algorithmExecutor = new AlgorithmExecutor(this.eaConfiguration, this.getGlobalLogger());
        this.energySolver = new EnergySolver(this.getGlobalLogger(), this.eaConfiguration, this.stepSize, this.logDir);
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

        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);

        this.getOCRegistry().publish(CostConfigurationStateExchange.class,
                new CostConfigurationStateExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(),
                        new CostConfiguration(this.costConfiguration)));

        this.lastTimeSchedulingStarted = this.getTimeDriver().getTimeAtStart().plusSeconds(60);
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();

        EALogObject logObject = this.algorithmExecutor.getEaLogger().shutdown();

        if (logObject != null) {
            logObject.setSender(this.getUUID());
            this.getOCRegistry().publish(EALogObject.class, logObject);
        }
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
    }

    /**
     * decide if a (re-)scheduling is necessary
     *
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

        if (this.optimizationESC == null) {
            throw new RuntimeException("OC-EnergySimulationCore not set, optimisation impossible, crashing now");
        }

        //retrieve information of ga should log to database
        if (this.logGa == null) {
            this.logGa = DatabaseLoggerThread.isLogEA();
        }

        EnumMap<AncillaryCommodity, PriceSignal> tempPriceSignals = new EnumMap<>(AncillaryCommodity.class);
        EnumMap<AncillaryCommodity, PowerLimitSignal> tempPowerLimitSignals = new EnumMap<>(AncillaryCommodity.class);

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

        final long now = this.getTimeDriver().getCurrentEpochSecond();

        List<InterdependentProblemPart<?, ?>> problemPartsList = this.oshGlobalObserver.getProblemParts();
        InterdependentProblemPart<?, ?>[] problemParts = new InterdependentProblemPart<?, ?>[problemPartsList.size()];
        problemParts = problemPartsList.toArray(problemParts);

        if (!this.oshGlobalObserver.getAndResetProblempartChangedFlag()) {
            return; //nothing new, return
        }

        // debug print
        this.getGlobalLogger().logDebug("=== scheduling... ===");

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

        boolean hasGUI = this.getControllerBoxStatus().hasGUI();
        boolean isReal = !this.getControllerBoxStatus().isSimulation();


        try {
            OptimizationCostFunction costFunction = new OptimizationCostFunction(
                    this.costConfiguration,
                    tempPriceSignals,
                    tempPowerLimitSignals,
                    now);

            SolutionDistributor distributor = new SolutionDistributor();
            distributor.gatherVariableInformation(problemParts);

            boolean extensiveLogging =
                    (hasGUI || isReal) && !distributor.getVariableInformation(VariableEncoding.BINARY).needsNoVariables();

            EAScheduleResult result = this.energySolver.getSolution(
                    problemParts,
                    this.optimizationESC,
                    now,
                    costFunction,
                    new OSHRandom(this.optimizationMainRandomGenerator.getNextLong()),
                    this.algorithmExecutor,
                    extensiveLogging);

            if (extensiveLogging) {
                this.getOCRegistry().publish(
                        GUIHotWaterPredictionStateExchange.class,
                        this,
                        new GUIHotWaterPredictionStateExchange(this.getUUID(),
                                this.getTimeDriver().getCurrentTime(), result.getPredictedHotWaterTankTemperature(),
                                result.getPredictedHotWaterDemand(), result.getPredictedHotWaterSupply()));

                this.getOCRegistry().publish(
                        GUIAncillaryMeterStateExchange.class,
                        this,
                        new GUIAncillaryMeterStateExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(),
                                result.getAncillaryMeter()));

                //sending schedules last so the wait command has all the other things (waterPred, Ancillarymeter) first
                // Send current Schedule to GUI (via Registry to Com)
                this.getOCRegistry().publish(
                        GUIScheduleStateExchange.class,
                        this,
                        new GUIScheduleStateExchange(this.getUUID(), this.getTimeDriver()
                                .getCurrentTime(), result.getSchedules(), this.stepSize));

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

