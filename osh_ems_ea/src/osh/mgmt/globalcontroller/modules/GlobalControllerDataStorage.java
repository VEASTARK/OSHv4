package osh.mgmt.globalcontroller.modules;

import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.CostConfiguration;
import osh.configuration.oc.EAConfiguration;
import osh.core.EARandomDistributor;
import osh.core.interfaces.IOSHStatus;
import osh.core.logging.IGlobalLogger;
import osh.core.oc.GlobalController;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OptimizationEnergySimulationCore;
import osh.mgmt.globalcontroller.modules.scheduling.DetailedOptimisationResults;
import osh.registry.Registry.OCRegistry;
import osh.utils.string.ParameterConstants.Optimization;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents a global data storage container for use in {@link GlobalControllerModule}.
 *
 * @author Sebastian Kramer
 */
public class GlobalControllerDataStorage {

    //management
    private final EnumMap<GlobalControllerEventEnum, List<GlobalControllerModule>> eventSubscriber
            = new EnumMap<>(GlobalControllerEventEnum.class);

    //general
    private final UUID uuid;
    private final IGlobalLogger globalLogger;
    private final EAConfiguration eaConfiguration;
    private final OptimizationEnergySimulationCore optimizationESC;
    private final IOSHStatus status;
    private final OCRegistry ocRegistry;

    //use carefully here
    private final GlobalController globalController;
    private final GlobalObserver globalObserver;

    private List<InterdependentProblemPart<?, ?>> problemParts;

    //time
    private ZonedDateTime now;

    //Module Storage
    private final Map<Class<? extends GlobalControllerModule>, GlobalControllerModule> moduleMap = new HashMap<>();

    //ExecuteSchedulingData
    private final CostConfiguration costConfiguration;

    private final EARandomDistributor eaRandomDistributor;
    private int stepSize;
    private UUID hotWaterTankID;

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;

    //HandleSchedulingData
    private Duration delayBetweenScheduling;
    private Duration delayAtStart;
    private ZonedDateTime lastTimeSchedulingStarted;
    private DetailedOptimisationResults lastOptimisationResults;

    private final OSHParameterCollection configurationParameters;

    /**
     * Constructs this data storage with the given initial values.
     *
     * @param uuid the unique identifier of the global controller
     * @param now the current time
     * @param status the status of the {@link osh.OSH}
     * @param ocRegistry the oc-registry
     * @param globalController the global controller
     * @param globalObserver the global observer
     * @param configurationParameters the configuration parameters
     * @param eaConfiguration the ga parameters
     * @param globalLogger the global logger
     * @param optimizationESC the oc-ESC
     */
    public GlobalControllerDataStorage(
            UUID uuid,
            ZonedDateTime now,
            IOSHStatus status,
            OCRegistry ocRegistry,
            GlobalController globalController,
            GlobalObserver globalObserver,
            OSHParameterCollection configurationParameters,
            EAConfiguration eaConfiguration,
            IGlobalLogger globalLogger,
            OptimizationEnergySimulationCore optimizationESC,
            CostConfiguration costConfiguration,
            EARandomDistributor eaRandomDistributor) {

        this.uuid = uuid;
        this.now = now;
        this.status = status;
        this.ocRegistry = ocRegistry;
        this.globalController = globalController;
        this.globalObserver = globalObserver;
        this.configurationParameters = configurationParameters;
        this.eaConfiguration = eaConfiguration;
        this.globalLogger = globalLogger;
        this.optimizationESC = optimizationESC;
        this.costConfiguration = costConfiguration;
        this.eaRandomDistributor = eaRandomDistributor;

        try {
            this.stepSize =
                    Integer.parseInt(this.configurationParameters.getParameter(Optimization.stepSize));
        } catch (Exception e) {
            this.stepSize = 60;
            this.globalLogger.logError("Can't get parameter stepSize, using the default value: " + this.stepSize);
        }

        try {
            this.delayAtStart = Duration.ofSeconds(
                    Integer.parseInt(this.configurationParameters.getParameter(Optimization.delayAtStart)));
        } catch (Exception e) {
            this.delayAtStart = Duration.ofMinutes(1);
            this.globalLogger.logError("Can't get parameter delayAtStart, using the default value: " + this.delayAtStart.toString());
        }

        try {
            this.delayBetweenScheduling = Duration.ofSeconds(
                    Integer.parseInt(this.configurationParameters.getParameter(Optimization.delayBetweenScheduling)));
        } catch (Exception e) {
            this.delayBetweenScheduling = Duration.ofMinutes(1);
            this.globalLogger.logError("Can't get parameter delayBetweenScheduling, using the default value: " + this.delayBetweenScheduling.toString());
        }

        try {
            this.hotWaterTankID =
                    UUID.fromString(this.configurationParameters.getParameter(Optimization.hotWaterTankUUID));
        } catch (Exception e) {
            this.hotWaterTankID = UUID.fromString("00000000-0000-4857-4853-000000000000");
            this.globalLogger.logError("Can't get parameter hotWaterTankUUID, using the default value: " + this.hotWaterTankID);
        }

        this.globalLogger.logDebug("Optimization StepSize = " + this.stepSize);
    }

    public void subscribe(GlobalControllerEventEnum event, GlobalControllerModule module) {
        if (!this.eventSubscriber.containsKey(event)) {
            this.eventSubscriber.put(event, new ArrayList<>());
        }
        this.eventSubscriber.get(event).add(module);
    }

    public void notify(GlobalControllerEventEnum event) {
        if (this.eventSubscriber.containsKey(event)) {
            for (GlobalControllerModule module : this.eventSubscriber.get(event)) {
                module.notifyForEvent(event);
            }
        }
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public IGlobalLogger getGlobalLogger() {
        return this.globalLogger;
    }

    public EAConfiguration getEaConfiguration() {
        return this.eaConfiguration;
    }

    public OptimizationEnergySimulationCore getOptimizationESC() {
        return this.optimizationESC;
    }

    public IOSHStatus getStatus() {
        return this.status;
    }

    public ZonedDateTime getNow() {
        return this.now;
    }

    public void setNow(ZonedDateTime now) {
        this.now = now;
    }

    public OCRegistry getOCRegistry() {
        return this.ocRegistry;
    }

    public LocalController getLocalController(UUID deviceId) {
        return this.globalController.getLocalController(deviceId);
    }

    public GlobalObserver getGlobalObserver() {
        return this.globalObserver;
    }

    public List<InterdependentProblemPart<?, ?>> getProblemParts() {
        return this.problemParts;
    }

    public void setProblemParts(List<InterdependentProblemPart<?, ?>> problemParts) {
        this.problemParts = problemParts;
    }

    @SuppressWarnings("unchecked")
    public <M extends GlobalControllerModule> M getControllerModule(Class<M> moduleClass) {
        return (M) this.moduleMap.get(moduleClass);
    }

    public void registerControllerModule(Class<? extends GlobalControllerModule> moduleClass,
                                         GlobalControllerModule module) {
        this.moduleMap.put(moduleClass, module);
    }

    public CostConfiguration getCostConfiguration() {
        return this.costConfiguration;
    }

    public EARandomDistributor getEARandomDistributor() {
        return this.eaRandomDistributor;
    }

    public int getStepSize() {
        return this.stepSize;
    }

    public UUID getHotWaterTankID() {
        return this.hotWaterTankID;
    }

    public Duration getDelayBetweenScheduling() {
        return this.delayBetweenScheduling;
    }

    public Duration getDelayAtStart() {
        return this.delayAtStart;
    }

    public ZonedDateTime getLastTimeSchedulingStarted() {
        return this.lastTimeSchedulingStarted;
    }

    public void setLastTimeSchedulingStarted(ZonedDateTime lastTimeSchedulingStarted) {
        this.lastTimeSchedulingStarted = lastTimeSchedulingStarted;
    }

    public DetailedOptimisationResults getLastOptimisationResults() {
        return this.lastOptimisationResults;
    }

    public void setLastOptimisationResults(final DetailedOptimisationResults lastOptimisationResults) {
        this.lastOptimisationResults = lastOptimisationResults;
    }

    public EnumMap<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

    public void setPriceSignals(EnumMap<AncillaryCommodity, PriceSignal> priceSignals) {
        this.priceSignals = priceSignals;
    }

    public EnumMap<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
        return this.powerLimitSignals;
    }

    public void setPowerLimitSignals(
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        this.powerLimitSignals = powerLimitSignals;
    }

    public OSHParameterCollection getConfigurationParameters() {
        return this.configurationParameters;
    }
}
