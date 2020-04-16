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
 * Generator and default configuration storage for the combined-heating-plant (chp) HVAC-producer.
 *
 * @author Sebastian Kramer
 */
public class CHP {

    /*

        This class serves as a storage for all default chp values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */

    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER,
            Commodity.HEATINGHOTWATERPOWER,
            Commodity.NATURALGASPOWER
    };
    public static boolean controllableCHP = true;
    //IDs
    public static final UUID chpUuid = UUIDStorage.chpUUID;
    public static final UUID hotWaterTankUuid = UUIDStorage.hotWaterTankUUID;
    public static final int typicalActivePower = -5500;
    public static final int typicalThermalPower = -12500;
    public static final int typicalAdditionalThermalPower;
    public static final int typicalGasPower = 20500;
    public static final double cosPhi = 0.9;
    public static final Duration rescheduleAfter = GeneralConfig.rescheduleAfter;
    public static final Duration newIPPAfter = GeneralConfig.newIppAfter;
    public static final Duration relativeHorizonIPP = GeneralConfig.relativeHorizon;
    public static final double currentHotWaterStorageMinTemp = 60;
    public static final double currentHotWaterStorageMaxTemp = 80;
    public static final double forcedOnHysteresis = 5.0;
    public static final int bitsPerSlot = 4;
    public static final long timePerSlot = 5 * 60;
    public static final double fixedCostPerStart = 8.0;
    public static final double forcedOnOffStepMultiplier = 0.1;
    public static final int forcedOffAdditionalCost = 10;
    public static final double onCervisiaStepSizeMultiplier = 0.0000001;
    public static final Duration minRuntime = Duration.ofMinutes(15); //no short runtimes
    public static final String driverName = osh.driver.simulation.DachsChpSimulationDriver.class.getName();

    public static final String controllableObserverName = osh.mgmt.localobserver.DachsChpLocalObserver.class.getName();
    public static final String controllableControllerName = osh.mgmt.localcontroller.DachsChpLocalController.class.getName();

    public static final String nonControllableObserverName = osh.mgmt.localobserver.NonControllableDachsChpLocalObserver.class.getName();
    public static final String nonControllableControllerName = osh.mgmt.localcontroller.NonControllableDachsChpLocalController.class.getName();

    /**
     * Generates the configuration file for the chp HVAC-producer with the set parameters.
     *
     * @return the configuration file for the chp HVAC-producer
     */
    public static AssignedDevice generateCHP() {
        Map<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.CHP.activePower, String.valueOf(typicalActivePower));
        params.put(ParameterConstants.CHP.thermalPower, String.valueOf(typicalThermalPower));
        params.put(ParameterConstants.CHP.additionalThermalPower, String.valueOf(typicalAdditionalThermalPower));
        params.put(ParameterConstants.CHP.gasPower, String.valueOf(typicalGasPower));
        params.put(ParameterConstants.CHP.hotWaterTankUUID, String.valueOf(hotWaterTankUuid));
        params.put(ParameterConstants.IPP.rescheduleAfter, String.valueOf(rescheduleAfter.toSeconds()));
        params.put(ParameterConstants.IPP.newIPPAfter, String.valueOf(newIPPAfter.toSeconds()));
        params.put(ParameterConstants.IPP.relativeHorizon, String.valueOf(relativeHorizonIPP.toSeconds()));
        params.put(ParameterConstants.TemperatureRestrictions.hotWaterStorageMinTemp, String.valueOf(currentHotWaterStorageMinTemp));
        params.put(ParameterConstants.TemperatureRestrictions.hotWaterStorageMaxTemp, String.valueOf(currentHotWaterStorageMaxTemp));
        params.put(ParameterConstants.TemperatureRestrictions.forcedOnHysteresis, String.valueOf(forcedOnHysteresis));
        params.put(ParameterConstants.CHP.cosPhi, String.valueOf(cosPhi));
        params.put(ParameterConstants.IPP.bitsPerSlot, String.valueOf(bitsPerSlot));
        params.put(ParameterConstants.IPP.timePerSlot, String.valueOf(timePerSlot));
        params.put(ParameterConstants.CHP.fixedCostPerStart, String.valueOf(fixedCostPerStart));
        params.put(ParameterConstants.CHP.forcedOnOffStepMultiplier, String.valueOf(forcedOnOffStepMultiplier));
        params.put(ParameterConstants.CHP.forcedOffAdditionalCost, String.valueOf(forcedOffAdditionalCost));
        params.put(ParameterConstants.CHP.cervisiaStepSizeMultiplier, String.valueOf(onCervisiaStepSizeMultiplier));
        params.put(ParameterConstants.CHP.minRuntime, String.valueOf(minRuntime.toSeconds()));

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.CHPPLANT,
                DeviceClassification.CHPPLANT,
                chpUuid,
                driverName,
                controllableCHP ? controllableObserverName : nonControllableObserverName,
                true,
                controllableCHP ? controllableControllerName : nonControllableControllerName);

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
