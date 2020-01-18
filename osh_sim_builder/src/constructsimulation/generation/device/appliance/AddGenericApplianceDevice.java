package constructsimulation.generation.device.appliance;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class AddGenericApplianceDevice {

    public static void addGenericApplianceDevice(
            EALConfiguration ealConfiguration,

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

        AssignedDevice genericAppliance = CreateGenericAppliance.createGenericApplianceDevice(
                deviceType,
                classification, deviceId,
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
                usedCommodities);
        ealConfiguration.getAssignedDevices().add(genericAppliance);

    }
}
