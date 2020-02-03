package osh.datatypes.registry.driver.details.appliance;

import osh.configuration.appliance.XsdLoadProfiles;
import osh.datatypes.registry.StateExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;


/**
 * Program driver details
 * (communication of program details)
 *
 * @author Ingo Mauser
 */
public class GenericApplianceProgramDriverDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -3542084220556069585L;
    protected String programName;
    protected String phaseName;
    protected ZonedDateTime startTime;
    protected ZonedDateTime endTime;
    protected Duration remainingTime;
    protected ZonedDateTime finishTime;


    protected XsdLoadProfiles loadProfiles;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public GenericApplianceProgramDriverDetails(
            UUID sender,
            ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    public String getProgramName() {
        return this.programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getPhaseName() {
        return this.phaseName;
    }

    public void setPhaseName(String phaseName) {
        this.phaseName = phaseName;
    }

    public ZonedDateTime getStartTime() {
        return this.startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return this.endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Duration getRemainingTime() {
        return this.remainingTime;
    }

    public void setRemainingTime(Duration remainingTime) {
        this.remainingTime = remainingTime;
    }

    public ZonedDateTime getFinishTime() {
        return this.finishTime;
    }

    public void setFinishTime(ZonedDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public XsdLoadProfiles getLoadProfiles() {
        return this.loadProfiles;
    }

    public void setLoadProfiles(XsdLoadProfiles originalLoadProfiles) {
        this.loadProfiles = originalLoadProfiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GenericApplianceProgramDriverDetails that = (GenericApplianceProgramDriverDetails) o;

        if (this.startTime != that.startTime) return false;
        if (this.endTime != that.endTime) return false;
        if (this.remainingTime != that.remainingTime) return false;
        if (this.finishTime != that.finishTime) return false;
        if (!Objects.equals(this.programName, that.programName)) return false;
        if (!Objects.equals(this.phaseName, that.phaseName)) return false;
        return Objects.equals(this.loadProfiles, that.loadProfiles);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.programName != null ? this.programName.hashCode() : 0);
        result = 31 * result + (this.phaseName != null ? this.phaseName.hashCode() : 0);
        result = 31 * result + (this.startTime != null ? this.startTime.hashCode() : 0);
        result = 31 * result + (this.endTime != null ? this.endTime.hashCode() : 0);
        result = 31 * result + (this.remainingTime != null ? this.remainingTime.hashCode() : 0);
        result = 31 * result + (this.finishTime != null ? this.finishTime.hashCode() : 0);
        result = 31 * result + (this.loadProfiles != null ? this.loadProfiles.hashCode() : 0);
        return result;
    }

    @Override
    public GenericApplianceProgramDriverDetails clone() {
        GenericApplianceProgramDriverDetails clone = new GenericApplianceProgramDriverDetails(this.getSender(),
                this.getTimestamp());
        clone.programName = this.programName;
        clone.phaseName = this.phaseName;
        clone.startTime = this.startTime;
        clone.endTime = this.endTime;
        clone.remainingTime = this.remainingTime;
        clone.finishTime = this.finishTime;

        return clone;
    }
}
