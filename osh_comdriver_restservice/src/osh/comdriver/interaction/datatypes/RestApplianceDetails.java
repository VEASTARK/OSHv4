package osh.comdriver.interaction.datatypes;

import osh.en50523.EN50523DeviceState;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlType(name = "applianceDetails")
public class RestApplianceDetails extends RestStateDetail {

    protected long startTime;

    protected long endTime;

    protected int remainingTime;

    protected int expectedProgramDuration;

    protected String programName;

    protected String phaseName;

    @Enumerated(value = EnumType.STRING)
    protected EN50523DeviceState state;

    protected String stateTextDE;

    protected Map<String, String> programExtras;

    protected Map<String, String> actions;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public RestApplianceDetails() {
        this(null, 0);
    }

    public RestApplianceDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
        this.programExtras = new HashMap<>();
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

    public int getExpectedProgramDuration() {
        return this.expectedProgramDuration;
    }

    public void setExpectedProgramDuration(int expectedProgramDuration) {
        this.expectedProgramDuration = expectedProgramDuration;
    }

    public String getStateTextDE() {
        return this.stateTextDE;
    }

    public void setStateTextDE(String stateTextDE) {
        this.stateTextDE = stateTextDE;
    }

    public EN50523DeviceState getState() {
        return this.state;
    }

    public void setState(EN50523DeviceState state) {
        this.state = state;
        this.stateTextDE = state.getDescriptionDE();
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

    public Map<String, String> getProgramExtras() {
        return this.programExtras;
    }

    public void setProgramExtras(Map<String, String> programExtras) {
        this.programExtras = programExtras;
    }

    public String getProgramExtra(String key) {
        return this.programExtras.get(key);
    }

    public void setProgramExtra(String key, String value) {
        this.programExtras.put(key, value);
    }

    public Map<String, String> getActions() {
        return this.actions;
    }

    public void setActions(Map<String, String> actions) {
        this.actions = actions;
    }

    public int getRemainingTime() {
        return this.remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }


}
