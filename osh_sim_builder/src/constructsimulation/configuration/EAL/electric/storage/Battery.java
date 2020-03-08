package constructsimulation.configuration.EAL.electric.storage;

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
 * Generator and default configuration storage for the battery electric storage.
 *
 * @author Sebastian Kramer
 */
public class Battery {

    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER
    };

    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static UUID batteryUuid = UUIDStorage.batteryUUID;
    public static int batteryCycles = 10000;
    //storage Model
    public static int batteryType = 1;
    public static int minChargingState = 0;
    public static int maxChargingState = 5 * 1000 * 3600; // [Ws]
    public static int minDischargePower = -3500; // [W]
    public static int maxDischargePower = -100;
    public static int minChargePower = 100; // [W]
    public static int maxChargePower = 3500;
    public static int minInverterPower = -1000000000; // [W]
    public static int maxInverterPower = 1000000000;
    public static int roomTemperature = 20;
    public static Duration rescheduleAfter = GeneralConfig.rescheduleAfter;
    public static Duration newIppAfter = GeneralConfig.newIppAfter;
    public static int triggerIppIfDeltaSoCBigger = 5000;
    public static String driverName = osh.driver.simulation.NonControllableBatterySimulationDriver.class.getName();
    public static String nonControllableObserverName = osh.mgmt.localobserver.NonControllableInverterBatteryStorageObserver.class
            .getName();

    /**
     * Generates the configuration file for the battery electric storage with the set parameters.
     *
     * @return the configuration file for the battery electric storage
     */
    public static AssignedDevice generateBattery() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.Battery.minChargingState, String.valueOf(minChargingState));
        params.put(ParameterConstants.Battery.maxChargingState, String.valueOf(maxChargingState));
        params.put(ParameterConstants.Battery.minDischargingPower, String.valueOf(minDischargePower));
        params.put(ParameterConstants.Battery.maxDischargingPower, String.valueOf(maxDischargePower));
        params.put(ParameterConstants.Battery.minChargingPower, String.valueOf(minChargePower));
        params.put(ParameterConstants.Battery.maxChargingPower, String.valueOf(maxChargePower));
        params.put(ParameterConstants.Battery.minInverterPower, String.valueOf(minInverterPower));
        params.put(ParameterConstants.Battery.maxInverterPower, String.valueOf(maxInverterPower));

        params.put(ParameterConstants.IPP.newIPPAfter, String.valueOf(newIppAfter.toSeconds()));
        params.put(ParameterConstants.IPP.triggerIppIfDeltaSoc, String.valueOf(triggerIppIfDeltaSoCBigger));
        params.put(ParameterConstants.IPP.rescheduleAfter, String.valueOf(rescheduleAfter.toSeconds()));
        params.put(ParameterConstants.Battery.batteryCycle, String.valueOf(batteryCycles));
        params.put(ParameterConstants.Battery.batteryType, String.valueOf(batteryType));
        params.put(ParameterConstants.Battery.roomTemperature, String.valueOf(roomTemperature));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.BATTERYSTORAGE,
                DeviceClassification.BATTERYSTORAGE,
                batteryUuid,
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
