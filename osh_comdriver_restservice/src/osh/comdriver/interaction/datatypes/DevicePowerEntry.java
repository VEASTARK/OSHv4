package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "def")
public class DevicePowerEntry {

    @XmlElement(name = "item")
    private Long[] item;

    public Long[] getItem() {
        return this.item;
    }

    public void setItem(Long[] item) {
        this.item = item;
    }

}
