package constructsimulation.configuration.CAL.signals;

import constructsimulation.configuration.general.FileReferenceStorage;
import constructsimulation.configuration.general.GeneralConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.datatypes.EPSTypes;
import constructsimulation.generation.device.CreateComDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.*;

/**
 * Generator and default configuration storage for the CAL energy-pricer-signals (eps).
 *
 * @author Sebastian Kramer
 */
public class EPS {

    public static final EnumMap<AncillaryCommodity, Double> priceMap = new EnumMap<>(AncillaryCommodity.class);
    public static final EnumMap<AncillaryCommodity, String> nameMap = new EnumMap<>(AncillaryCommodity.class);
    /*

       This class serves as a storage for all default values and a producer of the finalized config
       DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static final UUID epsUuid = UUIDStorage.epsSignalUuid;
    public static final String h0Filename = FileReferenceStorage.h0Filename15Min;
    public static final String h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
    // Multi-Commodity
    public static EPSTypes epsType = EPSTypes.MC_FLAT; // FLAT-GERMAN-2015-TARIFF
    //EPS.ALL
    public static final Duration newSignalAfter = GeneralConfig.newSignalAfter;
    public static final Duration signalPeriod = GeneralConfig.signalPeriod;
    //EPS.FLAT & EPS.MC_FLAT & EPS.H0 & EPS.WIK...
    public static final int resolutionOfPriceSignal = 900; //15 minutes
    public static final int signalConstantPeriod = 900; //15 minutes
    //EPS.MC_FLAT & EPS.WIK...
    public static AncillaryCommodity[] ancillaryCommodities = {
            AncillaryCommodity.ACTIVEPOWEREXTERNAL,
            AncillaryCommodity.REACTIVEPOWEREXTERNAL,
            AncillaryCommodity.PVACTIVEPOWERFEEDIN,
            AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
            AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
            AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,

            AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN,
            AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION,
            AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION,

            AncillaryCommodity.NATURALGASPOWEREXTERNAL,
    };
    //EPS.WIK... (see Liebe et al. (2015))
    public static final String[] wikWeekDayPrices = {
            "31.85, 31.85, 31.85, 31.85, 31.85, 22.86, 22.86", // 2015
            "31.73, 31.73, 31.73, 31.73, 31.73, 22.86, 22.86", // 2020
            "34.52, 34.52, 34.52, 34.52, 34.52, 22.86, 22.86", // 2025 (else)
    };
    // (see Liebe et al. (2015))
    public static final String[] wikHourlyPrices = {
            "0=24.50,21600=32.87,43200=37.15,46800=29.55,61200=32.87,68400=29.55,79200=24.50", // 2015
            "0=24.42,21600=32.77,43200=37.04,46800=29.46,61200=32.77,68400=29.46,79200=24.42", // 2020
            "0=26.10,21600=35.01,43200=39.58,46800=31.48,61200=35.01,68400=31.48,79200=26.10", // 2025
            "0=25.10,21600=33.66,43200=38.06,46800=30.27,61200=33.66,68400=30.27,79200=25.10", // thesis
    };
    // IMA Thesis
    public static final String[] hourlyPrices = {
            "0=20.0,7200=40.0,14400=20.0,21600=40.0,28800=20.0,36000=40.0,43200=20.0"
                    + ",50400=40.0,57600=20.0,64800=40.0,72000=20.0,79200=40.0",
            "0=30.0,32400=15.0,36000=30.0",
            "0=30.0,43200=15.0,46800=30.0",
            "0=10.0,3600=50.0,7200=10.0,10800=50.0,14400=10.0,18000=50.0,21600=10.0,25200=50.0,"
                    + "28800=10.0,32400=50.0,36000=10.0,39600=50.0,43200=10.0,46800=50.0,50400=10.0,"
                    + "54000=50.0,57600=10.0,61200=50.0,64800=10.0,68400=50.0,72000=10.0,75600=50.0,79200=10.0,82800=50.0"
    };
    //EPS.H0
    public static final double activePowerExternalSupplyMin = 10.0;
    public static final double activePowerExternalSupplyAvg = 30.0; // intended average tariff
    public static final double activePowerExternalSupplyMax = 50.0;
    //EPS.CSV
    public static final String csvPriceDynamicFilePath = FileReferenceStorage.csvPriceDynamicFilePath;
    //EPS.PVFEEDIN
    public static final String pvFeedInEPEXFilePath = FileReferenceStorage.pvFeedInEPEXFilePath;
    //EPS.EEX
    public static final EnumMap<EPSTypes, String> driverMap = new EnumMap<>(EPSTypes.class);
    public static final String comManager = osh.mgmt.commanager.EpsProviderComManager.class.getName();

    //default prices
    static {
        //electricity
        priceMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, 30.0);
        nameMap.put(AncillaryCommodity.ACTIVEPOWEREXTERNAL, ParameterConstants.EPS.activePrice);
        priceMap.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, 0.0);
        nameMap.put(AncillaryCommodity.REACTIVEPOWEREXTERNAL, ParameterConstants.EPS.reactivePrice);

        //production
        priceMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, 10.0);
        nameMap.put(AncillaryCommodity.PVACTIVEPOWERFEEDIN, ParameterConstants.EPS.pvFeedInPrice);
        priceMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, 0.0);
        nameMap.put(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, ParameterConstants.EPS.pvAutoConsumptionPrice);

        priceMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, 9.0);
        nameMap.put(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, ParameterConstants.EPS.chpFeedInPrice);
        priceMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, 5.0);
        nameMap.put(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, ParameterConstants.EPS.chpAutoConsumptionPrice);

        priceMap.put(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, 0.0);
        nameMap.put(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN, ParameterConstants.EPS.batteryFeedInPrice);
        priceMap.put(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, 0.0);
        nameMap.put(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION, ParameterConstants.EPS.batteryAutoConsumptionPrice);
        priceMap.put(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, 0.0);
        nameMap.put(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION, ParameterConstants.EPS.batteryConsumptionPrice);

        priceMap.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, 8.0);
        nameMap.put(AncillaryCommodity.NATURALGASPOWEREXTERNAL, ParameterConstants.EPS.gasPrice);
    }

    static {
        driverMap.put(EPSTypes.MC_FLAT, osh.comdriver.McFlatEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.H0, osh.comdriver.H0EpsFziProviderComDriver.class.getName());
        driverMap.put(EPSTypes.CSV, osh.comdriver.CsvEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.PVFEEDIN, osh.comdriver.FlexiblePVEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.REMS, osh.rems.simulation.RemsEpsProviderComDriver.class.getName());

        driverMap.put(EPSTypes.WIKHOURLY2015, osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.WIKHOURLY2020, osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.WIKHOURLY2025, osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.WIK_BASED_THESIS, osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.HOURLY_ALTERNATING, osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName());

        driverMap.put(EPSTypes.WIKWEEKDAY2015, osh.comdriver.WIKWeekDayBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.WIKWEEKDAY2020, osh.comdriver.WIKWeekDayBasedEpsProviderComDriver.class.getName());
        driverMap.put(EPSTypes.WIKWEEKDAY2025, osh.comdriver.WIKWeekDayBasedEpsProviderComDriver.class.getName());
    }

    /**
     * Generates the configuration file for the eps with the set parameters.
     *
     * @return the configuration file for the eps
     */
    public static AssignedComDevice generateEps() {
        Map<String, String> params = new HashMap<>();

        params.put(ParameterConstants.Signal.newSignal, String.valueOf(newSignalAfter.toSeconds()));
        params.put(ParameterConstants.Signal.signalPeriod, String.valueOf(signalPeriod.toSeconds()));
        params.put(ParameterConstants.EPS.resolution, String.valueOf(resolutionOfPriceSignal));
        params.put(ParameterConstants.Signal.signalConstantPeriod, String.valueOf(signalConstantPeriod));

        for (AncillaryCommodity ac : ancillaryCommodities) {
            params.put(nameMap.get(ac), String.valueOf(priceMap.get(ac)));
        }

        params.put(ParameterConstants.EPS.ancillaryCommodities, Arrays.toString(ancillaryCommodities));

        if (epsType == EPSTypes.H0) {
            params.put(ParameterConstants.General_Devices.h0Filename, h0Filename);
            params.put(ParameterConstants.General_Devices.h0Classname, h0ClassName);
            params.put(ParameterConstants.EPS.activePriceSupplyMin, String.valueOf(activePowerExternalSupplyMin));
            params.put(ParameterConstants.EPS.activePriceSupplyAvg, String.valueOf(activePowerExternalSupplyAvg));
            params.put(ParameterConstants.EPS.activePriceSupplyMax, String.valueOf(activePowerExternalSupplyMax));
        } else if (epsType == EPSTypes.CSV) {
            params.put(ParameterConstants.EPS.filePathPriceSignal, csvPriceDynamicFilePath);
        } else if (epsType == EPSTypes.PVFEEDIN) {
            params.put(ParameterConstants.EPS.filePathPVPriceSignal, pvFeedInEPEXFilePath);
        } else if (epsType == EPSTypes.WIKHOURLY2015 || epsType == EPSTypes.WIKHOURLY2020
                || epsType == EPSTypes.WIKHOURLY2025 || epsType == EPSTypes.WIK_BASED_THESIS
                || epsType == EPSTypes.HOURLY_ALTERNATING) {

            String activePrices = null;

            switch (epsType) {
                case WIKHOURLY2015: {
                    activePrices = wikHourlyPrices[0];
                    break;
                }
                case WIKHOURLY2020: {
                    activePrices = wikHourlyPrices[1];
                    break;
                }
                case WIKHOURLY2025: {
                    activePrices = wikHourlyPrices[2];
                    break;
                }
                case WIK_BASED_THESIS: {
                    activePrices = wikHourlyPrices[3];
                    break;
                }
                case HOURLY_ALTERNATING: {
                    activePrices = hourlyPrices[0];
                    break;
                }
                default: {
                }
            }

            params.put(ParameterConstants.EPS.activePriceArray, activePrices);
        } else if (epsType == EPSTypes.WIKWEEKDAY2015
                || epsType == EPSTypes.WIKWEEKDAY2020
                || epsType == EPSTypes.WIKWEEKDAY2025) {

            String activePrices = epsType == EPSTypes.WIKWEEKDAY2015 ? wikWeekDayPrices[0] :
                    (epsType == EPSTypes.WIKWEEKDAY2020 ? wikWeekDayPrices[1] :
                            wikWeekDayPrices[2]);

            params.put(ParameterConstants.EPS.activePriceArray, activePrices);
        }

        AssignedComDevice dev = CreateComDevice.createComDevice(
                ComDeviceTypes.MULTI_COMMODITY,
                epsUuid,
                driverMap.get(epsType),
                comManager);

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
