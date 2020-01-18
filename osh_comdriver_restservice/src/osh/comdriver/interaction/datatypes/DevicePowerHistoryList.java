package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "abc")
public class DevicePowerHistoryList {

    @XmlElement(name = "List")
    private List<DevicePowerHistory> listDevicePowerHistory;

    public List<DevicePowerHistory> getListDevicePowerHistory() {
        return this.listDevicePowerHistory;
    }

    public void setListDevicePowerHistory(
            List<DevicePowerHistory> listDevicePowerHistory) {
        this.listDevicePowerHistory = listDevicePowerHistory;
    }


}
