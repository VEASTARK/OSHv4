package osh.datatypes.registry.driver.details.appliance.miele;

import osh.datatypes.registry.StateExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * Communication program duration, starting time, and remaining time
 *
 * @author Kaibin Bao
 */
public class MieleApplianceDriverDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 5818061135587946337L;
    protected Duration expectedProgramDuration;
    protected ZonedDateTime startTime;
    protected Duration programRemainingTime;


    /**
     * CONSTRUCTOR
     */
    public MieleApplianceDriverDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    /**
     * gets the program duration in seconds
     */
    public Duration getExpectedProgramDuration() {
        return this.expectedProgramDuration;
    }

    /**
     * sets the program duration in seconds
     *
     * @param expectedProgramDuration
     */
    public void setExpectedProgramDuration(Duration expectedProgramDuration) {
        this.expectedProgramDuration = expectedProgramDuration;
    }

    /**
     * gets the start time from the timer set by the user
     *
     * @return
     */
    public ZonedDateTime getStartTime() {
        return this.startTime;
    }

    /**
     * setter for startTime
     *
     * @param startTime
     */
    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    /**
     * gets the remaining program time in seconds
     */
    public Duration getProgramRemainingTime() {
        return this.programRemainingTime;
    }

    /**
     * sets the remaining program time in seconds
     *
     * @param programTimeLeft
     */
    public void setProgramRemainingTime(Duration programTimeLeft) {
        this.programRemainingTime = programTimeLeft;
    }

}
