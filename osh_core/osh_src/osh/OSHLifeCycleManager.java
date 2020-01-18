package osh;

import osh.cal.CALManager;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.cal.CALConfiguration;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.OSHConfiguration;
import osh.core.DataBroker;
import osh.core.LifeCycleStates;
import osh.core.OCManager;
import osh.core.OSHRandomGenerator;
import osh.core.exceptions.LifeCycleManagerException;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.core.logging.OSHGlobalLogger;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.eal.hal.HALManager;
import osh.eal.hal.exceptions.HALManagerException;
import osh.registry.ComRegistry;
import osh.registry.DriverRegistry;
import osh.registry.OCRegistry;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.OSHSimulationResults;
import osh.simulation.SimulationEngine;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.xml.XMLSerialization;

import java.time.ZoneId;
import java.util.Random;
import java.util.UUID;

public class OSHLifeCycleManager {

    private final OSH theOrganicSmartHome;

    private OCManager ocManager;
    private HALManager ealManager;
    private CALManager calManager;

    private DataBroker dataBroker;

    private SimulationEngine simEngine;
    private boolean hasSimEngine;
    private ScreenplayType screenPlayType;

    private LifeCycleStates currentState;

    private final IGlobalLogger globalLogger;


    /**
     * OSH will be instantiated in this class with external logger
     */
    public OSHLifeCycleManager(IGlobalLogger globalLogger) {
        this(new OSH(), globalLogger);
    }

    /**
     * external OSH with external logger
     */
    public OSHLifeCycleManager(OSH theOrganicSmartHome, IGlobalLogger globalLogger) {
        this.theOrganicSmartHome = theOrganicSmartHome;
        this.globalLogger = globalLogger;

        this.theOrganicSmartHome.setLogger(globalLogger);
    }

    /**
     * OSH and logger will be instantiated in this class
     */
    public OSHLifeCycleManager(SystemLoggerConfiguration systemLoggerConfiguration) {
        this(new OSH(), systemLoggerConfiguration, false);
    }

    /**
     * external OSH with logger instantiated in this class
     */
    public OSHLifeCycleManager(
            OSH theOrganicSmartHome,
            SystemLoggerConfiguration systemLoggerConfiguration) {

        this(theOrganicSmartHome, systemLoggerConfiguration, false);
    }

    /**
     * additional flag for the instantiation of the global logger
     */
    public OSHLifeCycleManager(
            OSH theOrganicSmartHome,
            SystemLoggerConfiguration systemLoggerConfiguration,
            boolean dontInitLogger) {

        this.theOrganicSmartHome = theOrganicSmartHome;

        // create a new Logger with default LogLevel: error
        this.globalLogger = new OSHGlobalLogger(theOrganicSmartHome, systemLoggerConfiguration, dontInitLogger);
        this.theOrganicSmartHome.setLogger(this.globalLogger);
    }

    /**
     * Initialise the Organic Smart Home based on the given configuration files,
     * - without a SimEngine (real)
     * - with a ComRegistry instantiated here
     */
    public void initRealOSHFirstStep(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir) throws LifeCycleManagerException {

        this.hasSimEngine = false;
        ComRegistry comRegistry = new ComRegistry(this.theOrganicSmartHome);

        this.initOSHReadInFiles(
                oshConfigFile,
                ocConfigFile,
                ealConfigFile,
                calConfigFile,
                hostTimeZone,
                forcedStartTime,
                randomSeed,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir,
                comRegistry);
    }

    /**
     * Initialise the Organic Smart Home based on the given configuration files,
     * - without a SimEngine (real)
     * - with an external ComRegistry
     */
    public void initRealOSHFirstStep(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir,
            ComRegistry comRegistry) throws LifeCycleManagerException {

        this.hasSimEngine = false;

        this.initOSHReadInFiles(
                oshConfigFile,
                ocConfigFile,
                ealConfigFile,
                calConfigFile,
                hostTimeZone,
                forcedStartTime,
                randomSeed,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir,
                comRegistry);
    }

    /**
     * Initialise the Organic Smart Home based on the given configuration files,
     * - with an external SimEngine
     * - with an external ComRegistry
     */
    public void initOSHFirstStep(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir,
            SimulationEngine simEngine,
            ComRegistry comRegistry) throws LifeCycleManagerException {

        this.simEngine = simEngine;
        this.hasSimEngine = true;

        this.initOSHReadInFiles(
                oshConfigFile,
                ocConfigFile,
                ealConfigFile,
                calConfigFile,
                hostTimeZone,
                forcedStartTime,
                randomSeed,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir,
                comRegistry);
    }

    /**
     * Initialise the Organic Smart Home based on the given configuration files,
     * - with a SimEngine instantiated in the EAL-Manager
     * - with a ComRegistry instantiated in this class
     */
    public void initOSHFirstStep(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir,
            ScreenplayType screenPlayType) throws LifeCycleManagerException {

        this.hasSimEngine = true;
        this.screenPlayType = screenPlayType;
        ComRegistry comRegistry = new ComRegistry(this.theOrganicSmartHome);

        this.initOSHReadInFiles(
                oshConfigFile,
                ocConfigFile,
                ealConfigFile,
                calConfigFile,
                hostTimeZone,
                forcedStartTime,
                randomSeed,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir,
                comRegistry);
    }

    /**
     * Initialise the Organic Smart Home based on the given configuration files,
     * - with a SimEngine instantiated in the EAL-Manager
     * - with an external ComRegistry
     */
    public void initOSHFirstStep(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir,
            ComRegistry comRegistry,
            ScreenplayType screenPlayType) throws LifeCycleManagerException {

        this.hasSimEngine = true;
        this.screenPlayType = screenPlayType;

        this.initOSHReadInFiles(
                oshConfigFile,
                ocConfigFile,
                ealConfigFile,
                calConfigFile,
                hostTimeZone,
                forcedStartTime,
                randomSeed,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir,
                comRegistry);
    }

    /**
     * initialize the Organic Smart Home based on the given configuration files,
     * unmarshalls the configuration files
     */
    private void initOSHReadInFiles(
            String oshConfigFile,
            String ocConfigFile,
            String ealConfigFile,
            String calConfigFile,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long randomSeed,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir,
            ComRegistry comRegistry) throws LifeCycleManagerException {

        // load from files:
        Long usedRandomSeed = randomSeed;
        OSHConfiguration oshConfig;
        try {
            oshConfig = (OSHConfiguration) XMLSerialization.file2Unmarshal(
                    oshConfigFile,
                    OSHConfiguration.class);
        } catch (Exception ex) {
            this.globalLogger.logError("can't load OSH-configuration", ex);
            throw new LifeCycleManagerException(ex);
        }

        OCConfiguration ocConfig;
        try {
            ocConfig = (OCConfiguration) XMLSerialization.file2Unmarshal(
                    ocConfigFile,
                    OCConfiguration.class);
        } catch (Exception ex) {
            this.globalLogger.logError("can't load OC-configuration", ex);
            throw new LifeCycleManagerException(ex);
        }

        EALConfiguration ealConfig;
        try {
            ealConfig = (EALConfiguration) XMLSerialization.file2Unmarshal(
                    ealConfigFile,
                    EALConfiguration.class);
        } catch (Exception ex) {
            this.globalLogger.logError("can't load EAL-configuration", ex);
            throw new LifeCycleManagerException(ex);
        }

        CALConfiguration calConfig;
        try {
            calConfig = (CALConfiguration) XMLSerialization.file2Unmarshal(
                    calConfigFile,
                    CALConfiguration.class);
        } catch (Exception ex) {
            this.globalLogger.logError("can't load CAL-configuration", ex);
            throw new LifeCycleManagerException(ex);
        }

        /*
         * if a random seed is given from external it will override the random seed in the configuration package
         */
        if (usedRandomSeed == null && oshConfig.getRandomSeed() != null) {
            usedRandomSeed = Long.valueOf(oshConfig.getRandomSeed());
        }
        if (usedRandomSeed == null) {
            this.globalLogger
                    .logError(
                            "No randomSeed available: neither in OCConfig nor as Startup parameter - using default random seed!");
            this.globalLogger
                    .printDebugMessage(
                            "No randomSeed available: Using default seed \"0xd1ce5bL\"");
            usedRandomSeed = 0xd1ce5bL;
        }

        oshConfig.setRandomSeed(usedRandomSeed.toString());

        this.theOrganicSmartHome.setRandomGenerator(
                new OSHRandomGenerator(new Random(usedRandomSeed)));
        this.globalLogger.logInfo("Using random seed 0x" + Long.toHexString(usedRandomSeed));

        //assigning Registries

        // assign OCRegistry (O/C communication above HAL)
        OCRegistry ocRegistry = new OCRegistry(this.theOrganicSmartHome);
        this.theOrganicSmartHome.setOCRegistry(ocRegistry);

        // assign DriverRegistry (DeviceDriver, ComDriver and BusDriver communication below HAL)
        DriverRegistry driverRegistry = new DriverRegistry(this.theOrganicSmartHome);
        this.theOrganicSmartHome.setDriverRegistry(driverRegistry);

        // assign ComRegistry (communication to SignalProviders, REMS etc.)
        this.theOrganicSmartHome.setExternalRegistry(comRegistry);

        //instantiating the data broker and assiging it to the osh
        this.dataBroker = new DataBroker(UUID.randomUUID(), this.theOrganicSmartHome);
        this.theOrganicSmartHome.setDataBroker(this.dataBroker);

        this.initOSHSetupStatus(
                oshConfig,
                ocConfig,
                ealConfig,
                calConfig,
                hostTimeZone,
                forcedStartTime,
                optimizationMainRandomSeed,
                runID,
                configurationID,
                logDir);

    }

    private void initOSHSetupStatus(
            OSHConfiguration oshConfig,
            OCConfiguration ocConfig,
            EALConfiguration ealConfig,
            CALConfiguration calConfig,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long optimizationMainRandomSeed,
            String runID,
            String configurationID,
            String logDir
    ) throws LifeCycleManagerException {


        boolean hasGUI = false;

        // setting flag for GUI present
        for (AssignedComDevice comDev : calConfig.getAssignedComDevices()) {
            if (comDev.getComDeviceType() == ComDeviceTypes.GUI) {
                hasGUI = true;
                break;
            }
        }
        this.theOrganicSmartHome.getOSHStatusObj().setIsGUI(hasGUI);

        // set some status variables...
        switch (oshConfig.getRunningType()) {
            case SIMULATION: {
                this.theOrganicSmartHome.getOSHStatusObj().setIsSimulation(true);
                this.theOrganicSmartHome.getOSHStatusObj().setVirtual(false);
                break;
            }
            case REAL: {
                this.theOrganicSmartHome.getOSHStatusObj().setIsSimulation(false);
                this.theOrganicSmartHome.getOSHStatusObj().setVirtual(false);
                break;
            }
            case HIL: {
                this.theOrganicSmartHome.getOSHStatusObj().setIsSimulation(false);
                this.theOrganicSmartHome.getOSHStatusObj().setVirtual(true);
                break;
            }
        }

        // info: record simulation is already set in the constructor
        this.theOrganicSmartHome.getOSHStatusObj().setRunID(runID);
        this.theOrganicSmartHome.getOSHStatusObj().setConfigurationID(configurationID);
        this.theOrganicSmartHome.getOSHStatusObj().setLogDir(logDir);
        this.theOrganicSmartHome.getOSHStatusObj().setHhUUID(UUID.fromString(oshConfig.getHhUUID()));


        this.initOSHInstantiateManagers(
                oshConfig,
                ocConfig,
                ealConfig,
                calConfig,
                hostTimeZone,
                forcedStartTime,
                optimizationMainRandomSeed,
                logDir);

    }

    private void initOSHInstantiateManagers(
            OSHConfiguration oshConfig,
            OCConfiguration ocConfig,
            EALConfiguration ealConfig,
            CALConfiguration calConfig,
            ZoneId hostTimeZone,
            long forcedStartTime,
            Long optimizationMainRandomSeed,
            String logDir) throws LifeCycleManagerException {

        this.ocManager = new OCManager(this.theOrganicSmartHome);
        this.ealManager = new HALManager(this.theOrganicSmartHome);
        this.calManager = new CALManager(this.theOrganicSmartHome);

        try {
            this.globalLogger.logInfo("...starting EAL-layer");


            if (this.hasSimEngine) {
                if (this.simEngine != null) {
                    this.ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime, this.simEngine);
                } else {
                    this.ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime, Long.valueOf(oshConfig.getRandomSeed()), oshConfig.getEngineParameters(),
                            this.screenPlayType, oshConfig.getGridConfigurations(), oshConfig.getMeterUUID());
                    this.simEngine = this.ealManager.getSimEngine();
                }
            } else {
                this.ealManager.loadConfiguration(ealConfig, hostTimeZone, forcedStartTime);
            }
        } catch (Exception ex) {
            throw new LifeCycleManagerException(ex);
        }

        try {
            this.globalLogger.logInfo("...starting O/C-layer");
            this.ocManager.loadConfiguration(
                    ocConfig,
                    oshConfig.getGridConfigurations(),
                    oshConfig.getMeterUUID(),
                    this.ealManager.getConnectedDevices(),
                    optimizationMainRandomSeed,
                    logDir);
        } catch (Exception ex) {
            throw new LifeCycleManagerException(ex);
        }

        try {
            this.globalLogger.logInfo("...starting CAL-layer");
            this.calManager.loadConfiguration(calConfig);
        } catch (Exception ex) {
            throw new LifeCycleManagerException(ex);
        }

        try {
            this.switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_IS_UP);
            this.globalLogger.logInfo("...Organic Smart Home is up!");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
            throw new LifeCycleManagerException(ex);
        }

        //start all component threads (do not start earlier, otherwise components
        //get callbacks before the system is up. This can lead to evil race conditions.
        this.theOrganicSmartHome.getComRegistry().startQueueProcessingThreads();
        this.theOrganicSmartHome.getOCRegistry().startQueueProcessingThreads();
        this.theOrganicSmartHome.getComRegistry().startQueueProcessingThreads();

        this.theOrganicSmartHome.getTimer().startTimerProcessingThreads();
    }

//	private void handleSimEngine(
//			String runID,
//			String configurationID,
//			String logDir) {
//		
//		//if sim engine is not from external, instantiate it now
//		if (simEngine == null) {
//
//			ISimulationActionLogger simlogger = null;
//			try {
//				File theDir = new File(logDir);
//
//				// if the directory does not exist, create it
//				if (!theDir.exists()) {
//					theDir.mkdir();  
//				}
//
//				simlogger = new ActionSimulationLogger( 
//						globalLogger, 
//						logDir + "/" + configurationID + "_" + oshConfiguration.getRandomSeed() + "_actionlog.mxml");
//			} 
//			catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//
//			// MINUTEWISE POWER LOGGER
//			PrintWriter powerWriter = null;
//			try {
//				powerWriter = new PrintWriter(new File("" + logDir + "/" + configurationID + "_" + oshConfiguration.getRandomSeed() + "_powerlog" + ".csv"));
//				powerWriter.println("currentTick" 
//						+ ";" + "currentActivePowerConsumption"
//						+ ";" + "currentActivePowerPv"
//						+ ";" + "currentActivePowerPvAutoConsumption" 
//						+ ";" + "currentActivePowerPvFeedIn"
//						+ ";" + "currentActivePowerChp"
//						+ ";" + "currentActivePowerChpAutoConsumption" 
//						+ ";" + "currentActivePowerChpFeedIn"
//						+ ";" + "currentActivePowerBatteryCharging"
//						+ ";" + "currentActivePowerBatteryDischarging"
//						+ ";" + "currentActivePowerBatteryAutoConsumption" 
//						+ ";" + "currentActivePowerBatteryFeedIn"
//						+ ";" + "currentActivePowerExternal"
//						+ ";" + "currentReactivePowerExternal" 
//						+ ";" + "currentGasPowerExternal"
//						+ ";" + "epsCosts"
//						+ ";" + "plsCosts"
//						+ ";" + "gasCosts"
//						+ ";" + "feedInCostsPV"
//						+ ";" + "feedInCostsCHP"
//						+ ";" + "autoConsumptionCosts"
//						+ ";" + "currentPvFeedInPrice");
//			} 
//			catch (FileNotFoundException e2) {
//				e2.printStackTrace();
//			}
//			
//			ArrayList<OSHComponent> allDrivers = new ArrayList<>();
//			
//			allDrivers.addAll(ealManager.getConnectedBusManagers());
//			allDrivers.addAll(ealManager.getConnectedDevices());
//
//			simEngine = new BuildingSimulationEngine(
//					allDrivers,
//					oshConfiguration.getEngineParameters(),
//					ocManager.getESC(),
//					currentScreenplayType,
//					simlogger,
//					powerWriter);
//
//			//assign time base
//			simEngine.assignTimerDriver(ealManager.getRealTimeDriver());
//		}
//		
//		//assign Com-Registry
//		simEngine.assignComRegistry(theOrganicSmartHome.getComRegistry());
//
//		//assign OC-Registry
//		simEngine.assignOCRegistry(theOrganicSmartHome.getOCRegistry());
//
//		//assign Driver-Registry
//		simEngine.assignDriverRegistry(theOrganicSmartHome.getDriverRegistry());
//	}


    public void switchToLifeCycleState(LifeCycleStates nextState) throws OSHException {
        this.currentState = nextState;
        switch (nextState) {
            case ON_SYSTEM_INIT: {
                //NOTHING
                break;
            }
            case ON_SYSTEM_RUNNING: {
                this.dataBroker.onSystemRunning();
                this.ocManager.onSystemRunning();
                this.ealManager.onSystemRunning();
                this.calManager.onSystemRunning();
                this.globalLogger.logInfo("...switching to SYSTEM_RUNNING");
                break;
            }
            case ON_SYSTEM_IS_UP: {
                this.dataBroker.onSystemIsUp();
                this.ocManager.onSystemIsUp();
                this.ealManager.onSystemIsUp();
                this.calManager.onSystemIsUp();
                this.globalLogger.logInfo("...switching to SYSTEM_IS_UP");
                break;
            }
            case ON_SYSTEM_SHUTDOWN: {
                this.dataBroker.onSystemShutdown();
                this.ocManager.onSystemShutdown();
                this.ealManager.onSystemShutdown();
                this.calManager.onSystemShutdown();
                DatabaseLoggerThread.shutDown();
                this.globalLogger.logInfo("...switching to SYSTEM_SHUTDOWN");
                break;
            }
            case ON_SYSTEM_HALT: {
                this.dataBroker.onSystemHalt();
                this.ocManager.onSystemHalt();
                this.ealManager.onSystemHalt();
                this.calManager.onSystemHalt();
                this.globalLogger.logInfo("...switching to SYSTEM_HALT");
                break;
            }
            case ON_SYSTEM_RESUME: {
                this.dataBroker.onSystemResume();
                this.ocManager.onSystemResume();
                this.ealManager.onSystemResume();
                this.calManager.onSystemResume();
                this.globalLogger.logInfo("...switching to SYSTEM_RESUME");
                break;
            }
            case ON_SYSTEM_ERROR: {
                this.dataBroker.onSystemError();
                this.ocManager.onSystemError();
                this.ealManager.onSystemError();
                this.calManager.onSystemError();
                this.globalLogger.logInfo("...switching to SYSTEM_ERROR");
                break;
            }
        }
    }

    /**
     * @return the currentState
     */
    protected LifeCycleStates getCurrentState() {
        return this.currentState;
    }

    public SimulationEngine getSimEngine() {
        return this.simEngine;
    }

    public void loadScreenplay(String screenplayFileName) throws LifeCycleManagerException {
        if (this.ealManager != null) {
            try {
                this.ealManager.loadScreenplay(screenplayFileName);
            } catch (SimulationEngineException | HALManagerException e) {
                throw new LifeCycleManagerException(e);
            }
        } else {
            throw new LifeCycleManagerException("Unable to load Screenplay with this EAL");
        }
    }

    public void initDatabaseLogging(boolean isDatabaseLogging, String tableName,
                                    long forcedStartTime, int[] databasesToLog) throws LifeCycleManagerException {
        if (isDatabaseLogging) {
            DatabaseLoggerThread.initLogger(tableName,
                    this.theOrganicSmartHome.getOSHStatus().getLogDir(),
                    forcedStartTime,
                    databasesToLog);

            if (this.ealManager != null) {
                try {
                    this.ealManager.initDatabaseLogging();
                } catch (HALManagerException e) {
                    throw new LifeCycleManagerException(e);
                }
            }
        }
    }

    public OSHSimulationResults startSimulation(long simulationDuration) throws LifeCycleManagerException, OSHException {

        if (this.ealManager != null) {
            this.switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_RUNNING);
            try {
                this.globalLogger.logInfo("... sim started");
                OSHSimulationResults simResults = this.ealManager.startSimulation(simulationDuration);
                this.globalLogger.logInfo("... sim stopped");
                return simResults;
            } catch (SimulationEngineException | HALManagerException e) {
                throw new LifeCycleManagerException(e);
            }
        } else {
            throw new LifeCycleManagerException("Unable to start simulation with this EAL");
        }
    }

}
