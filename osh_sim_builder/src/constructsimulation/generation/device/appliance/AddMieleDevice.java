package constructsimulation.generation.device.appliance;

import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.simulation.screenplay.ScreenplayType;

import java.util.UUID;

public class AddMieleDevice {

    public static void addMieleDevice(
            EALConfiguration ealConfiguration,

            UUID deviceId,
            DeviceTypes deviceType,
            String profileSource,
            ScreenplayType screenplayType,
            Integer epsOptimizationObjective,

            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass,

            Double averageYearlyRuns,
            String h0Filename,
            String deviceMax1stDof,
            String device2ndDof) {

        AssignedDevice mieleAppliance = CreateMieleDevice.createMieleDevice(
                deviceType,
                DeviceClassification.APPLIANCE,
                deviceId,

                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass,

                profileSource,
                screenplayType,
                epsOptimizationObjective,
                averageYearlyRuns,
                h0Filename,
                deviceMax1stDof,
                device2ndDof);

        ealConfiguration.getAssignedDevices().add(mieleAppliance);
    }

}
