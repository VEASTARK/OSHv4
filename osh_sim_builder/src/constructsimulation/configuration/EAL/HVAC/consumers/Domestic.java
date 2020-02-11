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
    public static UUID domesticUuid = UUIDStorage.dhwUsageUUID;
    public static int[] yearlyDomesticHotWaterEnergyUsed = {
            700,    //1pax
            1400,    //2pax
            2100,    //3pax
            2800,    //4pax
            3500    //5pax
    };
    public static int pastDaysPrediction = GeneralConfig.pastDaysForPrediction;
    public static float weightForOtherWeekday = GeneralConfig.weightForOtherWeekdays;
    public static float weightForSameWeekday = GeneralConfig.weightForSameWeekday;
    public static String vdiDrawOffProfileFileName = FileReferenceStorage.vdi_drawoffProfileFileName;
    public static String weekDayHourProbabilityFileName = FileReferenceStorage.vdi_weekDayHourProbabilityFileName;
    public static String eshlDrawOffProfileFileName = FileReferenceStorage.eshl_drawOffProfileName;
    public static String vdiDriverName = osh.driver.simulation.dhw.VDI6002DomesticHotWaterSimulationDriver.class
            .getName();
    public static String vdiNonControllableObserverName = osh.mgmt.localobserver
            .dhw.VDI6002DomesticHotWaterLocalObserver.class.getName();

    public static String eshlDriverName = osh.driver.simulation.dhw.ESHLDomesticHotWaterSimulationDriver.class
            .getName();
    public static String eshlNonControllableObserverName = osh.mgmt.localobserver
            .dhw.DomesticHotWaterLocalObserver.class.getName();

    /**
     * Generates the configuration file for the domestic-hotwater HVAC-consumers with the set parameters.
     *
     * @return the configuration file for the domestic-hotwater HVAC-consumers
     */
    public static AssignedDevice generateDomestic() {

        HashMap<String, String> params = new HashMap<>();

        params.put("usedcommodities", Arrays.toString(usedCommodities));


        if (!useVDI6002Simulator) {
            params.put("sourcefile", String.format(eshlDrawOffProfileFileName, HouseConfig.personCount));
            params.put("pastDaysPrediction", String.valueOf(pastDaysPrediction));
            params.put("weightForOtherWeekday", String.valueOf(weightForOtherWeekday));
            params.put("weightForSameWeekday", String.valueOf(weightForSameWeekday));
        } else {
            params.put("drawOffTypesFile", vdiDrawOffProfileFileName);
            params.put("weekDayHourProbabilitiesFile", weekDayHourProbabilityFileName);
            params.put("avgYearlyDemamd", String.valueOf(yearlyDomesticHotWaterEnergyUsed[HouseConfig.personCount - 1]));
        }

        if (!params.containsKey("compressionType"))
            params.put("compressionType", GeneralConfig.hvacCompressionType.toString());
        if (!params.containsKey("compressionValue"))
            params.put("compressionValue", String.valueOf(GeneralConfig.hvacCompressionValue));

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
