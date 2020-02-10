package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;


public class GUIDeviceListStateExchange extends StateExchange {

    private Set<DeviceTableEntry> deviceList;

    public GUIDeviceListStateExchange(UUID sender, ZonedDateTime timestamp, Set<DeviceTableEntry> deviceList) {
        super(sender, timestamp);
        this.deviceList = deviceList;
    }

    public Set<DeviceTableEntry> getDeviceList() {
        return this.deviceList;
    }

}
