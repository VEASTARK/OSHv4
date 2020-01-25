package osh.simulation;

import osh.eal.EALTimeDriver;
import osh.registry.Registry;
import osh.registry.Registry.ComRegistry;
import osh.registry.Registry.DriverRegistry;
import osh.registry.Registry.OCRegistry;
import osh.simulation.exception.SimulationEngineException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 */
public abstract class SimulationEngine {

    //LOGGING
    protected SimulationResults oshSimulationResults;

    //TIMER
    protected EALTimeDriver timeDriver;

    //COMMUNICATION
    protected ComRegistry comRegistry;
    protected OCRegistry ocRegistry;
    protected DriverRegistry driverRegistry;

    private long currentSimulationTick = -1;
    private long simulationDuration;

    private final Set<SimulationEngine> subEngines = new HashSet<>();


    // ## IMPORTANT GETTERS AND SETTERS ##

    // # TIMER DRIVER #
    public void assignTimerDriver(EALTimeDriver timeDriver) {
        this.timeDriver = timeDriver;
    }

    // # REGISTRIES #
    // EXTERNAL REGISTRY
    public void assignComRegistry(ComRegistry externalRegistry) {
        this.comRegistry = externalRegistry;
    }

    // OC REGISTRY
    public void assignOCRegistry(OCRegistry ocRegistry) {
        this.ocRegistry = ocRegistry;
    }

    // DRIVER REGISTRY
    public void assignDriverRegistry(DriverRegistry driverRegistry) {
        this.driverRegistry = driverRegistry;
    }


    // # SUB SIMULATION ENGINES #
    // add engine that depends on this engine
    public void addSubSimulationEngine(SimulationEngine simEngine) {
        this.subEngines.add(simEngine);
    }

    // remove engine that depends on this engine
    public void removeSubSimulationEngine(SimulationEngine simEngine) {
        this.subEngines.remove(simEngine);
    }

    // ## LOGIC ##


    /**
     * will call every simulation Engine that the simulation setup is complete.
     * So every ISimulationSubject will be notified, too
     *
     * @throws SimulationEngineException
     */
    public void notifySimulationIsUp() throws SimulationEngineException {
        // notify main engine that simulation is up
        this.notifyLocalEngineOnSimulationIsUp();

        // notify sub engines that simulation is up
        for (SimulationEngine simulationEngine : this.subEngines) {
            simulationEngine.notifySimulationIsUp();
        }
    }

    abstract protected void notifyLocalEngineOnSimulationIsUp() throws SimulationEngineException;

    /**
     * @param currentTick
     * @param doSimulation if true, do a full simulation with a call to
     *                     simulateNextTimeTick, if false, only update timerdriver and empty all
     *                     queues.
     * @throws SimulationEngineException
     */
    private void internalSimulateNextTimeTick(long currentTick, boolean doSimulation) throws SimulationEngineException {
        //update realtimeDriver
        //doNextSimulationTickTimer:
        if (this.timeDriver != null) this.timeDriver.updateTimer(currentTick);

        //do the simulation in subengines
        if (doSimulation) {
            this.simulateNextTimeTick(currentTick);
            for (SimulationEngine e : this.subEngines) {
                e.triggerEngine();
            }
        }

        //empty all queues
        boolean queuesWereEmpty;
        do {
            queuesWereEmpty = this.doSimulateNextTimeTickQueues(currentTick);
            for (SimulationEngine e : this.subEngines) {
                queuesWereEmpty &= e.doSimulateNextTimeTickQueues(currentTick);
            }
        } while (!queuesWereEmpty);
    }


    /**
     * this function is to be called from inside internalSimulateNextTimeTick.
     *
     * @param currentTick
     */
    private boolean doSimulateNextTimeTickQueues(long currentTick) {
        boolean allQueuesWereEmpty;

        allQueuesWereEmpty = this.processRegistry(this.comRegistry);
        allQueuesWereEmpty &= this.processRegistry(this.ocRegistry);
        allQueuesWereEmpty &= this.processRegistry(this.driverRegistry);

        return allQueuesWereEmpty;
    }

    private boolean processRegistry(Registry registry) {
        // empty all queues
        if (registry != null) {
            return registry.flushQueue();
        } else {
            System.out.println("ERROR: No Registry available!");
            System.exit(0); // shutdown
            return true;
        }
    }

    /**
     * Simulate next time tick
     *
     * @throws SimulationEngineException
     */
    protected abstract void simulateNextTimeTick(long currentTick) throws SimulationEngineException;

    /**
     * start the simulation based on an external clock This function is
     * deprecated, because I can't see that the function does what it should do.
     * If I'm wrong, feel free to delete the tag.
     * <p>
     * IMA: Simulation with external clock (timerDriver), e.g. combination of
     * real house and simulated houses
     * <p>
     * TODO: simplify implementation.
     *
     * @throws SimulationEngineException
     */
    @Deprecated
    public void runSimulationByExternalClock(int startTime) throws SimulationEngineException {
        this.internalSimulateNextTimeTick(startTime, false);
    }

    /**
     * Trigger the simulation by an external clock to simulate the next step
     *
     * @throws SimulationEngineException
     */
    public void triggerEngine() throws SimulationEngineException {
        //next tick
        ++this.currentSimulationTick;

        //simulate it
        this.internalSimulateNextTimeTick(this.currentSimulationTick, true);
    }

    public SimulationResults triggerEngineWithResult() throws SimulationEngineException {
        //next tick
        ++this.currentSimulationTick;

        //simulate it
        this.internalSimulateNextTimeTick(this.currentSimulationTick, true);

        return this.oshSimulationResults;
    }

    public SimulationResults runSimulationForTick(int tick) throws SimulationEngineException {
        this.internalSimulateNextTimeTick(tick, true);
        return this.oshSimulationResults;
    }

    /**
     * start the simulation with a given numberOfTicks based on the internal clock
     *
     * @param numberOfTicks
     * @throws SimulationEngineException
     */
    public SimulationResults runSimulation(long numberOfTicks) throws SimulationEngineException {
        this.simulationDuration = numberOfTicks;
        for (int currentTick = 0; currentTick < numberOfTicks; currentTick++) {
            this.internalSimulateNextTimeTick(currentTick, true);
        }
        return this.oshSimulationResults;
    }

    /**
     * Reset the simulation timer to zero. Necessary when you want to run several simulations
     * beginning at the same start time '0'
     *
     * @throws SimulationEngineException
     */
    public void resetSimulationTimer() throws SimulationEngineException {
        this.internalSimulateNextTimeTick(0, false);
    }

    /**
     * You can set a specific start time for the simulation.
     * Normally you don't need that
     *
     * @param startTimeTick
     * @throws SimulationEngineException
     */
    public void setSimulationTimerTo(int startTimeTick) throws SimulationEngineException {
        this.internalSimulateNextTimeTick(startTimeTick, false);
    }

    /**
     * Required for appliances (to know whether it is the last day)
     *
     * @return
     */
    public long getSimulationDuration() {
        return this.simulationDuration;
    }

    public void setSimulationDuration(int duration) {
        this.simulationDuration = duration;
    }

}
