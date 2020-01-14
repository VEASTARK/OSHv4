package osh.eal.hal;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.core.logging.IGlobalLogger;
import osh.core.threads.InvokerThreadRegistry;

import java.time.ZoneId;

/**
 * class for accessing the real-time clock in the lab scenario
 * and for accessing the ticks in the simulation...
 * Note: Time base is Unix-time ! ! !
 *
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 * @category smart-home osh HAL
 */
public class HALRealTimeDriver implements Runnable {

    final boolean runningVirtual;
    private long timeTick;
    private long unixTimeAtStart;
    private final long timeIncrement;
    private boolean isSimulation;
    private final IGlobalLogger globalLogger;
    private InvokerThreadRegistry registeredComponents;
    private final ZoneId hostTimeZone;

    /**
     * CONSTRUCTOR for testing purposes
     *
     * @param globalLogger
     * @param isSimulation
     * @param timeIncrement
     * @param forcedStartTime
     */
    public HALRealTimeDriver(
            IGlobalLogger globalLogger,
            boolean isSimulation,
            long timeIncrement,
            long forcedStartTime) {
        this(globalLogger, ZoneId.systemDefault(), isSimulation, false, timeIncrement, forcedStartTime);
    }

    /**
     * CONSTRUCTOR for normal usage
     *
     * @param globalLogger
     * @param isSimulation
     * @param runningVirtual
     * @param timeIncrement
     * @param forcedStartTime
     */
    public HALRealTimeDriver(
            IGlobalLogger globalLogger,
            ZoneId hostTimeZone,
            boolean isSimulation,
            boolean runningVirtual,
            long timeIncrement,
            long forcedStartTime) {

        this.hostTimeZone = hostTimeZone;
        this.globalLogger = globalLogger;
        this.timeIncrement = timeIncrement;
        this.isSimulation = isSimulation;
        this.runningVirtual = runningVirtual;

        if (!isSimulation) {
            new Thread(this, "RealTimeDriver").start();
            this.unixTimeAtStart = System.currentTimeMillis() / 1000L;
        } else {
            this.unixTimeAtStart = forcedStartTime;
        }
    }

    /**
     * you can register a component on the realtime-driver. So with the given frequency a component
     * will be announced that the given time period is over. You can also unregister a component (method: unregisterComponent(IRealTimeObserver iRealTimeObserver) )
     *
     * @param iRealTimeListener
     * @param refreshFrequency  (>=1)
     * @throws OSHException
     */
    public void registerComponent(IRealTimeSubscriber iRealTimeListener, long refreshFrequency) throws OSHException {
        this.registerComponent(iRealTimeListener, refreshFrequency, Thread.NORM_PRIORITY);
    }

    public void registerComponent(IRealTimeSubscriber iRealTimeListener, long refreshFrequency, int priority) throws OSHException {
        this.globalLogger.logDebug("registerComponent:" + iRealTimeListener + " refreshFrequency:" + refreshFrequency);
        this.registeredComponents.addRealtimeSubscriber(iRealTimeListener, refreshFrequency, priority);
    }

    public void unregisterComponent(IRealTimeSubscriber iRealTimeListener) {
        this.globalLogger.logDebug("unregisterComponent:" + iRealTimeListener);
        this.registeredComponents.removeRealtimeSubscriber(iRealTimeListener);
    }

/*	public void unregisterComponent(IRealTimeListener iRealTimeObserver){
        HALrealTimeComponentRegister removeCandidate = null;
        for (HALrealTimeComponentRegister _component:registedComponents){
            if (_component.iRealTimeObserver == iRealTimeObserver){
                removeCandidate = _component;
            }
        }
        if (removeCandidate != null) {
            removeCandidate.thread.should_run = false;
            registedComponents.remove(removeCandidate);
        }
    }*/

    public ZoneId getHostTimeZone() {
        return this.hostTimeZone;
    }

    public long getUnixTime() {
        long unixTime;

        if (!this.isSimulation) {
            unixTime = System.currentTimeMillis() / 1000L;
        } else {
            unixTime = this.timeTick * this.timeIncrement + this.unixTimeAtStart;
        }

        return unixTime;
    }

    /**
     * get the configured time increment of the time base. (In the real-smart-home scenario it's usually 1)
     *
     * @return
     */
    public long getTimeIncrement() {
        return this.timeIncrement;
    }

    public long getUnixTimeAtStart() {
        return this.unixTimeAtStart;
    }

    public boolean isSimulation() {
        return this.isSimulation;
    }

    public void setSimulation(boolean isSimulation) {
        this.isSimulation = isSimulation;
    }

    /**
     * duration the cb is running
     */
    public long getRuntime() {
        return this.timeTick;
    }

    /**
     * time the cb started / time since the cb is running
     */
    public long runningSince() {
        return this.unixTimeAtStart;
    }

    public void setThreadRegistry(InvokerThreadRegistry threadRegistry) {
        this.registeredComponents = threadRegistry;
    }

    //for simulation update the time from the simulation engine
    public void updateTimer(long simulationTick) {
        if (simulationTick % 3600 == 0) {
            this.globalLogger.logDebug("updateTimer(" + simulationTick + ")");
        }
        this.timeTick = simulationTick;
        this.registeredComponents.triggerInvokers();
    }

    /**
     * You should N E V E R invoke this method by yourself ! ! ! !
     * only the simulation engine should do this!
     */
    public void resetTimer() {
        this.unixTimeAtStart = System.currentTimeMillis() / 1000L;
        this.timeTick = 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000);
                ++this.timeTick;
                try {
                    this.registeredComponents.triggerInvokers();
                } catch (Exception e) {
                    // Catch all ... Should never happen
                    this.globalLogger.logError("Should never ever ever happen!", e);
                    e.printStackTrace();
                }
            } catch (InterruptedException ex) {
                this.globalLogger.logError("Should never ever ever happen!", ex);
                ex.printStackTrace();
            }
        }
    }

    public void startTimerProcessingThreads() {
        this.registeredComponents.startThreads();
    }

}
