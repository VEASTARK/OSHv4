package osh.comdriver.interaction.datatypes;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

@XmlType(name = "deviceMetaDetails")
public class RestDeviceMetaDetails extends RestStateDetail {

    protected String name;
    protected String location;

    protected DeviceTypes deviceType;
    protected DeviceClassification deviceClassification;

    protected boolean configured;

    protected String icon;

    /**
     * for JAXB
     */
    @Deprecated
    public RestDeviceMetaDetails() {
        this(null, 0);
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public RestDeviceMetaDetails(UUID sender, long timestamp) {
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


}
