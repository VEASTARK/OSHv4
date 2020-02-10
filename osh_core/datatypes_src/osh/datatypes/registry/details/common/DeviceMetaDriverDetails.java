package osh.datatypes.registry.details.common;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;

public class DeviceMetaDriverDetails extends StateExchange {

    protected String name;
    protected String location;

    protected DeviceTypes deviceType;
    protected DeviceClassification deviceClassification;

    protected boolean configured;

    protected String icon;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public DeviceMetaDriverDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
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
        return this.deviceClassification;
    }

    public void setDeviceClassification(DeviceClassification deviceClassification) {
        this.deviceClassification = deviceClassification;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }


    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Device \"" + this.name + "\" in " + this.location + " type: " + this.deviceType + "(" + this.deviceClassification + ")";
    }

    @Override
    public DeviceMetaDriverDetails clone() {
        long uuidSenderLSB = this.getSender().getLeastSignificantBits();
        long uuidSenderMSB = this.getSender().getMostSignificantBits();
        DeviceMetaDriverDetails clone = new DeviceMetaDriverDetails(new UUID(uuidSenderMSB, uuidSenderLSB), this.getTimestamp());

        clone.name = this.name;
        clone.location = this.location;

        clone.deviceType = this.deviceType;
        clone.deviceClassification = this.deviceClassification;

        clone.configured = this.configured;

        clone.icon = this.icon;

        return clone;
    }

}
