package constructsimulation.configuration.CAL.additional;

import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.device.CreateComDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for the CAL Energy Logger.
 *
 * @author Sebastian Kramer
 */
public class EnergyLogger {

    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static final UUID energyLoggerUuid = UUIDStorage.energyLoggerUuid;

    public static final String driverName = osh.comdriver.logging.EnergyLoggingComDriver.class.getName();
    public static final String comManagerName = osh.mgmt.commanager.EnergyLoggingComManager.class.getName();

    /**
     * Generates the configuration file for the Energy Logger with the set parameters.
     *
     * @return the configuration file for the Energy Logger
     */
    public static AssignedComDevice generateEnergyLogger() {
        Map<String, String> params = new HashMap<>();

        AssignedComDevice dev = CreateComDevice.createComDevice(
                ComDeviceTypes.GENERALLOGGER,
                energyLoggerUuid,
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
