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
 * Generator and default configuration storage for the gas-heating HVAC-producer.
 *
 * @author Sebastian Kramer
 */
public class GasHeating {

    /*

        This class serves as a storage for all gas-heating default values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */

    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER,
            Commodity.HEATINGHOTWATERPOWER,
            Commodity.NATURALGASPOWER
    };
    public static UUID gasUuid = UUIDStorage.gasHeatingUUID;
    public static double minTemperature = 60.0;
    public static double maxTemperature = 80.0;
    public static int maxHotWaterPower = 15000; //15 kW
    public static int maxGasPower = 15000; //15 kW

    public static Duration newIppAfter = GeneralConfig.newIppAfter;

    public static int typicalActivePowerOn = 100; //W
    public static int typicalActivePowerOff = 0; //W
    public static int typicalReactivePowerOn = 0; //W
    public static int typicalReactivePowerOff = 0; //W

    public static String driverClassName = osh.driver.simulation.GasBoilerSimulationDriver.class.getName();
    public static String observerClassName = osh.mgmt.localobserver.NonControllableGasBoilerLocalObserver.class.getName();

    /**
     * Generates the configuration file for the gas-heating HVAC-producer with the set parameters.
     *
     * @return the configuration file for the gas-heating HVAC-producer
     */
    public static AssignedDevice generateGas() {
        Map<String, String> params = new HashMap<>();
        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.GasBoiler.maxGasPower, String.valueOf(maxGasPower));
        params.put(ParameterConstants.IPP.newIPPAfter, String.valueOf(newIppAfter.toSeconds()));
        params.put(ParameterConstants.GasBoiler.activePowerOn, String.valueOf(typicalActivePowerOn));
        params.put(ParameterConstants.GasBoiler.activePowerOff, String.valueOf(typicalActivePowerOff));
        params.put(ParameterConstants.GasBoiler.reactivePowerOn, String.valueOf(typicalReactivePowerOn));
        params.put(ParameterConstants.GasBoiler.reactivePowerOff, String.valueOf(typicalReactivePowerOff));

        params.put(ParameterConstants.GasBoiler.hotWaterStorageMinTemp, String.valueOf(minTemperature));
        params.put(ParameterConstants.GasBoiler.hotWaterStorageMaxTemp, String.valueOf(maxTemperature));

        params.put(ParameterConstants.GasBoiler.maxHotWaterPower, String.valueOf(maxHotWaterPower));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.GASHEATING,
                DeviceClassification.HVAC,
                gasUuid,
                driverClassName,
                observerClassName,
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
