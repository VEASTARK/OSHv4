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

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for HVAC-storage for hotwater.
 *
 * @author Sebastian Kramer
 */
public class HotWaterStorage {

    /*

        This class serves as a storage for all default hotwater-storage values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */

    private static final Commodity[] usedCommodities = {
            Commodity.HEATINGHOTWATERPOWER,
            Commodity.DOMESTICHOTWATERPOWER
    };
    public static UUID hotWaterStorageUuid = UUIDStorage.hotWaterTankUUID;
    public static double tankSize = 350.0;
    public static Duration newIPPAfter = GeneralConfig.newIppAfter;
    public static double triggerIfDeltaTemp = 0.25;
    public static double tankDiameter = 0.5;
    public static double initialTemperature = 70.0;
    public static double ambientTemperature = 20.0;
    public static String driverName = osh.driver.simulation.HotWaterTankSimulationDriver.class.getName();
    public static String nonControllableObserverName = osh.mgmt.localobserver.HotWaterTankLocalObserver.class.getName();

    /**
     * Generates the configuration file for the hotwater HVAC-storage with the set parameters.
     *
     * @return the configuration file for the hotwater HVAC-storage
     */
    public static AssignedDevice generateHotStorage() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));
        params.put(ParameterConstants.WaterTank.tankCapacity, String.valueOf(tankSize));
        params.put(ParameterConstants.WaterTank.tankDiameter, String.valueOf(tankDiameter));
        params.put(ParameterConstants.WaterTank.initialTemperature, String.valueOf(initialTemperature));
        params.put(ParameterConstants.WaterTank.ambientTemperature, String.valueOf(ambientTemperature));
        params.put(ParameterConstants.IPP.newIPPAfter, String.valueOf(newIPPAfter.toSeconds()));
        params.put(ParameterConstants.IPP.triggerIppIfDeltaTemp, String.valueOf(triggerIfDeltaTemp));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.HOTWATERSTORAGE,
                DeviceClassification.HVAC,
                hotWaterStorageUuid,
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
