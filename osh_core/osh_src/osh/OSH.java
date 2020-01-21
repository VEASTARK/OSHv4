package osh;

import osh.core.DataBroker;
import osh.core.LifeCycleStates;
import osh.core.OSHRandomGenerator;
import osh.core.OSHStatus;
import osh.core.interfaces.IOSHCom;
import osh.core.interfaces.IOSHDriver;
import osh.core.interfaces.IOSHOC;
import osh.core.interfaces.IOSHStatus;
import osh.core.logging.IGlobalLogger;
import osh.core.oc.GlobalController;
import osh.core.oc.GlobalObserver;
import osh.eal.hal.HALRealTimeDriver;
import osh.registry.DataRegistry.ComRegistry;
import osh.registry.DataRegistry.DriverRegistry;
import osh.registry.DataRegistry.OCRegistry;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class OSH implements IOSHOC, IOSHDriver, IOSHCom {

    /* default */ IGlobalLogger logger;
    /* default */ OSHStatus oshstatus;
    /* default */ HALRealTimeDriver timer;
    /* default */ OSHRandomGenerator randomGenerator;
    /* default */ GlobalController globalcontroller;
    /* default */ GlobalObserver globalobserver;
    private OSHLifeCycleManager lifeCycleManager;
    /**
     * ExternalRegistry (communication to external units (e.g. REMS and other OSH))
     */
    private ComRegistry externalRegistry;
    /**
     * OCRegistry (O/C communication above HAL)
     */
    private OCRegistry ocRegistry;
    /**
     * DriverRegistry (HALDriver and BusDriver communication below HAL)
     */
    private DriverRegistry driverRegistry;
    private DataBroker dataBroker;


    /**
     * CONSTRUCTOR
     */
    public OSH() {
        this.oshstatus = new OSHStatus();
    }


    @Override
    public ComRegistry getComRegistry() {
        return this.externalRegistry;
    }

    public void setComRegistry(ComRegistry externalRegistry) {
        this.externalRegistry = externalRegistry;
    }


    @Override
    public OCRegistry getOCRegistry() {
        return this.ocRegistry;
    }

    public void setOCRegistry(OCRegistry ocRegistry) {
        this.ocRegistry = ocRegistry;
    }


    @Override
    public DriverRegistry getDriverRegistry() {
        return this.driverRegistry;
    }

    public void setDriverRegistry(DriverRegistry driverRegistry) {
        this.driverRegistry = driverRegistry;
    }


    @Override
    public IGlobalLogger getLogger() {
        return this.logger;
    }

    public void setLogger(IGlobalLogger logger) {
        this.logger = logger;
    }

    @Override
    public IOSHStatus getOSHStatus() {
        return this.oshstatus;
    }

    public OSHStatus getOSHStatusObj() {
        return this.oshstatus;
    }

    @Override
    public HALRealTimeDriver getTimer() {
        return this.timer;
    }

    public void setTimer(HALRealTimeDriver timer) {
        this.timer = timer;
    }

    @Override
    public OSHRandomGenerator getRandomGenerator() {
        return this.randomGenerator;
    }

    public void setRandomGenerator(OSHRandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }


    public void setControllerBoxStatus(OSHStatus cbs) {
        this.oshstatus = cbs;
    }

    @Override
    public GlobalController getGlobalController() {
        return this.globalcontroller;
    }

    public void setGlobalController(GlobalController globalcontroller) {
        this.globalcontroller = globalcontroller;
    }

    @Override
    public GlobalObserver getGlobalObserver() {
        return this.globalobserver;
    }

    public void setGlobalObserver(GlobalObserver globalobserver) {
        this.globalobserver = globalobserver;
    }

    @Override
    public boolean isSimulation() {
        return this.oshstatus.isSimulation();
    }


    /**
     * @return the currentLifeCycleState
     */
    @Override
    public LifeCycleStates getCurrentLifeCycleState() {
        return this.lifeCycleManager.getCurrentState();
    }

    /**
     */
    public OSHLifeCycleManager getLifeCycleManager() {
        return this.lifeCycleManager;
    }

    /**
     * @param lifeCycleManager the lifeCycleManager to set
     */
    public void setLifeCycleManager(
            OSHLifeCycleManager lifeCycleManager) {
        this.lifeCycleManager = lifeCycleManager;
    }

    /**
     * @return the dataBroker
     */
    public DataBroker getDataBroker() {
        return this.dataBroker;
    }

    /**
     * @param dataBroker the dataBroker to set
     */
    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }
}
