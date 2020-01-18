package osh.busdriver.mielegateway.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;
import osh.en50523.EN50523DeviceState;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;


/**
 * A Miele device on the XML homebus
 *
 * @author Kaibin Bao
 */
@XmlType
public class MieleDeviceHomeBusDataREST {
    @XmlPath("class/text()")
    private int deviceClass;

    @XmlPath("UID/text()")
    private int uid;

    @XmlPath("type/text()")
    private String type;

    @XmlPath("name/text()")
    private String name;

    @XmlPath("state/text()")
    private EN50523DeviceState state;
//	private int state;

    @XmlPath("additionalName/text()")
    private String additionalName;

    @XmlPath("room/text()")
    private String roomName;

    @XmlPath("room[@id]")
    private String roomId;

    @XmlPath("room[@level]")
    private String roomLevel;

    @XmlPath("information/key[@name='State']/@value")
    private String stateName;

    @XmlPath("information/key[@name='Phase']/@value")
    private String phaseName;

    @XmlPath("information/key[@name='Duration']/@value")
    private MieleDuration duration;

    @XmlPath("information/key[@name='Start Time']/@value")
    private MieleDuration startTime;

    @XmlPath("information/key[@name='Remaining Time']/@value")
    private MieleDuration remainingTime;

    @XmlPath("actions/action[@name='Details']/@URL")
    private String detailsUrl;

    @XmlTransient
    private MieleApplianceRawDataREST deviceDetails;

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
        return this.roomName;
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

    public MieleDuration getStartTime() {
        return this.startTime;
    }

    public MieleDuration getRemainingTime() {
        return this.remainingTime;
    }

    public String getDetailsUrl() {
        return this.detailsUrl;
    }

    public MieleApplianceRawDataREST getDeviceDetails() {
        return this.deviceDetails;
    }

    /* SETTERS */

    public void setDeviceDetails(MieleApplianceRawDataREST deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    @Override
    public String toString() {
        return String.format("miele device %x, class %x, state %s", this.uid, this.deviceClass, this.state.toString());
    }
}
