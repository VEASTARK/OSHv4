package osh.mgmt.globalcontroller.modules;

import osh.configuration.OSHParameterCollection;
import osh.core.OSHRandom;
import osh.core.interfaces.IOSHStatus;
import osh.core.logging.IGlobalLogger;
import osh.core.oc.GlobalController;
import osh.core.oc.GlobalObserver;
import osh.core.oc.LocalController;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.esc.OCEnergySimulationCore;
import osh.mgmt.globalcontroller.jmetal.GAParameters;
import osh.mgmt.globalcontroller.jmetal.logging.EALogger;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.mgmt.globalcontroller.modules.scheduling.DetailedOptimisationResults;
import osh.registry.Registry.OCRegistry;
import osh.utils.string.ParameterConstants;

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
    private final GAParameters gaParameters;
    private final OCEnergySimulationCore ocESC;
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
    private int epsOptimizationObjective;
    private int plsOptimizationObjective;
    private int varOptimizationObjective;
    private double upperOverlimitFactor;
    private double lowerOverlimitFactor;

    private final OSHRandom optimizationMainRandomGenerator;
    private long optimizationMainRandomSeed;
    private int stepSize;
    private UUID hotWaterTankID;
    private final IEALogger eaLogger;

    private EnumMap<AncillaryCommodity, PriceSignal> priceSignals;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;

    //HandleSchedulingData
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
     * @param gaParameters the ga parameters
     * @param globalLogger the global logger
     * @param ocESC the oc-ESC
     */
    public GlobalControllerDataStorage(
            UUID uuid,
            ZonedDateTime now,
            IOSHStatus status,
            OCRegistry ocRegistry,
            GlobalController globalController,
            GlobalObserver globalObserver,
            OSHParameterCollection configurationParameters,
            GAParameters gaParameters,
            IGlobalLogger globalLogger,
            OCEnergySimulationCore ocESC) {

        this.uuid = uuid;
        this.now = now;
        this.status = status;
        this.ocRegistry = ocRegistry;
        this.globalController = globalController;
        this.globalObserver = globalObserver;
        this.configurationParameters = configurationParameters;
        this.gaParameters = gaParameters;
        this.globalLogger = globalLogger;
        this.ocESC = ocESC;

        try {
            this.upperOverlimitFactor =
                    Double.parseDouble(this.configurationParameters.getParameter(ParameterConstants.Optimization.upperOverlimitFactor));
        } catch (Exception e) {
            this.upperOverlimitFactor = 1.0;
            this.globalLogger.logWarning("Can't get upperOverlimitFactor, using the default value: " + this.upperOverlimitFactor);
        }

        try {
            this.lowerOverlimitFactor =
                    Double.parseDouble(this.configurationParameters.getParameter(ParameterConstants.Optimization.lowerOverlimitFactor));
        } catch (Exception e) {
            this.lowerOverlimitFactor = 1.0;
            this.globalLogger.logWarning("Can't get lowerOverlimitFactor, using the default value: " + this.lowerOverlimitFactor);
        }

        try {
            this.epsOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.epsObjective));
        } catch (Exception e) {
            this.epsOptimizationObjective = 0;
            this.globalLogger.logWarning("Can't get epsOptimizationObjective, using the default value: " + this.epsOptimizationObjective);
        }

        try {
            this.plsOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.plsObjective));
        } catch (Exception e) {
            this.plsOptimizationObjective = 0;
            this.globalLogger.logWarning("Can't get plsOptimizationObjective, using the default value: " + this.plsOptimizationObjective);
        }

        try {
            this.varOptimizationObjective =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.varObjective));
        } catch (Exception e) {
            this.varOptimizationObjective = 0;
            this.globalLogger.logWarning("Can't get varOptimizationObjective, using the default value: " + this.varOptimizationObjective);
        }

        try {
            this.optimizationMainRandomSeed =
                    Long.parseLong(this.configurationParameters.getParameter(ParameterConstants.Optimization.optimizationRandomSeed));
        } catch (Exception e) {
            this.optimizationMainRandomSeed = 0xd1ce5bL;
            this.globalLogger.logError("Can't get parameter optimizationMainRandomSeed, using the default value: " + this.optimizationMainRandomSeed);
        }
        this.optimizationMainRandomGenerator = new OSHRandom(this.optimizationMainRandomSeed);

        try {
            this.stepSize =
                    Integer.parseInt(this.configurationParameters.getParameter(ParameterConstants.Optimization.stepSize));
        } catch (Exception e) {
            this.stepSize = 60;
            this.globalLogger.logError("Can't get parameter stepSize, using the default value: " + this.stepSize);
        }

        try {
            this.hotWaterTankID =
                    UUID.fromString(this.configurationParameters.getParameter(ParameterConstants.Optimization.hotWaterTankUUID));
        } catch (Exception e) {
            this.hotWaterTankID = UUID.fromString("00000000-0000-4857-4853-000000000000");
            this.globalLogger.logError("Can't get parameter hotWaterTankUUID, using the default value: " + this.hotWaterTankID);
        }

        this.globalLogger.logDebug("Optimization StepSize = " + this.stepSize);
        this.eaLogger = new EALogger(this.globalLogger,true,true,10,20,true);
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

    public GAParameters getGaParameters() {
        return this.gaParameters;
    }

    public OCEnergySimulationCore getOcESC() {
        return this.ocESC;
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

    public OSHRandom getOptimizationMainRandomGenerator() {
        return this.optimizationMainRandomGenerator;
    }

    public long getOptimizationMainRandomSeed() {
        return this.optimizationMainRandomSeed;
    }

    public int getStepSize() {
        return this.stepSize;
    }

    public int getEpsOptimizationObjective() {
        return this.epsOptimizationObjective;
    }

    public int getPlsOptimizationObjective() {
        return this.plsOptimizationObjective;
    }

    public int getVarOptimizationObjective() {
        return this.varOptimizationObjective;
    }

    public double getUpperOverlimitFactor() {
        return this.upperOverlimitFactor;
    }

    public double getLowerOverlimitFactor() {
        return this.lowerOverlimitFactor;
    }

    public IEALogger getEaLogger() {
        return this.eaLogger;
    }

    public UUID getHotWaterTankID() {
        return this.hotWaterTankID;
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
