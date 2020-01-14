package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.UUID;

//import osh.comdriver.interaction.datatypes.fzi.RestHabiteqDetails;
//import osh.comdriver.interaction.datatypes.fzi.RestRemoteControlDetails;

/**
 * XML serializable
 *
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlRootElement(name = "Device")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestDevice {

    @XmlAttribute(name = "uuid")
    public UUID uuid;

    public RestDeviceMetaDetails deviceMetaDetails;

    public RestGenericParametersDetails genericParametersDetails;

    public RestBusDeviceStatusDetails busDeviceStatusDetails;

    public RestPowerDetails powerDetails;
    public RestElectricityDetails electricityDetails;

    public RestSwitchDetails switchDetails;

    public RestApplianceDetails applianceDetails;

    public RestTemperatureDetails temperatureDetails;
    public RestConfigurationDetails configurationDetails;
    public RestScheduleDetails scheduleDetails;

    //FZI
//	public RestRemoteControlDetails remoteControlDetails;

    public RestTemperatureSensorDetails temperatureSensorDetails;
    public RestLightStateDetails lightStateDetails;
    public RestWindowSensorDetails windowSensorDetails;

//	public RestHabiteqDetails habiteqDetails;


    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }


    public RestDeviceMetaDetails getDeviceMetaDetails() {
        if (this.deviceMetaDetails == null)
            this.deviceMetaDetails = new RestDeviceMetaDetails(null, 0L);
        return this.deviceMetaDetails;
    }

    public boolean hasDeviceMetaDetails() {
        return (this.deviceMetaDetails != null);
    }

    public RestGenericParametersDetails getGenericParametersDetails() {
        if (this.genericParametersDetails == null)
            this.genericParametersDetails = new RestGenericParametersDetails(null, 0L);
        return this.genericParametersDetails;
    }

    public boolean hasGenericParametersDetails() {
        return (this.genericParametersDetails != null);
    }

    public RestBusDeviceStatusDetails getBusDeviceStatusDetails() {
        if (this.busDeviceStatusDetails == null)
            this.busDeviceStatusDetails = new RestBusDeviceStatusDetails(null, 0L);
        return this.busDeviceStatusDetails;
    }


    public RestPowerDetails getPowerDetails() {
        if (this.powerDetails == null)
            this.powerDetails = new RestPowerDetails(null, 0L);
        return this.powerDetails;
    }

    public RestSwitchDetails getSwitchDetails() {
        if (this.switchDetails == null)
            this.switchDetails = new RestSwitchDetails(null, 0L);
        return this.switchDetails;
    }

    public RestElectricityDetails getElectricityDetails() {
        if (this.electricityDetails == null)
            this.electricityDetails = new RestElectricityDetails(null, 0L);
        return this.electricityDetails;
    }

//	public void setElectricityDetails(RestElectricityDetails electricityDetails) {
//		this.electricityDetails = electricityDetails;
//	}

    public RestApplianceDetails getApplianceDetails() {
        if (this.applianceDetails == null)
            this.applianceDetails = new RestApplianceDetails(null, 0L);
        return this.applianceDetails;
    }


    public RestTemperatureDetails getTemperatureDetails() {
        if (this.temperatureDetails == null)
            this.temperatureDetails = new RestTemperatureDetails(null, 0L);
        return this.temperatureDetails;
    }

    public RestConfigurationDetails getConfigurationDetails() {
        if (this.configurationDetails == null)
            this.configurationDetails = new RestConfigurationDetails(null, 0L);
        return this.configurationDetails;
    }

    public RestScheduleDetails getScheduleDetails() {
        if (this.scheduleDetails == null)
            this.scheduleDetails = new RestScheduleDetails(null, 0L);
        return this.scheduleDetails;
    }

    public void setScheduleDetails(RestScheduleDetails scheduleDetails) {
        this.scheduleDetails = scheduleDetails;
    }


    public RestTemperatureSensorDetails getTemperatureSensorDetails() {
        if (this.temperatureSensorDetails == null)
            this.temperatureSensorDetails = new RestTemperatureSensorDetails(null, 0L);
        return this.temperatureSensorDetails;
    }


    public RestLightStateDetails getLightStatusDetails() {
        if (this.lightStateDetails == null)
            this.lightStateDetails = new RestLightStateDetails(null, 0L);
        return this.lightStateDetails;
    }


    public RestWindowSensorDetails getWindowSensorDetails() {
        if (this.windowSensorDetails == null)
            this.windowSensorDetails = new RestWindowSensorDetails(null, 0L);
        return this.windowSensorDetails;
    }


    public RestDevice cloneOnly(List<String> typeNames) {
        RestDevice clone = new RestDevice();
        clone.uuid = this.uuid;
        boolean cloneHasContent = false;

        // RestDeviceMetaDetails deviceMetaDetails
        if (typeNames.contains(RestDeviceMetaDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.deviceMetaDetails = this.deviceMetaDetails;
            if (clone.deviceMetaDetails != null)
                cloneHasContent = true;
        }

        // RestGenericParametersDetails genericParametersDetails
        if (typeNames.contains(RestGenericParametersDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.genericParametersDetails = this.genericParametersDetails;
            if (clone.genericParametersDetails != null)
                cloneHasContent = true;
        }

        // RestBusDeviceStatusDetails busDeviceStatusDetails
        if (typeNames.contains(RestBusDeviceStatusDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.busDeviceStatusDetails = this.busDeviceStatusDetails;
            if (clone.busDeviceStatusDetails != null)
                cloneHasContent = true;
        }

        // RestPowerDetails powerDetails
        if (typeNames.contains(RestPowerDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.powerDetails = this.powerDetails;
            if (clone.powerDetails != null)
                cloneHasContent = true;
        }

        // RestElectricityDetails electricityDetails
        if (typeNames.contains(RestElectricityDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.electricityDetails = this.electricityDetails;
            if (clone.electricityDetails != null)
                cloneHasContent = true;
        }

        // RestSwitchDetails switchDetails
        if (typeNames.contains(RestSwitchDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.switchDetails = this.switchDetails;
            if (clone.switchDetails != null)
                cloneHasContent = true;
        }

        // RestApplianceDetails applianceDetails
        if (typeNames.contains(RestApplianceDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.applianceDetails = this.applianceDetails;
            if (clone.applianceDetails != null)
                cloneHasContent = true;
        }

        // RestTemperatureDetails temperatureDetails
        if (typeNames.contains(RestTemperatureDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.temperatureDetails = this.temperatureDetails;
            if (clone.temperatureDetails != null)
                cloneHasContent = true;
        }

        // RestConfigurationDetails configurationDetails
        if (typeNames.contains(RestConfigurationDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.configurationDetails = this.configurationDetails;
            if (clone.configurationDetails != null)
                cloneHasContent = true;
        }

        // RestScheduleDetails scheduleDetails
        if (typeNames.contains(RestScheduleDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.scheduleDetails = this.scheduleDetails;
            if (clone.scheduleDetails != null)
                cloneHasContent = true;
        }

        // ###########
        // ### FZI ###
        // ###########


        // RestTemperatureSensorDetails temperatureSensorDetails
        if (typeNames.contains(RestTemperatureSensorDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.temperatureSensorDetails = this.temperatureSensorDetails;
            if (clone.temperatureSensorDetails != null)
                cloneHasContent = true;
        }

        // RestLightStateDetails lightStateDetails
        if (typeNames.contains(RestLightStateDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.lightStateDetails = this.lightStateDetails;
            if (clone.lightStateDetails != null)
                cloneHasContent = true;
        }

        // RestWindowSensorDetails windowSensorDetails
        if (typeNames.contains(RestWindowSensorDetails.class.getAnnotation(
                XmlType.class).name())) {
            clone.windowSensorDetails = this.windowSensorDetails;
            if (clone.windowSensorDetails != null)
                cloneHasContent = true;
        }

        if (cloneHasContent)
            return clone;
        else
            return null;
    }
}
