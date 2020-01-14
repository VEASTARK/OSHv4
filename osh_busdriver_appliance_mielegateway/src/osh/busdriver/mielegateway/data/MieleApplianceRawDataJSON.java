package osh.busdriver.mielegateway.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The XML homebus device detail root node
 *
 * @author Kaibin Bao
 */

public class MieleApplianceRawDataJSON {
    @JsonProperty("applianceTypeName")
    private String applianceTypeName;

    @JsonProperty("stateName")
    private String stateName;

    @JsonProperty("programName")
    private String programName;

    @JsonProperty("phaseName")
    private String phaseName;

    @JsonIgnore
    private MieleDuration startTime;

    @JsonIgnore
    private MieleDuration smartStartTime;

    @JsonIgnore
    private MieleDuration remainingTime;

    @JsonIgnore
    private MieleDuration duration;

    @JsonIgnore
    private MieleDuration endTime;

    /* GETTERS */

    public String getApplianceTypeName() {
        return this.applianceTypeName;
    }

    public String getStateName() {
        return this.stateName;
    }

    public String getProgramName() {
        return this.programName;
    }

    public String getPhaseName() {
        return this.phaseName;
    }

    public MieleDuration getStartTime() {
        return this.startTime;
    }

    @JsonProperty("startTime")
    public void setStartTime(Integer startTime) {
        if (startTime != null) {
            this.startTime = new MieleDuration(startTime);
        } else {
            this.startTime = null;
        }
    }

    public MieleDuration getSmartStartTime() {
        return this.smartStartTime;
    }

    @JsonProperty("smartStartTime")
    public void setSmartStartTime(Integer smartStartTime) {
        if (smartStartTime != null) {
            this.smartStartTime = new MieleDuration(smartStartTime);
        } else {
            this.smartStartTime = null;
        }
    }

    public MieleDuration getRemainingTime() {
        return this.remainingTime;
    }

    @JsonProperty("remainingTime")
    public void setRemainingTime(Integer remainingTime) {
        if (remainingTime != null) {
            this.remainingTime = new MieleDuration(remainingTime);
        } else {
            this.remainingTime = null;
        }
    }

    public MieleDuration getDuration() {
        return this.duration;
    }

    @JsonProperty("duration")
    public void setDuration(Integer duration) {
        if (duration != null) {
            this.duration = new MieleDuration(duration);
        } else {
            this.duration = null;
        }
    }

    public MieleDuration getEndTime() {
        return this.endTime;
    }

    @JsonProperty("endTime")
    public void setEndTime(Integer endTime) {
        if (endTime != null) {
            this.endTime = new MieleDuration(endTime);
        } else {
            this.endTime = null;
        }
    }
}
