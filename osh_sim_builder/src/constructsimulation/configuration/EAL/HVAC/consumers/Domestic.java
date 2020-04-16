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
 * Generator and default configuration storage for HVAC-consumers for domestic-hotwater.
 *
 * @author Sebastian Kramer
 */
public class Domestic {

    /*

        This class serves as a storage for all default chp values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

     */

    private static final Commodity[] usedCommodities = {Commodity.DOMESTICHOTWATERPOWER};
    public static boolean useVDI6002Simulator = true;
    public static final UUID domesticUuid = UUIDStorage.dhwUsageUUID;
    public static final int[] yearlyDomesticHotWaterEnergyUsed = {
            700,    //1pax
            1400,    //2pax
            2100,    //3pax
            2800,    //4pax
            3500    //5pax
    };
    public static final int pastDaysPrediction = GeneralConfig.pastDaysForPrediction;
    public static final float weightForOtherWeekday = GeneralConfig.weightForOtherWeekdays;
    public static final float weightForSameWeekday = GeneralConfig.weightForSameWeekday;
    public static final String vdiDrawOffProfileFileName = FileReferenceStorage.vdi_drawoffProfileFileName;
    public static final String weekDayHourProbabilityFileName = FileReferenceStorage.vdi_weekDayHourProbabilityFileName;
    public static final String eshlDrawOffProfileFileName = FileReferenceStorage.eshl_drawOffProfileName;
    public static final String vdiDriverName = osh.driver.simulation.dhw.VDI6002DomesticHotWaterSimulationDriver.class
            .getName();
    public static final String vdiNonControllableObserverName = osh.mgmt.localobserver
            .dhw.VDI6002DomesticHotWaterLocalObserver.class.getName();

    public static final String eshlDriverName = osh.driver.simulation.dhw.ESHLDomesticHotWaterSimulationDriver.class
            .getName();
    public static final String eshlNonControllableObserverName = osh.mgmt.localobserver
            .dhw.DomesticHotWaterLocalObserver.class.getName();

    /**
     * Generates the configuration file for the domestic-hotwater HVAC-consumers with the set parameters.
     *
     * @return the configuration file for the domestic-hotwater HVAC-consumers
     */
    public static AssignedDevice generateDomestic() {

        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities));


        if (!useVDI6002Simulator) {
            params.put(ParameterConstants.WaterDemand.sourceFile, String.format(eshlDrawOffProfileFileName,
                    HouseConfig.personCount));
            params.put(ParameterConstants.Prediction.pastDaysPrediction, String.valueOf(pastDaysPrediction));
            params.put(ParameterConstants.Prediction.weightForOtherWeekday, String.valueOf(weightForOtherWeekday));
            params.put(ParameterConstants.Prediction.weightForSameWeekday, String.valueOf(weightForSameWeekday));
        } else {
            params.put(ParameterConstants.WaterDemand.drawOffFile, vdiDrawOffProfileFileName);
            params.put(ParameterConstants.WaterDemand.probabilitiesFile, weekDayHourProbabilityFileName);
            params.put(ParameterConstants.WaterDemand.averageYearlyDemand,
                    String.valueOf(yearlyDomesticHotWaterEnergyUsed[HouseConfig.personCount - 1]));
        }

        if (!params.containsKey(ParameterConstants.Compression.compressionType))
            params.put(ParameterConstants.Compression.compressionType, GeneralConfig.hvacCompressionType.toString());
        if (!params.containsKey(ParameterConstants.Compression.compressionValue))
            params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.hvacCompressionValue));

        AssignedDevice dev = CreateDevice.createDevice(
                DeviceTypes.DOMESTICHOTWATER,
                DeviceClassification.HVAC,
                domesticUuid,
                useVDI6002Simulator ? vdiDriverName : eshlDriverName,
                useVDI6002Simulator ? vdiNonControllableObserverName : eshlNonControllableObserverName,
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
