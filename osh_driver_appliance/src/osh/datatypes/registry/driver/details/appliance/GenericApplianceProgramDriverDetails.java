package osh.datatypes.registry.driver.details.appliance;

import osh.configuration.appliance.XsdLoadProfiles;
import osh.datatypes.registry.StateExchange;

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
    protected long startTime;
    protected long endTime;
    protected int remainingTime;
    protected long finishTime;


    protected XsdLoadProfiles loadProfiles;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    private GenericApplianceProgramDriverDetails() {
        this(null, 0);
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public GenericApplianceProgramDriverDetails(
            UUID sender,
            long timestamp) {
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

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getRemainingTime() {
        return this.remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public long getFinishTime() {
        return this.finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public XsdLoadProfiles getLoadProfiles() {
        return this.loadProfiles;
    }

    public void setLoadProfiles(XsdLoadProfiles originalLoadProfiles) {
        this.loadProfiles = originalLoadProfiles;
    }

    //TODO equals
//	@Override
//	public boolean equals(Object obj) {
//
//		if(this.programName == null) {
//			if(other.programName != null)
//				return false;
//		} else if(!this.programName.equals(other.programName)) {
//				return false;
//		}
//		
//		if(this.phaseName == null) {
//			if(other.phaseName != null)
//				return false;
//		} else if(!this.phaseName.equals(other.phaseName)) {
//			return false;
//		}
//		
//		return super.equals(obj);
//	}

    // TODO cloning
}
