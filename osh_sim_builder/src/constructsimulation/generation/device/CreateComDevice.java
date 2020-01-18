package constructsimulation.generation.device;

import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class CreateComDevice {

    public static AssignedComDevice createComDevice(
            ComDeviceTypes deviceType,
            UUID deviceId,
            String comDriverClassName,
            String comManagerClassName) {
        AssignedComDevice comDevice = new AssignedComDevice();
        comDevice.setComDeviceID(deviceId.toString());
        comDevice.setComDeviceType(deviceType);
        comDevice.setComDriverClassName(comDriverClassName);
        comDevice.setComManagerClassName(comManagerClassName);

        return comDevice;
    }

}
