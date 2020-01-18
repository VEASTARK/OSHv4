package constructsimulation.generation.device.old.waterheating;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@Deprecated
public class AddWaterHeatingDevice {

    public static void addWaterHeatingDevice(
            EALConfiguration ealConfiguration,

            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            boolean isControllable, // not used
            String localControllerClass,

            ScreenplayType screenplayType,

            String[] pvNominalPower) {


        AssignedDevice device = CreateWaterHeatingDevice.createWaterHeatingDevice(
                deviceId,
                driverClassName,
                localObserverClass,
                localControllerClass,
                screenplayType,
                pvNominalPower);

        ealConfiguration.getAssignedDevices().add(device);

    }

}
