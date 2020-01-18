package constructsimulation.generation.device.pv;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

public class AddESHLPVDevice {

    public static void addRealPvDevice(
            EALConfiguration ealConfiguration,

            DeviceTypes deviceType,
            DeviceClassification classification,
            UUID deviceId,

            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass,

            ScreenplayType screenplayType,

            String nominalPower,
            String usedCommodities) {


        AssignedDevice device = CreateESHLPvDevice.createRealPvDevice(
                deviceType,
                classification,
                deviceId,
                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass,
                screenplayType,
                nominalPower,
                usedCommodities);

        ealConfiguration.getAssignedDevices().add(device);

    }

}
