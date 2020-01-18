package osh.busdriver.mielegateway.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlRootElement;
import java.net.URL;

/**
 * The XML homebus device detail root node
 *
 * @author Kaibin Bao
 */
@XmlRootElement(name = "device")
public class MieleApplianceRawDataREST {
    @XmlPath("information/key[@name='Appliance Type']/@value")
    private String applianceTypeName;

    @XmlPath("information/key[@name='State']/@value")
    private String stateName;

    @XmlPath("information/key[@name='Program']/@value")
    private String programName;

    @XmlPath("information/key[@name='Phase']/@value")
    private String phaseName;

    @XmlPath("information/key[@name='Start Time']/@value")
    private MieleDuration startTime;

    @XmlPath("information/key[@name='Smart Start']/@value")
    private MieleDuration smartStartTime;

    @XmlPath("information/key[@name='Remaining Time']/@value")
    private MieleDuration remainingTime;

    @XmlPath("information/key[@name='Duration']/@value")
    private MieleDuration duration;

    @XmlPath("information/key[@name='End Time']/@value")
    private MieleDuration endTime;

    /* SPECIFIC INFORMATION */



    /* COMMAND URLS */

    @XmlPath("actions/action[@name='Stop']/@URL")
    private URL stopCommandUrl;

    @XmlPath("actions/action[@name='Start']/@URL")
    private URL startCommandUrl;

    @XmlPath("actions/action[@name='Light On']/@URL")
    private URL lightOnCommandUrl;

    @XmlPath("actions/action[@name='Light Off']/@URL")
    private URL lightOffCommandUrl;

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

    public MieleDuration getSmartStartTime() {
        return this.smartStartTime;
    }

    public MieleDuration getRemainingTime() {
        return this.remainingTime;
    }

    public MieleDuration getDuration() {
        return this.duration;
    }

    public MieleDuration getEndTime() {
        return this.endTime;
    }

    public URL getStopCommandUrl() {
        return this.stopCommandUrl;
    }

    public URL getStartCommandUrl() {
        return this.startCommandUrl;
    }

    public URL getLightOnCommandUrl() {
        return this.lightOnCommandUrl;
    }

    public URL getLightOffCommandUrl() {
        return this.lightOffCommandUrl;
    }
}
