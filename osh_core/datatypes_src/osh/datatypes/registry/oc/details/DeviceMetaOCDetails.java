package osh.datatypes.registry.oc.details;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.registry.StateExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class DeviceMetaOCDetails extends StateExchange implements Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -6362041571466196750L;
    protected String name;
    protected String location;

    protected DeviceTypes deviceType;
    protected DeviceClassification deviceClassification;

    protected boolean configured;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public DeviceMetaOCDetails(UUID sender, long timestamp) {
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

    @Override
    public String toString() {
        return "Device \"" + this.name + "\" in " + this.location + " type: " + this.deviceType + "(" + this.deviceClassification + ")";
    }

    @Override
    public DeviceMetaOCDetails clone() {
        long uuidSenderLSB = this.getSender().getLeastSignificantBits();
        long uuidSenderMSB = this.getSender().getMostSignificantBits();
        DeviceMetaOCDetails clone = new DeviceMetaOCDetails(new UUID(uuidSenderMSB, uuidSenderLSB), this.getTimestamp());

        clone.name = this.name;
        clone.location = this.location;

        clone.deviceType = this.deviceType;
        clone.deviceClassification = this.deviceClassification;

        clone.configured = this.configured;

        return clone;
    }

}
