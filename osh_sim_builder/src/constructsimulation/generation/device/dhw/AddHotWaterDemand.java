package constructsimulation.generation.device.dhw;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class AddHotWaterDemand {

    public static void addWaterHeatingDevice(
            EALConfiguration ealConfiguration,

            DeviceTypes deviceType,
            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            String localControllerClass,

            ScreenplayType screenplayType,

            String sourceFile,
            String usedCommodities) {


        AssignedDevice device = CreateHotWaterDemand.createHotWaterTank(
                deviceType,
                deviceId,
                driverClassName,
                localObserverClass,
                localControllerClass,
                screenplayType,
                sourceFile,
                usedCommodities);

        ealConfiguration.getAssignedDevices().add(device);

    }

}
