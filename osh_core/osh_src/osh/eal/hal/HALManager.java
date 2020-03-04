package osh.eal.hal;

import osh.OSH;
import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.eal.AssignedBusDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.GridConfig;
import osh.core.bus.BusManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.core.oc.LocalObserver;
import osh.eal.EALManager;
import osh.eal.EALTimeDriver;
import osh.eal.hal.exceptions.HALManagerException;
import osh.simulation.*;
import osh.simulation.energy.SimEnergySimulationCore;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.screenplay.ScreenplayType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the manager of the HAL
 *
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class HALManager extends EALManager implements ILifeCycleListener {

    private EALConfiguration ealConfig;

    private final ArrayList<HALDeviceDriver> connectedDevices;
    private final ArrayList<BusManager> connectedBusManagers;

    private SimulationEngine simEngine;

    /**
     * CONSTRUCTOR
     */
    public HALManager(OSH osh) {
        super(osh);

        this.connectedDevices = new ArrayList<>();
        this.connectedBusManagers = new ArrayList<>();
    }


    public EALTimeDriver getTimeDriver() {
        return this.getOSH().getTimeDriver();
    }

    public ArrayList<HALDeviceDriver> getConnectedDevices() {
        return this.connectedDevices;
    }

    // if you want to do 'real' things ;-), no SimEngine
    public void loadConfiguration(
            EALConfiguration ealConfig,
            ZonedDateTime forcedStartTime) throws HALManagerException {

        this.simEngine = null;
        this.loadFromConfiguration(ealConfig, forcedStartTime);

        this.getGlobalLogger().logInfo("...EAL-layer is up!");
    }

    //loading with external SimulationEngine
    public void loadConfiguration(
            EALConfiguration ealConfig,
            ZonedDateTime forcedStartTime,
            SimulationEngine simEngine) throws HALManagerException {

        this.simEngine = simEngine;
        this.loadFromConfiguration(ealConfig, forcedStartTime);
        this.initSimulationEngine();

        this.getGlobalLogger().logInfo("...EAL-layer is up!");
    }

    //building the SimulationEngine in this class
    public void loadConfiguration(
            EALConfiguration ealConfig,
            ZonedDateTime forcedStartTime,
            Long randomSeed,
            List<ConfigurationParameter> engineParameters,
            ScreenplayType currentScreenplayType,
            List<GridConfig> gridConfigurations,
            String meterUUID) throws HALManagerException {

        this.loadFromConfiguration(ealConfig, forcedStartTime);
        this.buildSimulationEngine(
                this.getOSH().getOSHStatus().getRunID(),
                this.getOSH().getOSHStatus().getConfigurationID(),
                this.getOSH().getOSHStatus().getLogDir(),
                randomSeed,
                engineParameters,
                currentScreenplayType,
                gridConfigurations,
                meterUUID);
        this.initSimulationEngine();

        this.getGlobalLogger().logInfo("...EAL-layer is up!");
    }


    private void loadFromConfiguration(
            EALConfiguration ealConfig,
            ZonedDateTime forcedStartTime) throws HALManagerException {

        boolean isSimulation = this.getOSH().getOSHStatus().isSimulation();

        this.getGlobalLogger().logInfo("...loading EAL configuration");
        this.ealConfig = ealConfig;
        if (ealConfig == null) throw new NullPointerException("ealConfig is null");

        // init real time driver and set the mode
        EALTimeDriver timeDriver = new EALTimeDriver(
                forcedStartTime,
                isSimulation,
                this.getGlobalLogger(),
                this.getOSH().getTimeRegistry());
        ((OSH) this.getOSH()).setTimeDriver(timeDriver);

        this.getGlobalLogger().logInfo("...creating EAL-BUS-devices...");
        this.processBusDeviceConfiguration();
        this.getGlobalLogger().logInfo("...creating EAL-BUS-devices... [DONE]");

        this.getGlobalLogger().logInfo("...creating EAL-device-drivers");
        this.processDeviceDriverConfiguration();
        this.getGlobalLogger().logInfo("...creating EAL-device-drivers... [DONE]");
    }

    private void buildSimulationEngine(
            String runID,
            String configurationID,
            String logDir,
            Long randomSeed,
            List<ConfigurationParameter> engineParameters,
            ScreenplayType currentScreenplayType,
            List<GridConfig> gridConfigurations,
            String meterUUID) throws HALManagerException {

        this.getGlobalLogger().logInfo("...creating EAL-SimulationEngine...");

        ISimulationActionLogger simlogger = null;
        try {
            File theDir = new File(logDir);

            // if the directory does not exist, create it
            if (!theDir.exists()) {
                theDir.mkdir();
            }

            simlogger = new ActionSimulationLogger(
                    this.getGlobalLogger(),
                    logDir + "/" + configurationID + "_" + randomSeed + "_actionlog.mxml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // MINUTEWISE POWER LOGGER
        PrintWriter powerWriter = null;
        try {
            powerWriter = new PrintWriter(new File("" + logDir + "/" + configurationID + "_" + randomSeed + "_powerlog" + ".csv"));
            powerWriter.println("currentTick"
                    + ";" + "currentActivePowerConsumption"
                    + ";" + "currentActivePowerPv"
                    + ";" + "currentActivePowerPvAutoConsumption"
                    + ";" + "currentActivePowerPvFeedIn"
                    + ";" + "currentActivePowerChp"
                    + ";" + "currentActivePowerChpAutoConsumption"
                    + ";" + "currentActivePowerChpFeedIn"
                    + ";" + "currentActivePowerBatteryCharging"
                    + ";" + "currentActivePowerBatteryDischarging"
                    + ";" + "currentActivePowerBatteryAutoConsumption"
                    + ";" + "currentActivePowerBatteryFeedIn"
                    + ";" + "currentActivePowerExternal"
                    + ";" + "currentReactivePowerExternal"
                    + ";" + "currentGasPowerExternal"
                    + ";" + "epsCosts"
                    + ";" + "plsCosts"
                    + ";" + "gasCosts"
                    + ";" + "feedInCostsPV"
                    + ";" + "feedInCostsCHP"
                    + ";" + "autoConsumptionCosts"
                    + ";" + "currentPvFeedInPrice");
        } catch (FileNotFoundException e2) {
            e2.printStackTrace();
        }

        ArrayList<OSHComponent> allDrivers = new ArrayList<>();

        allDrivers.addAll(this.connectedDevices);
        allDrivers.addAll(this.connectedBusManagers);

        SimEnergySimulationCore esc = new SimEnergySimulationCore(gridConfigurations, meterUUID);

        try {
            this.simEngine = new BuildingSimulationEngine(
                    allDrivers,
                    engineParameters,
                    esc,
                    currentScreenplayType,
                    simlogger,
                    powerWriter,
                    this.getOSH().getOSHStatus().getHhUUID());
        } catch (SimulationEngineException e) {
            throw new HALManagerException(e);
        }

        //assign time base
        this.simEngine.assignTimerDriver(this.getTimeDriver());

        this.getGlobalLogger().logInfo("...creating EAL-SimulationEngine... [DONE]");
    }

    private void initSimulationEngine() {
        //assign Com-Registry
        this.simEngine.assignComRegistry(((OSH) this.getOSH()).getComRegistry());

        //assign OC-Registry
        this.simEngine.assignOCRegistry(((OSH) this.getOSH()).getOCRegistry());

        //assign Driver-Registry
        this.simEngine.assignDriverRegistry(((OSH) this.getOSH()).getDriverRegistry());
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processDeviceDriverConfiguration() throws HALManagerException {

        for (int i = 0; i < this.ealConfig.getAssignedDevices().size(); i++) {

            AssignedDevice _device = this.ealConfig.getAssignedDevices().get(i);

            if (_device == null)
                throw new HALManagerException("configuration fail: assigned device is null!");

            // load driver parameter
            OSHParameterCollection drvParams = new OSHParameterCollection();
            drvParams.loadCollection(_device.getDriverParameters());

            // get the class of the driver an make an instance
            Class driverClass;
            try {
                driverClass = Class.forName(_device.getDriverClassName());
            } catch (ClassNotFoundException ex) {
                throw new HALManagerException(ex);
            }

            HALDeviceDriver _driver;
            try {
                Constructor<HALDeviceDriver> constructor = driverClass.getConstructor(
                        IOSH.class,
                        UUID.class,
                        OSHParameterCollection.class);
                _driver = constructor.newInstance(
                        this.getOSH(),
                        UUID.fromString(_device.getDeviceID()),
                        drvParams);
                this.getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - Driver loaded ...... [OK]");
            } catch (InstantiationException iex) {
                throw new HALManagerException("Instantiation of " + driverClass + " failed!", iex);
            } catch (Exception ex) {
                throw new HALManagerException(ex);
            }

            _driver.setControllable(_device.isControllable());
            _driver.setObservable(_device.isObservable());
            _driver.setDeviceType(_device.getDeviceType());
            _driver.setDeviceClassification(_device.getDeviceClassification());
            // add driver to the list of connected devices
            this.connectedDevices.add(_driver);

            // assign the dispatcher
            //_driver.assignDispatcher(halDispatcher);

            // get the class to the controller and the observer and refer it for
            // the cbox-layer
            if (_device.isControllable()) {
                // ...the controller class
                String controllerClassName = _device.getAssignedLocalOCUnit()
                        .getLocalControllerClassName();

                try {
                    _driver
                            .setRequiredLocalControllerClass((Class<LocalController>) Class
                                    .forName(controllerClassName));

                } catch (Exception ex) {
                    throw new HALManagerException(ex);
                }
                this.getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - LocalController loaded ...... [OK]");
            }

            if (_device.isObservable()) {
                // ...and the observer class
                String observerClassName = _device.getAssignedLocalOCUnit()
                        .getLocalObserverClassName();

                try {
                    _driver
                            .setRequiredLocalObserverClass((Class<LocalObserver>) Class
                                    .forName(observerClassName));
                } catch (ClassNotFoundException ex) {
                    throw new HALManagerException(ex);
                }
                this.getGlobalLogger().logInfo("" + _driver.getClass().getSimpleName() + " - UUID - " + _device.getDeviceID() + " - LocalObserver loaded ...... [OK]");
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processBusDeviceConfiguration() throws HALManagerException {
        for (int i = 0; i < this.ealConfig.getAssignedBusDevices().size(); i++) {
            AssignedBusDevice _device = this.ealConfig.getAssignedBusDevices().get(i);

            // load driver parameter
            OSHParameterCollection drvParams = new OSHParameterCollection();
            drvParams.loadCollection(_device.getBusDriverParameters());

            // get the class of the driver an make an instance

            Class controllerClass;
            String controllerClassName = _device.getBusManagerClassName();
            if (controllerClassName == null || controllerClassName.isEmpty()) {
                throw new HALManagerException("no com manager for driver " + _device.getBusDeviceID() + " available.");
            }
            try {
                controllerClass = Class.forName(controllerClassName);
            } catch (ClassNotFoundException ex) {
                throw new HALManagerException(ex);
            }

            Class busDriverClass;
            try {
                busDriverClass = Class.forName(_device.getBusDriverClassName());
            } catch (ClassNotFoundException ex) {
                throw new HALManagerException(ex);
            }

            BusManager _busManager;
            try {
                _busManager = (BusManager) controllerClass.getConstructor(
                        IOSHOC.class,
                        UUID.class).newInstance(
                        this.getOSH(),
                        UUID.fromString(_device.getBusDeviceID()));
                this.getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getBusDeviceID() + " - BusManager loaded ...... [OK]");
            } catch (Exception ex) {
                throw new HALManagerException(ex);
            }

            HALBusDriver _busDriver;
            try {
                Constructor<HALBusDriver> constructor = busDriverClass.getConstructor(
                        IOSH.class,
                        UUID.class,
                        OSHParameterCollection.class);

                _busDriver = constructor.newInstance(
                        this.getOSH(),
                        UUID.fromString(_device.getBusDeviceID()),
                        drvParams);
                this.getGlobalLogger().logInfo("" + _device.getClass().getSimpleName() + " - UUID - " + _device.getBusDeviceID() + " - BusDriver loaded ...... [OK]");
            } catch (Exception ex) {
                throw new HALManagerException(ex);
            }

            _busManager.setOcDataSubscriber(_busDriver);
            _busDriver.setOcDataSubscriber(_busManager);

            _busDriver.setBusDeviceType(_device.getBusDeviceType());

            this.connectedBusManagers.add(_busManager);

        }
    }

    /**
     * get all members of the lifecycle-process. Used to trigger lifecycle-changes
     *
     * @return
     */
    private ArrayList<ILifeCycleListener> getLifeCycleMembers() {

        // device drivers
        ArrayList<ILifeCycleListener> boxLifeCycleMembers = new ArrayList<>(this.connectedDevices);

        // bus managers
        for (BusManager busManager : this.connectedBusManagers) {
            boxLifeCycleMembers.add(busManager);
            boxLifeCycleMembers.add(busManager.getBusDriver());
        }

        return boxLifeCycleMembers;
    }


//	public void startHAL() {
//		//TODO: why is nothing here?
//	}
//
//	public void addDevice(HALDeviceDriver driver, String deviceDescription) {
//		//TODO: why is nothing here?
//	}
//
//	public void removeDevice(HALDeviceDriver driver) {
//		//TODO: why is nothing here?
//	}

    public ArrayList<BusManager> getConnectedBusManagers() {
        return this.connectedBusManagers;
    }

    public SimulationEngine getSimEngine() {
        return this.simEngine;
    }

    public void initDatabaseLogging() throws HALManagerException {
        if (this.simEngine != null && this.simEngine instanceof BuildingSimulationEngine) {
            ((BuildingSimulationEngine) this.simEngine).setDatabaseLogging();
        } else {
            throw new HALManagerException("Unable to initiate database logging with this SimulationEngine");
        }
    }

    public void loadScreenplay(String screenplayFileName) throws SimulationEngineException, HALManagerException {
        if (this.simEngine != null && this.simEngine instanceof BuildingSimulationEngine) {
            ((BuildingSimulationEngine) this.simEngine).loadSingleScreenplayFromFile(screenplayFileName);
        } else {
            throw new HALManagerException("Unable to load Screenplay with this SimulationEngine");
        }
    }

    public OSHSimulationResults startSimulation(long simulationDuration) throws SimulationEngineException, HALManagerException {
        if (this.simEngine != null && this.simEngine instanceof BuildingSimulationEngine) {
            this.simEngine.notifySimulationIsUp();
            return (OSHSimulationResults) this.simEngine.runSimulation(simulationDuration);
        } else {
            throw new HALManagerException("Unable to load start simulation with this SimulationEngine");
        }
    }

    @Override
    public void onSystemRunning() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemRunning();
        }
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemShutdown();
        }

        if (this.simEngine != null) {
            ((BuildingSimulationEngine) this.simEngine).shutdown();
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemIsUp();
        }
    }

    @Override
    public void onSystemHalt() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemHalt();
        }
    }

    @Override
    public void onSystemResume() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemResume();
        }
    }

    @Override
    public void onSystemError() throws OSHException {
        for (ILifeCycleListener listener : this.getLifeCycleMembers()) {
            listener.onSystemError();
        }
    }
}
