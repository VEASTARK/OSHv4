package constructsimulation.generation.device.appliance;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import constructsimulation.generation.utility.AddAssignedDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class CreateGenericAppliance {

    public static AssignedDevice createGenericApplianceDevice(
            DeviceTypes deviceType,
            DeviceClassification classification,
            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass,

            ScreenplayType screenplayType,
            String deviceMax1stDof,
            String device2ndDof,
            double averageYearlyRuns,
            String h0Filename,
            String h0Classname,
            String probabilityFileName,
            String configurationShares,
            String profileSource,
            String usedCommodities) {
        return createGenericApplianceDevice(
                deviceType,
                classification,
                deviceId,
                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass,
                screenplayType,
                deviceMax1stDof,
                device2ndDof,
                averageYearlyRuns,
                h0Filename,
                h0Classname,
                probabilityFileName,
                configurationShares,
                profileSource,
                usedCommodities,
                new HashMap<>());
    }

    public static AssignedDevice createGenericApplianceDevice(
            DeviceTypes deviceType,
            DeviceClassification classification,
            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass,

            ScreenplayType screenplayType,
            String deviceMax1stDof,
            String device2ndDof,
            double averageYearlyRuns,
            String h0Filename,
            String h0Classname,
            String probabilityFileName,
            String configurationShares,
            String profileSource,
            String usedCommodities,
            Map<String, String> deviceParams) {

        AssignedDevice _assdev = CreateDevice.createDevice(
                deviceType,
                classification,
                deviceId,
                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass);

        if (!deviceParams.containsKey("compressionType"))
            deviceParams.put("compressionType", AddAssignedDevice.compressionType.toString());
        if (!deviceParams.containsKey("compressionValue"))
            deviceParams.put("compressionValue", String.valueOf(AddAssignedDevice.compressionValue));

        // add parameters for driver

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "screenplaytype",
                    "String",
                    "" + screenplayType);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "devicemax1stdof",
                    "String",
                    deviceMax1stDof);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "device2nddof",
                    "String",
                    "" + device2ndDof);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "averageyearlyruns",
                    "String",
                    "" + averageYearlyRuns);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "h0filename",
                    "String",
                    h0Filename);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "h0classname",
                    "String",
                    h0Classname);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "probabilityfilename",
                    "String",
                    probabilityFileName);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "configurationshares",
                    "String",
                    configurationShares);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "profilesource",
                    "String",
                    profileSource);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "usedcommodities",
                    "String",
                    usedCommodities);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "compressionType",
                    "String",
                    AddAssignedDevice.compressionType.toString());
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "compressionValue",
                    "String",
                    String.valueOf(AddAssignedDevice.compressionValue));
            _assdev.getDriverParameters().add(cp);
        }

        return _assdev;
    }
}
