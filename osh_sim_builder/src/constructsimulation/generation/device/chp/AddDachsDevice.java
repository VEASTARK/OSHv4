package constructsimulation.generation.device.chp;

import osh.configuration.eal.EALConfiguration;

import java.util.Map;
import java.util.UUID;


/**
 * @author mauser
 */
public class AddDachsDevice {

    public static void addDachsDevice(
            EALConfiguration ealConfiguration,
            UUID dachsDeviceId,
            String dachsDeviceDriver,
            String dachsDeviceObserver,
            String dachsDeviceController,
            Map<String, String> dachsParams) {

        ealConfiguration.getAssignedDevices().add(
                CreateDachsDevice.createDachsDevice(
                        dachsDeviceId,
                        dachsDeviceDriver,
                        dachsDeviceObserver,
                        dachsDeviceController,
                        dachsParams
                ));
    }


}
