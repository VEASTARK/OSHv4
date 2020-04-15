package constructsimulation.configuration.CAL.additional;

import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.device.CreateComDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.comdriver.logging.OSHLoggingComDriver;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for the CAL Logger ComDriver.
 *
 * @author Sebastian Kramer
 */
public class GeneralLogger {

    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static UUID generalLoggerUuid = UUIDStorage.generalLoggerUuid;

    //loggingIntervals for database
    //FORMAT: months, weeks, days
    //only the first non-zero value will be regarded, so {1, 3, 4} is the same as {1, 0, 0} and so on
    public static int[][] loggingIntervals = {
            {0, 0, 1},    // 1 day
            {0, 1, 0},    // 1 week
            {1, 0, 0}    // 1 month
    };

    public static boolean logH0 = false;
    public static boolean logEpsPls = false;
    public static boolean logIntervals = false;
    public static boolean logDevices = false;
    public static boolean logBaseload = false;
    public static boolean logDetailedPower = false;
    public static boolean logThermal = false;
    public static boolean logWaterTank = true;
    public static boolean logEA = true;
    public static boolean logSmartHeater = false;

    public static String driverName = OSHLoggingComDriver.class.getName();
    public static String comManagerName = osh.core.com.DummyComManager.class.getName();

    /**
     * Generates the configuration file for the General Logger ComDriver with the set parameters.
     *
     * @return the configuration file for the General Logger ComDriver
     */
    public static AssignedComDevice generateLogger() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.Logging.logH0, String.valueOf(logH0));
        params.put(ParameterConstants.Logging.logEpsPls, String.valueOf(logEpsPls));
        params.put(ParameterConstants.Logging.logDetailedPower, String.valueOf(logDetailedPower));
        params.put(ParameterConstants.Logging.logIntervals, String.valueOf(logIntervals));
        params.put(ParameterConstants.Logging.logDevices, String.valueOf(logDevices));
        params.put(ParameterConstants.Logging.logBaseload, String.valueOf(logBaseload));
        params.put(ParameterConstants.Logging.logThermal, String.valueOf(logThermal));
        params.put(ParameterConstants.Logging.logWaterTank, String.valueOf(logWaterTank));
        params.put(ParameterConstants.Logging.logEA, String.valueOf(logEA));
        params.put(ParameterConstants.Logging.logSmartHeater, String.valueOf(logSmartHeater));
        params.put(ParameterConstants.Logging.loggingIntervals, Arrays.toString(
                Arrays.stream(loggingIntervals).map(Arrays::toString).toArray(String[]::new)));

        AssignedComDevice dev = CreateComDevice.createComDevice(
                ComDeviceTypes.GENERALLOGGER,
                generalLoggerUuid,
                driverName,
                comManagerName);

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            dev.getComDriverParameters().add(cp);
        }
        return dev;
    }
}
