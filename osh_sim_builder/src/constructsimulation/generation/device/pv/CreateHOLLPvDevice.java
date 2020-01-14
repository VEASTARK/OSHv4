package constructsimulation.generation.device.pv;

import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

public class CreateHOLLPvDevice {

    public static AssignedDevice createRealPvDevice(
            DeviceTypes deviceType,
            DeviceClassification classification,
            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass,

            ScreenplayType screenplayType,

            String nominalPower,
            String complexPowerMax,
            String cosPhiMax,
            String usedCommodities) {

        AssignedDevice _assdev = CreateDevice.createDevice(
                deviceType,
                classification,
                deviceId,
                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass);

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "screenplaytype",
                    "String",
                    "" + screenplayType);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "nominalpower",
                    "String",
                    nominalPower);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "complexpowermax",
                    "String",
                    complexPowerMax);
            _assdev.getDriverParameters().add(cp);
        }

        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "cosphimax",
                    "String",
                    cosPhiMax);
            _assdev.getDriverParameters().add(cp);
        }
        {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    "usedcommodities",
                    "String",
                    usedCommodities);
            _assdev.getDriverParameters().add(cp);
        }
        return _assdev;
    }

}
