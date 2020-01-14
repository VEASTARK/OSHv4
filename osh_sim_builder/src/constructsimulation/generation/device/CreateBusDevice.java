package constructsimulation.generation.device;

import osh.configuration.eal.AssignedBusDevice;
import osh.configuration.system.BusDeviceTypes;

import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class CreateBusDevice {

    public static AssignedBusDevice createBusDevice(
            BusDeviceTypes deviceType,
            UUID deviceId,
            String comDriverClassName,
            String comManagerClassName) {
        AssignedBusDevice busDevice = new AssignedBusDevice();
        busDevice.setBusDeviceID(deviceId.toString());
        busDevice.setBusDeviceType(deviceType);
        busDevice.setBusDriverClassName(comDriverClassName);
        busDevice.setBusManagerClassName(comManagerClassName);

        return busDevice;
    }

}
