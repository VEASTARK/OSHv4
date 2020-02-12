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
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.*;

/**
 * Generator and default configuration storage for the household-appliances electric consumer.
 *
 * @author Sebastian Kramer
 */
public class Appliances {

    private static final DeviceTypes[] deviceTypes = {
            DeviceTypes.DISHWASHER,
            DeviceTypes.INDUCTIONCOOKTOP,
            DeviceTypes.ELECTRICSTOVE,
            DeviceTypes.DRYER,
            DeviceTypes.WASHINGMACHINE
    };
    private static final Commodity[][][] usedCommodities = {
            //DISHWASHER
            {
                    null,  //NONE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER}
            },
            //COOKTOP
            {
                    null,   //NONE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    null,   //DELAYABLE
                    null,   //INTERRUPTIBLE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.NATURALGASPOWER},
                    null,   //HYBRID_DELAYABLE
                    null,   //HYBRID_INTERRUPTIBLE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.NATURALGASPOWER}
            },
            //OVEN
            {
                    null,   //NONE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    null,   //DELAYABLE
                    null,   //INTERRUPTIBLE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.NATURALGASPOWER},
                    null,   //HYBRID_DELAYABLE
                    null,   //HYBRID_INTERRUPTIBLE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.NATURALGASPOWER}
            },
            //TUMBLE_DRYER
            {
                    null,   //NONE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER}
            },
            //WASHINGMACHINE
            {
                    null,   //NONE
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER},
                    {Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER, Commodity.HEATINGHOTWATERPOWER}
            }
    };
    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static ApplianceType[] applianceTypesToUse = {
            ApplianceType.DELAYABLE,     //DISHWASHER
            ApplianceType.STANDARD,     //COOKTOP
            ApplianceType.STANDARD,     //STOVE
            ApplianceType.DELAYABLE,     //TUMBLE_DRYER
            ApplianceType.DELAYABLE      //WASHINGMACHINE
    };
    public static UUID[] applianceUuids = UUIDStorage.applianceUUID;
    public static String h0FilePath = FileReferenceStorage.h0Filename15Min;
    public static String h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
    public static boolean useSecondTDof = false;
    public static Duration[] firstTDOF = {
            Duration.ofHours(12),
            Duration.ZERO,
            Duration.ZERO,
            Duration.ofHours(12),
            Duration.ofHours(12)
    };
    public static Duration[] secondTDOF = {
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO,
            Duration.ZERO
    };

//    NONE,
//    STANDARD,
//    DELAYABLE,
//    INTERRUPTIBLE,
//    HYBRID,
//    HYBRID_DELAYABLE,
//    HYBRID_INTERRUPTIBLE,
//    HYBRID_SINGLE
    /**
     * sources:<br>
     * DESTATIS Fachserie 15 Reihe 2, etc........
     */
    public static int[][] averageYearlyRuns = {
            // 1p, 	2p, 	...
            /* DW */  {90, 160, 240, 310, 340},
            /* IH */ {170, 300, 350, 400, 420},
            /* OV */ {85, 150, 175, 200, 210},
            /* TD */ {80, 140, 210, 270, 280},
            /* WM */ {120, 200, 280, 360, 420}
    };

    public static double[][] configurationShares = {
            // 0	1    2   ...
            /* DW */ {0.2, 0.3, 0.3, 0.2},
            /* IH */ {0.4, 0.4, 0.2},
            /* OV */ {0.4, 0.4, 0.2},
            /* TD */ {0.0, 0.2, 0.8},
            /* WM */ {0.2, 0.5, 0.3}
    };

    public static String[] probabilityFilePaths = FileReferenceStorage.probabilityFilePaths;
    public static String[][] profileSourcesPaths = FileReferenceStorage.profileSourcesPaths;

    public static String driverName = osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName();
    public static String observerName = osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName();
    public static String controllerName = osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName();

    /**
     * Generates the configuration file for the household-appliances with the set parameters.
     *
     * @return the configuration file for the household-appliances
     */
    public static List<AssignedDevice> generateAppliances() {
        List<AssignedDevice> list = new ArrayList<>();

        for (int i = 0; i < applianceTypesToUse.length; i++) {
            ApplianceType type = applianceTypesToUse[i];

            boolean isControllable = type == ApplianceType.DELAYABLE || type == ApplianceType.INTERRUPTIBLE
                    || type == ApplianceType.HYBRID_DELAYABLE || type == ApplianceType.HYBRID_INTERRUPTIBLE;

            assert (i != 2 || i != 3 || !isControllable);

            HashMap<String, String> params = new HashMap<>();

            params.put(ParameterConstants.General_Devices.usedCommodities, Arrays.toString(usedCommodities[i][type.ordinal()]));
            params.put(ParameterConstants.Appliances.firstTDoF, String.valueOf(isControllable ? firstTDOF[i].toSeconds() : 0));
            params.put(ParameterConstants.Appliances.secondTDof, String.valueOf((isControllable && useSecondTDof) ?
                    secondTDOF[i].toSeconds() : 0));

            params.put(ParameterConstants.Appliances.averageYearlyRuns,
                    String.valueOf(averageYearlyRuns[i][HouseConfig.personCount - 1]));
            params.put(ParameterConstants.General_Devices.h0Filename, h0FilePath);
            params.put(ParameterConstants.General_Devices.h0Classname, h0ClassName);

            params.put(ParameterConstants.Appliances.probabilityFile, probabilityFilePaths[i]);
            params.put(ParameterConstants.General_Devices.profileSource, profileSourcesPaths[i][type.ordinal()]);
            params.put(ParameterConstants.Appliances.configurationShares, Arrays.toString(configurationShares[i]));

            if (!params.containsKey(ParameterConstants.Compression.compressionType))
                params.put(ParameterConstants.Compression.compressionType, GeneralConfig.compressionType.toString());
            if (!params.containsKey(ParameterConstants.Compression.compressionValue))
                params.put(ParameterConstants.Compression.compressionValue, String.valueOf(GeneralConfig.compressionValue));


            AssignedDevice dev = CreateDevice.createDevice(
                    deviceTypes[i],
                    DeviceClassification.APPLIANCE,
                    applianceUuids[i],
                    driverName,
                    observerName,
                    true,
                    controllerName);

            for (Map.Entry<String, String> en : params.entrySet()) {
                ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                        en.getKey(),
                        "String",
                        en.getValue());
                dev.getDriverParameters().add(cp);
            }
            list.add(dev);
        }
        return list;
    }
}
