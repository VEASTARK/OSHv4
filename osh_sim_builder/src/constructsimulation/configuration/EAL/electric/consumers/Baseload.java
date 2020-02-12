package constructsimulation.configuration.EAL.electric.consumers;

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
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.utils.string.ParameterConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for the baseload electric consumer.
 *
 * @author Sebastian Kramer
 */
public class Baseload {

    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER
    };
    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static UUID baseloadUuid = UUIDStorage.baseloadUUID;
    public static int[] avgYearlyBaseloadConsumption =
            // 1p, 2p, ...
            {1426, 2097, 2628, 2993, 3370};
    public static String h0Filename = FileReferenceStorage.h0Filename15Min;
    public static String h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
    public static int pastDaysPrediction = GeneralConfig.pastDaysForPrediction;
    public static float weightForOtherWeekday = GeneralConfig.weightForOtherWeekdays;
    public static float weightForSameWeekday = GeneralConfig.weightForSameWeekday;
    public static double cosPhi = 0.99;
    public static boolean isInductive = true;
    public static LoadProfileCompressionTypes compressionType = GeneralConfig.compressionType;
    public static int compressionValue = GeneralConfig.compressionValue;

    public static String driverName = osh.driver.simulation.BaseloadSimulationDriver.class.getName();

    public static String nonControllableObserverName = osh.mgmt.localobserver.BaseloadLocalObserver.class.getName();

    /**
     * Generates the configuration file for the baseload with the set parameters.
     *
     * @return the configuration file for the baseload
     */
    public static AssignedDevice generateBaseload() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.General_Devices.h0Filename, h0Filename);
        params.put(ParameterConstants.General_Devices.h0Classname, h0ClassName);

        params.put(ParameterConstants.Baseload.yearlyConsumption,
                String.valueOf(avgYearlyBaseloadConsumption[HouseConfig.personCount - 1]));
        params.put(ParameterConstants.Baseload.cosPhi, String.valueOf(cosPhi));
        params.put(ParameterConstants.Baseload.isInductive, String.valueOf(isInductive));

        params.put(ParameterConstants.Prediction.pastDaysPrediction, String.valueOf(pastDaysPrediction));
        params.put(ParameterConstants.Prediction.weightForOtherWeekday, String.valueOf(weightForOtherWeekday));
        params.put(ParameterConstants.Prediction.weightForSameWeekday, String.valueOf(weightForSameWeekday));

        params.put(ParameterConstants.Compression.compressionType, compressionType.toString());
        params.put(ParameterConstants.Compression.compressionValue, String.valueOf(compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.BASELOAD,
                DeviceClassification.BASELOAD,
                baseloadUuid,
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
