package constructsimulation.configuration.EAL.HVAC.producers;

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
 * Generator and default configuration storage for the insert-heating-element (ihe) HVAC-producer.
 *
 * @author Sebastian Kramer
 */
public class IHE {

    /*

        This class serves as a storage for all default ihe values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */

    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER,
            Commodity.HEATINGHOTWATERPOWER
    };
    public static UUID iheUuid = UUIDStorage.iheUUID;
    public static double maxHotWaterTemperature = 80;
    public static Duration newIPPAfter = GeneralConfig.newIppAfter;
    public static double triggerIfDeltaTempBigger = 1.0;
    public static String driverName = osh.driver.simulation.SmartHeaterSimulationDriver.class.getName();
    public static String nonControllableObserverName = osh.mgmt.localobserver.SmartHeaterLocalObserver.class.getName();

    /**
     * Generates the configuration file for the ihe HVAC-producer with the set parameters.
     *
     * @return the configuration file for the ihe HVAC-producer
     */
    public static AssignedDevice generateIHE() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.IHE.temperatureSetting, String.valueOf(maxHotWaterTemperature));
        params.put(ParameterConstants.IPP.newIPPAfter, String.valueOf(newIPPAfter));
        params.put(ParameterConstants.IPP.triggerIppIfDeltaTemp, String.valueOf(triggerIfDeltaTempBigger));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.INSERTHEATINGELEMENT,
                DeviceClassification.HVAC,
                iheUuid,
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
