package constructsimulation.generation.device.smartplug;

import constructsimulation.generation.device.CreateDevice;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class CreateSmartPlug {

    public static AssignedDevice createSmartPlug(
            UUID deviceId,
            DeviceTypes deviceType,
            DeviceClassification deviceClassification,
            String driverClassName,
            String localObserverClassName,
            String localControllerClassName,
            boolean isControllable) {

        return CreateDevice.createDevice(
                deviceType,
                deviceClassification,
                deviceId,
                driverClassName,
                localObserverClassName,
                isControllable,
                localControllerClassName);
    }

}
