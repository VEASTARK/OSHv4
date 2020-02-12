package constructsimulation.configuration.EAL.HVAC.storage;

import constructsimulation.configuration.general.GeneralConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.utils.string.ParameterConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for HVAC-storage for coldwater.
 *
 * @author Sebastian Kramer
 */
public class ColdWaterStorage {

    /*

    This class serves as a storage for all default coldwater-storage values and a producer of the finalized config
    DO NOT change anything if you merely wish to produce a new configuration file!

 */
    private static final Commodity[] usedCommodities = {Commodity.COLDWATERPOWER};
    public static UUID coldWaterStorageUuid = UUIDStorage.coldWaterTankUUID;
    public static double tankSize = 1500.0;
    public static double tankDiameter = 0.5;
    public static double initialTemperature = 15.0;
    public static double ambientTemperature = 18.0;
    public static String driverName = osh.driver.simulation.ColdWaterTankSimulationDriver.class.getName();
    public static String nonControllableObserverName = osh.mgmt.localobserver.ColdWaterTankLocalObserver.class.getName();

    /**
     * Generates the configuration file for the coldwater HVAC-storage with the set parameters.
     *
     * @return the configuration file for the coldwater HVAC-storage
     */
    public static AssignedDevice generateColdStorage() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.WaterTank.tankCapacity, String.valueOf(tankSize));
        params.put(ParameterConstants.WaterTank.tankDiameter, String.valueOf(tankDiameter));
        params.put(ParameterConstants.WaterTank.initialTemperature, String.valueOf(initialTemperature));
        params.put(ParameterConstants.WaterTank.ambientTemperature, String.valueOf(ambientTemperature));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.HOTWATERSTORAGE,
                DeviceClassification.HVAC,
                coldWaterStorageUuid,
                driverName,
                nonControllableObserverName,
                false,
                null);

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            dev.getDriverParameters().add(cp);
        }
        return dev;
    }
}
