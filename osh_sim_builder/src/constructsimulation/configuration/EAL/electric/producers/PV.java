package constructsimulation.configuration.EAL.electric.producers;

import constructsimulation.configuration.general.FileReferenceStorage;
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
 * Generator and default configuration storage for the pv electric producer.
 *
 * @author Sebastian Kramer
 */
public class PV {

    private static final double profileNominalPowerHOLL = 5307.48; // positive value! [sic!]
    private static final Commodity[] usedCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER
    };
    /*

        This class serves as a storage for all default values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */
    public static boolean usePVHOLL = true;
    public static boolean usePVEv0 = false;
    public static UUID pvUuid = UUIDStorage.pvUUID;

    public static int wattsPeak = 4000;
    public static int pastDaysPrediction = GeneralConfig.pastDaysForPrediction;

    public static String hollFilePath = FileReferenceStorage.holl_filePath;
    public static String hollFileExtension = ".csv";
    public static String ev0FilePath = FileReferenceStorage.ev0_filePath;
    public static String ev0FileExtension = ".csv";

    public static int pvComplexPowerMax = 10000;
    public static double pvCosPhiMax = -0.8;
    public static String hollDriverName = osh.driver.simulation.PvSimulationDriverHollData.class.getName();
    public static String ev0DriverName = osh.driver.simulation.PvSimulationDriverEv0.class.getName();

    public static String nonControllableObserverName = osh.mgmt.localobserver.PvLocalObserver.class.getName();
    public static String nonControllableControllerName = osh.mgmt.localcontroller.PvLocalController.class.getName();

    /**
     * Generates the configuration file for the pv with the set parameters.
     *
     * @return the configuration file for the pv
     */
    public static AssignedDevice generatePV() {
        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));

        params.put(ParameterConstants.PV.nominalPower, String.valueOf(wattsPeak));
        params.put(ParameterConstants.Prediction.pastDaysPrediction, String.valueOf(pastDaysPrediction));
        params.put(ParameterConstants.PV.complexPowerMax, String.valueOf(pvComplexPowerMax));
        params.put(ParameterConstants.PV.cosPhiMax, String.valueOf(pvCosPhiMax));

        if (usePVHOLL) {
            params.put(ParameterConstants.PV.profileNominalPower, String.valueOf(profileNominalPowerHOLL));
            params.put(ParameterConstants.General_Devices.filePath, hollFilePath);
            params.put(ParameterConstants.General_Devices.fileExtension, hollFileExtension);
        } else {
            params.put(ParameterConstants.General_Devices.profileSource, ev0FilePath + ev0FileExtension);
        }

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.PVSYSTEM,
                DeviceClassification.PVSYSTEM,
                pvUuid,
                usePVHOLL ? hollDriverName : ev0DriverName,
                nonControllableObserverName,
                false,
                nonControllableControllerName);

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
