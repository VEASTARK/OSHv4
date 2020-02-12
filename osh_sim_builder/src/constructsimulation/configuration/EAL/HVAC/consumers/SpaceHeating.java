package constructsimulation.configuration.EAL.HVAC.consumers;

import constructsimulation.configuration.general.FileReferenceStorage;
import constructsimulation.configuration.general.GeneralConfig;
import constructsimulation.configuration.general.HouseConfig;
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
 * Generator and default configuration storage for HVAC-consumers for space-heating.
 *
 * @author Sebastian Kramer
 */
public class SpaceHeating {

    private static final Commodity[] usedCommodities = {Commodity.HEATINGHOTWATERPOWER};
    /*

        This class serves as a storage for all default space-heating values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */
    public static UUID heatingUuid = UUIDStorage.spaceHeatingUUID;
    public static int pastDaysPrediction = GeneralConfig.pastDaysForPrediction;
    public static float weightForOtherWeekday = GeneralConfig.weightForOtherWeekdays;
    public static float weightForSameWeekday = GeneralConfig.weightForSameWeekday;
    public static String sourceFileName = FileReferenceStorage.eshl_heatingDrawOffFileName;
    public static String driverName = osh.driver.simulation.heating.ESHLSpaceHeatingSimulationDriver.class
            .getName();
    public static String nonControllableObserverName = osh.mgmt.localobserver
            .heating.SpaceHeatingLocalObserver.class.getName();

    /**
     * Generates the configuration file for the space-heating HVAC-consumers with the set parameters.
     *
     * @return the configuration file for the space-heating HVAC-consumers
     */
    public static AssignedDevice generateHeating() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.WaterDemand.sourceFile, String.format(sourceFileName, HouseConfig.personCount));
        params.put(ParameterConstants.Prediction.pastDaysPrediction, String.valueOf(pastDaysPrediction));
        params.put(ParameterConstants.Prediction.weightForOtherWeekday, String.valueOf(weightForOtherWeekday));
        params.put(ParameterConstants.Prediction.weightForSameWeekday, String.valueOf(weightForSameWeekday));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.hvacCompressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.hvacCompressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.SPACEHEATING,
                DeviceClassification.HVAC,
                heatingUuid,
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
