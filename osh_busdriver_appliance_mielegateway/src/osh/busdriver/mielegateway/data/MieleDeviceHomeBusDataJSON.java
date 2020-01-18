package osh.busdriver.mielegateway.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import osh.en50523.EN50523DeviceState;

import java.util.ArrayList;


/**
 * A Miele device on the XML homebus
 *
 * @author Kaibin Bao
 */
public class MieleDeviceHomeBusDataJSON {
    @JsonProperty("class")
    private int deviceClass;

    @JsonProperty("uid")
    private int uid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("state")
    private EN50523DeviceState state;
//	private int state;

    @JsonProperty("additionalName")
    private String additionalName;

    @JsonProperty("room")
    private String room;

    @JsonProperty("roomId")
    private String roomId;

    @JsonProperty("roomLevel")
    private String roomLevel;

    @JsonProperty("stateName")
    private String stateName;

    @JsonProperty("phaseName")
    private String phaseName;

    @JsonIgnore
    private MieleDuration duration;

    @JsonIgnore
    private MieleDuration startTime;

    @JsonIgnore
    private MieleDuration remainingTime;

    @JsonProperty("actions")
    private ArrayList<String> actions;

    @JsonProperty("detailsUrl")
    private String detailsUrl;

    @JsonProperty("deviceDetails")
    private MieleApplianceRawDataJSON deviceDetails;

    /* GETTERS */

    public int getDeviceClass() {
        return this.deviceClass;
    }

    public int getUid() {
        return this.uid;
    }

    public String getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public EN50523DeviceState getState() {
        return this.state;
    }

//	public int getState() {
//		return state;
//	}

    public String getAdditionalName() {
        return this.additionalName;
    }

    public String getRoomName() {
        return this.room;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getRoomLevel() {
        return this.roomLevel;
    }

    public String getStateName() {
        return this.stateName;
    }

    public String getPhaseName() {
        return this.phaseName;
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

    public MieleDuration getRemainingTime() {
        return this.remainingTime;
    }

    /* SETTERS */

    @JsonProperty("remainingTime")
    public void setRemainingTime(Integer remainingTime) {
        if (remainingTime != null) {
            this.remainingTime = new MieleDuration(remainingTime);
        } else {
            this.remainingTime = null;
        }
    }

    public String getDetailsUrl() {
        return this.detailsUrl;
    }

    public MieleApplianceRawDataJSON getDeviceDetails() {
        return this.deviceDetails;
    }

    public void setDeviceDetails(MieleApplianceRawDataJSON deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    public ArrayList<String> getActions() {
        return this.actions;
    }

    public void setActions(ArrayList<String> actions) {
        this.actions = actions;
    }

    @Override
    public String toString() {
        return String.format("miele device %x, class %x, state %s", this.uid, this.deviceClass, this.state.toString());
    }
}
