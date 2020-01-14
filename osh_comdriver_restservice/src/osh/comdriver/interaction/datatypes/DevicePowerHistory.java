package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "def")
public class DevicePowerHistory {

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "data")
    private DevicePowerEntry[] data;

    public void setDataLong(Long[][] lData) {

        this.data = new DevicePowerEntry[lData.length];
        for (int i = 0; i < lData.length; i++) {
            this.data[i] = new DevicePowerEntry();
            this.data[i].setItem(lData[i]);
        }

    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DevicePowerEntry[] getData() {
        return this.data;
    }

    public void setData(DevicePowerEntry[] data) {
        this.data = data;
    }


}
