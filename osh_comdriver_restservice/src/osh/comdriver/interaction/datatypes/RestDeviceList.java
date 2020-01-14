package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kaibin Bao
 */
@XmlRootElement(name = "DeviceList")
public class RestDeviceList {
    private List<RestDevice> deviceList;

    public RestDeviceList() {
        this.deviceList = new ArrayList<>();
    }

    @XmlElement(name = "Device")
    public List<RestDevice> getDeviceList() {
        if (this.deviceList == null)
            this.deviceList = new ArrayList<>();
        return this.deviceList;
    }

    public void setDeviceList(List<RestDevice> device) {
        this.deviceList = device;
    }

    public void add(RestDevice device) {
        this.deviceList.add(device);
    }
}
