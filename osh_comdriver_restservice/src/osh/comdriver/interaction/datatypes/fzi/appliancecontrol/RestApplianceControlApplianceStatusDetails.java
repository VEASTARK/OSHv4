package osh.comdriver.interaction.datatypes.fzi.appliancecontrol;

import javax.xml.bind.annotation.XmlType;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlType(name = "switchOffapplianceDetails")
public class RestApplianceControlApplianceStatusDetails {

    protected boolean on;

    /**
     * for JAXB
     */
    public RestApplianceControlApplianceStatusDetails() {
        //NOTHING
    }

    public boolean isOn() {
        return this.on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

}
