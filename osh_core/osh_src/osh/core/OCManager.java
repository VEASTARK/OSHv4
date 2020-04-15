package osh.core;

import osh.OSH;
import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.oc.CostConfiguration;
import osh.configuration.oc.EAConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.GridConfig;
import osh.core.exceptions.OCManagerException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.core.logging.IGlobalLogger;
import osh.core.oc.*;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.OptimizationEnergySimulationCore;
import osh.utils.string.ParameterConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * for initialization an management of the Organic Smart Home
 *
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth, Sebastian Kramer
 */
public class OCManager extends OSHComponent implements ILifeCycleListener {

    private OSHParameterCollection globalObserverParameterCollection;
    private OSHParameterCollection globalControllerParameterCollection;

    private GlobalOCUnit globalOCunit;

    private ArrayList<LocalOCUnit> localOCUnits;

    public OCManager(OSH theOrganicSmartHome) {
        super(theOrganicSmartHome);
    }

    @Override
    public IOSHOC getOSH() {
        return (IOSHOC) super.getOSH();
    }

    /**
     * initialize the Organic Smart Home based on the given configuration objects
     *
     * @param ocConfig
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void loadConfiguration(
            OCConfiguration ocConfig,
            List<GridConfig> gridConfigurations,
            String meterUUID,
            List<HALDeviceDriver> deviceDrivers,
            Long optimizationMainRandomSeed,
            String logDir) throws OCManagerException {

        /*
         * if a optimisation-random seed is given from external it will override the optimisation-random seed in the configuration package
         */
        Long usedOptimizationRandomSeed = optimizationMainRandomSeed;
        if (optimizationMainRandomSeed == null && ocConfig.getOptimizationMainRandomSeed() != null) {
            usedOptimizationRandomSeed = Long.parseLong(ocConfig.getOptimizationMainRandomSeed());
        }
        if (optimizationMainRandomSeed == null) {
            this.getLogger()
                    .logError(
                            "No optimizationMainRandomSeed available: neither in OCConfig nor as Startup parameter - using default random seed!");
            this.getLogger()
                    .printDebugMessage(
                            "No optimizationMainRandomSeed available: Using default seed \"0xd1ce5bL\"");
            usedOptimizationRandomSeed = 0xd1ce5bL;
        }

        ocConfig.setOptimizationMainRandomSeed(String.valueOf(usedOptimizationRandomSeed));


        this.getLogger().logInfo("...initializing OC Manager of Organic Smart Home");

        // create OCEnergySimulationCore
        OptimizationEnergySimulationCore ocESC;
        try {
            ocESC = new OptimizationEnergySimulationCore(gridConfigurations, meterUUID);
        } catch (HALManagerException e) {
            throw new OCManagerException(e);
        }


        // create local OC-Unit connected with the specific HAL-driver
        this.localOCUnits = this.createLocalOCUnits(deviceDrivers);

        // load global o/c-unit
        GlobalObserver globalObserver;
        GlobalController globalController;
        this.getLogger().logInfo("...creating global O/C-units");
        Class globalObserverClass;
        Class globalControllerClass;

        this.globalObserverParameterCollection = new OSHParameterCollection();
        this.globalObserverParameterCollection.loadCollection(ocConfig
                .getGlobalObserverParameters());

        this.globalControllerParameterCollection = new OSHParameterCollection();
        this.globalControllerParameterCollection.loadCollection(ocConfig
                .getGlobalControllerParameters());

        this.globalControllerParameterCollection.setParameter(ParameterConstants.Optimization.optimizationRandomSeed,
                ocConfig.getOptimizationMainRandomSeed());

        try {
            globalObserverClass = Class.forName(ocConfig.getGlobalObserverClass());
            globalControllerClass = Class.forName(ocConfig.getGlobalControllerClass());
        } catch (Exception ex) {
            throw new OCManagerException(ex);
        }

        try {
            globalObserver = (GlobalObserver) globalObserverClass
                    .getConstructor(IOSHOC.class,
                            OSHParameterCollection.class).newInstance(
                            this.getOSH(),
                            this.globalObserverParameterCollection);
        } catch (Exception ex) {
            throw new OCManagerException(ex);
        }

        try {
            globalController = (GlobalController) globalControllerClass
                    .getConstructor(IOSHOC.class,
                            OSHParameterCollection.class,
                            EAConfiguration.class,
                            CostConfiguration.class,
                            OptimizationEnergySimulationCore.class).newInstance(
                            this.getOSH(),
                            this.globalControllerParameterCollection,
                            ocConfig.getEaConfiguration(),
                            ocConfig.getCostConfiguration(),
                            ocESC);
        } catch (Exception ex) {
            throw new OCManagerException(ex);
        }
        ((OSH) this.getOSH()).setGlobalObserver(globalObserver);
        ((OSH) this.getOSH()).setGlobalController(globalController);


        // create global O/C-unit
        this.globalOCunit = new GlobalOCUnit(
                UUID.fromString(ocConfig.getGlobalOcUuid()),
                this.getOSH(),
                globalObserver,
                globalController);

        this.registerLocalUnits();
    }

    /**
     * get all members of the lifecycle-process. Used to trigger lifecycle-changes
     *
     * @return
     */
    private ArrayList<ILifeCycleListener> getLifeCycleMembers() {

        ArrayList<ILifeCycleListener> boxLifeCycleMembers = new ArrayList<>();

        // OC-units for device drivers
        for (LocalOCUnit localOCUnit : this.localOCUnits) {

            if (localOCUnit.localObserver != null) {
                boxLifeCycleMembers
                        .add(localOCUnit.localObserver);
            }
            if (localOCUnit.localController != null) {
                boxLifeCycleMembers
                        .add(localOCUnit.localController);
            }
        }

        boxLifeCycleMembers.add(this.globalOCunit.getObserver());
        boxLifeCycleMembers.add(this.globalOCunit.getController());

        return boxLifeCycleMembers;
    }

    /**
     * creates local o/c-unit based on the driver information. Only for devices
     * witch are at least "Observable" such an instance will be created
     *
     * @param deviceDrivers
     * @return
     */
    private ArrayList<LocalOCUnit> createLocalOCUnits(
            List<HALDeviceDriver> deviceDrivers)
            throws OCManagerException {

        ArrayList<LocalOCUnit> _localOCUnits = new ArrayList<>();

        this.getLogger().logInfo("...creating local units");

        for (HALDeviceDriver deviceDriver : deviceDrivers) {

            // is this device able to be observed or controlled?
            // ...then build an o/c-unit
            // otherwise do nothing ;-)
            LocalObserver localObserver;

            if (deviceDriver.isObservable()) {
                // getting the class for the local oc unit
                try {
                    localObserver = deviceDriver
                            .getRequiredLocalObserverClass()
                            .getConstructor(IOSHOC.class)
                            .newInstance(this.getOSH());
                } catch (Exception ex) {
                    throw new OCManagerException(ex);
                }

                LocalController localController = null;

                if (deviceDriver.isControllable()) {
                    try {
                        localController = deviceDriver
                                .getRequiredLocalControllerClass()
                                .getConstructor(IOSHOC.class)
                                .newInstance(this.getOSH());
                    } catch (Exception ex) {
                        throw new OCManagerException(ex);
                    }

                }
                // init localunit and refer the realtime module

                // UUID id = _driver.getDeviceID();

                LocalOCUnit _localOCUnit = new LocalOCUnit(
                        this.getOSH(),
                        deviceDriver.getUUID(),
                        localObserver,
                        localController);

                // assign the type of the device an it's classification
                _localOCUnit.setDeviceType(deviceDriver.getDeviceType());
                _localOCUnit.setDeviceClassification(deviceDriver
                        .getDeviceClassification());

                // register the local observer at the specific HAL-driver
                // (Observer Pattern/publish-subscribe)
                deviceDriver.setOcDataSubscriber(_localOCUnit.localObserver);

                // do the same with the local controller
                // Before check if the controller is available
                // (it's not available when the device is not controllable)
                if (_localOCUnit.localController != null) {
                    _localOCUnit.localController.setOcDataSubscriber(deviceDriver);
                }

                // add the new unit
                _localOCUnits.add(_localOCUnit);
            }
        }

        return _localOCUnits;
    }

    /**
     * register local o/c-units at the Organic Smart Home (global o/c-unit)
     */
    private void registerLocalUnits() {
        for (LocalOCUnit _localUnit : this.localOCUnits) {
            try {
                this.globalOCunit.registerLocalUnit(_localUnit);
            } catch (OSHException e) {
                this.getGlobalLogger().logError("", e);
            }
        }
    }

    /**
     * get the global logger => here the configuration of the logger can be done
     * during the runtime.
     *
     * @return
     */
    public IGlobalLogger getLogger() {
        return this.getOSH().getLogger();
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
