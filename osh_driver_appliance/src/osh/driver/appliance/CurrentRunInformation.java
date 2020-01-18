package osh.driver.appliance;

/**
 * @author Ingo Mauser
 */
public class CurrentRunInformation {

    private int currentProfileID;
    private int currentSegment;
    private boolean currentlyRunningPhase; // false: PAUSE, true: PHASE
    private int currentTickCounter;

    public CurrentRunInformation(
            int currentProfileID,
            int currentSegment,
            boolean currentlyRunningPhase,
            int currentTickCounter) {
        super();

        this.currentProfileID = currentProfileID;
        this.currentSegment = currentSegment;
        this.currentlyRunningPhase = currentlyRunningPhase;
        this.currentTickCounter = currentTickCounter;
    }

    public int getCurrentProfileID() {
        return this.currentProfileID;
    }

    public void setCurrentProfileID(int currentProfileID) {
        this.currentProfileID = currentProfileID;
    }

    public int getCurrentSegment() {
        return this.currentSegment;
    }

    public void setCurrentSegment(int currentSegment) {
        this.currentSegment = currentSegment;
    }

    public boolean isCurrentlyRunningPhase() {
        return this.currentlyRunningPhase;
    }

    public void setCurrentlyRunningPhase(boolean currentlyRunningPhase) {
        this.currentlyRunningPhase = currentlyRunningPhase;
    }

    public int getCurrentTickCounter() {
        return this.currentTickCounter;
    }

    public void setCurrentTickCounter(int currentTickCounter) {
        this.currentTickCounter = currentTickCounter;
    }

}
