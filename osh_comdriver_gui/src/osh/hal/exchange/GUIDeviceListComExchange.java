package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.gui.DeviceTableEntry;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class GUIDeviceListComExchange extends CALComExchange {

    private final Set<DeviceTableEntry> deviceList;


    /**
     * CONSTRUCTOR
     */
    public GUIDeviceListComExchange(
            UUID deviceID,
            Long timestamp,
            Set<DeviceTableEntry> deviceList) {
        super(deviceID, timestamp);

        synchronized (deviceList) {
            int devListSize = deviceList.size();
            DeviceTableEntry[] dte = new DeviceTableEntry[devListSize];
            Object[] oa = deviceList.toArray();

            for (int i = 0; i < devListSize; i++) {
                dte[i] = (DeviceTableEntry) oa[i];
            }

            Set<DeviceTableEntry> clonedDeviceList = new HashSet<>();

            for (DeviceTableEntry e : dte) {
                clonedDeviceList.add(e.clone());
            }

            this.deviceList = clonedDeviceList;
        }
    }


    public Set<DeviceTableEntry> getDeviceList() {
        return this.deviceList;
    }

}
