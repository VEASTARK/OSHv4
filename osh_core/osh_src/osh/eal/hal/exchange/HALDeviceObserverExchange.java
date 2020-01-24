package osh.eal.hal.exchange;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.registry.details.common.DeviceMetaDriverDetails;
import osh.eal.hal.interfaces.common.IHALDeviceMetaDetails;

import java.util.UUID;

/**
 * Please remember cloning!
 *
 * @author Florian Allerding, Ingo Mauser
 */
public class HALDeviceObserverExchange
        extends HALObserverExchange
        implements IHALDeviceMetaDetails {

    private String name;
    private String location;

    private DeviceTypes deviceType;
    private DeviceClassification deviceClass;

    private boolean configured;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public HALDeviceObserverExchange(UUID deviceID, long timestamp) {
        super(deviceID, timestamp);
    }


    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    public void setDeviceType(DeviceTypes deviceType) {
        this.deviceType = deviceType;
    }

    public DeviceClassification getDeviceClassification() {
        return this.deviceClass;
    }

    public void setDeviceClass(DeviceClassification deviceClass) {
        this.deviceClass = deviceClass;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public void setDeviceMetaDetails(DeviceMetaDriverDetails deviceMetaDetails) {
        this.name = deviceMetaDetails.getName();
        this.location = deviceMetaDetails.getLocation();

        this.deviceType = deviceMetaDetails.getDeviceType();
        this.deviceClass = deviceMetaDetails.getDeviceClassification();

        this.configured = deviceMetaDetails.isConfigured();
    }

}
