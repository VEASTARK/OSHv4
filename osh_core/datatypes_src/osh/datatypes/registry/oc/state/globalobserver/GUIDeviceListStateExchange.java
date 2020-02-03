package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class GUIDeviceListStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 9104434585663750101L;
    private Set<DeviceTableEntry> deviceList;

    public GUIDeviceListStateExchange(UUID sender, ZonedDateTime timestamp, Set<DeviceTableEntry> deviceList) {
        super(sender, timestamp);
        this.deviceList = deviceList;
    }

    public Set<DeviceTableEntry> getDeviceList() {
        return this.deviceList;
    }

}
