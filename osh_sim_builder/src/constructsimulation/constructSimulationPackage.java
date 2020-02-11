package constructsimulation;

import constructsimulation.data.BuildingConfiguration;
import constructsimulation.data.OldSimOSHConfigurationData;
import constructsimulation.datatypes.*;
import constructsimulation.generation.ConstructSimulationHelper;
import constructsimulation.generation.utility.AddAssignedComDevice;
import constructsimulation.generation.utility.AddAssignedDevice;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.utils.xml.XMLSerialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Ingo Mauser, Jan Mueller, Sebastian Kramer
 */
public class constructSimulationPackage {

    /* #######################
     * # important variables #
     * ####################### */

    public static final SimulationPackage simPackage = new SimulationPackage();

    //LoadCompression
    // General values, if you want specialized Values add them to the device configuration parameter
    public static LoadProfileCompressionTypes compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
    public static int compressionValue = 100; //Watt

    //public static PLSTypes plsType = PLSTypes.Normal;
    public static PLSTypes plsType = PLSTypes.NORMAL;

    //H0-Configuration
    public static final String h0ClassName = osh.utils.slp.H0Profile15Minutes.class.getName();
//	public static String h0ClassName = osh.utils.slp.H0Profile1Hour.class.getName();

    public static final String defaultLogPath = "logs";

    // ELECTRICITY
//	public static EPSTypes epsType = EPSTypes.H0;
//	public static EPSTypes epsType = EPSTypes.STEPS; // HOCH_TIEF
//	public static EPSTypes epsType = EPSTypes.PVFEEDIN;
//	public static EPSTypes epsType = EPSTypes.CSV;
    // MC
//	public static EPSTypes epsType = EPSTypes.MC_FLAT; // FLAT-GERMAN-2015-TARIFF
//	public static EPSTypes epsType = EPSTypes.WIKHOURLY2015; // WIK-2015 Hourly Based Tariff
//	public static EPSTypes epsType = EPSTypes.WIKHOURLY2020; // WIK-2015 Hourly Based Tariff
    public static EPSTypes epsType = EPSTypes.WIKHOURLY2025; // WIK-2015 Hourly Based Tariff
//	public static EPSTypes epsType = EPSTypes.WIKWEEKDAY2015; // WIK-2015 Hourly Based Tariff
//	public static EPSTypes epsType = EPSTypes.WIKWEEKDAY2020; // WIK-2015 Hourly Based Tariff
//	public static EPSTypes epsType = EPSTypes.WIKWEEKDAY2025; // WIK-2015 Hourly Based Tariff
//	public static EPSTypes epsType = EPSTypes.WIK_BASED_THESIS; // WIK based Tariff in Thesis
//	public static EPSTypes epsType = EPSTypes.HOURLY_FLUCTUATING; // fluctuating between 2 prices every 2 hours

    //EPS.ALL
    public static final int newSignalAfterThisPeriod = 43200; // 12 hours
    public static final int signalPeriod = 129600; // 36 hours

    //EPS.FLAT & EPS.MC_FLAT & EPS.H0 & EPS.WIK...
    public static final int signalConstantPeriod = 900; //15 minutes

    //EPS.MC_FLAT
    public static final double activePowerPrice = 30.0;
    public static double reactivePowerPrice;

    //EPS.MC_FLAT & EPS.WIK...
    public static final AncillaryCommodity[] ancillaryCommodities = {
            AncillaryCommodity.ACTIVEPOWEREXTERNAL,
            AncillaryCommodity.REACTIVEPOWEREXTERNAL,
            AncillaryCommodity.NATURALGASPOWEREXTERNAL,
            AncillaryCommodity.PVACTIVEPOWERFEEDIN,
            AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
            AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
            AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION
    };

    //EPS.WIK...
    public static final String[] wikWeekDayPrices = {
            "31.85, 31.85, 31.85, 31.85, 31.85, 22.86, 22.86",
            "31.73, 31.73, 31.73, 31.73, 31.73, 22.86, 22.86",
            "34.52, 34.52, 34.52, 34.52, 34.52, 22.86, 22.86",
    };

    public static final String[] wikHourlyPrices = {
            "0=24.50,21600=32.87,43200=37.15,46800=29.55,61200=32.87,68400=29.55,79200=24.50",
            "0=24.42,21600=32.77,43200=37.04,46800=29.46,61200=32.77,68400=29.46,79200=24.42",
            "0=26.10,21600=35.01,43200=39.58,46800=31.48,61200=35.01,68400=31.48,79200=26.10",
            "0=25.10,21600=33.66,43200=38.06,46800=30.27,61200=33.66,68400=30.27,79200=25.10", // thesis
    };

    public static final String[] hourlyPrices = {
            "0=20.0,7200=40.0,14400=20.0,21600=40.0,28800=20.0,36000=40.0,43200=40.0"
                    + ",50400=20.0,57600=40.0,64800=20.0,72000=40.0",
    };

    //EPS.MC_FLAT & EPS.H0 & EPS.CSV
    public static final double activePowerFeedInPV = 10.0;
    public static final double activePowerFeedInCHP = 9.0;
    public static final double naturalGasPowerPrice = 8.0;
    public static double activePowerAutoConsumptionPV;
    public static final double activePowerAutoConsumptionCHP = 5.0;

    //EPS.H0
    public static final double activePowerExternalSupplyMin = 10.0;
    public static final double activePowerExternalSupplyAvg = 30.0;
    public static final double activePowerExternalSupplyMax = 50.0;

    //EPS.CSV
//	public static int resolutionOfPriceSignal = 3600; //1 hour
//	public static String filePathPriceSignal = "configfiles/externalSignal/priceDynamic.csv";

    public static final int resolutionOfPriceSignal = 900; //15 min
    //	 min=7.09, avg=28, max=42.77
    public static final String filePathPriceSignal = "configfiles/externalSignal/FILE.csv";


    //	public static int resolutionOfPriceSignal = 3600; //60 min
    // min=?, avg=?, max=?
    public static final String filePathActivePowerFeedInPVPriceSignal = "configfiles/externalSignal/pricePVFeedInEPEX.csv";


//	public static int resolutionOfPriceSignal = 900; //15 min
//	// min=3.56, avg=24.47, max=39.24
//	public static String filePathPriceSignal = "configfiles/externalSignal/FILE.csv";


    //PLS
    public static final int activeLowerLimit = -3000;
    public static final int activeUpperLimit = 3000;
    public static final int reactiveLowerLimit = -3000;
    public static final int reactiveUpperLimit = 3000;


    //	public static int numberOfPersons = 1;
//	public static int numberOfPersons = 2;
//	public static int numberOfPersons = 3;
    public static int numberOfPersons = 4;
//	public static int numberOfPersons = 5;

    //loggingIntervals for database
//	//FORMAT: months, weeks, days
//	//only the first non-zero value will be regarded, so {1, 3, 4} is the same as {1, 0, 0} and so on
    public static final int[][] loggingIntervals = {
            {0, 0, 1},    // 1 day
            {0, 1, 0},    // 1 week
            {1, 0, 0}    // 1 month
    };
    //if should do aggregated logging for H0
    public static boolean logH0;
    public static boolean logEpsPls;
    public static boolean logIntervals;
    public static boolean logDevices = true;
//	static boolean useMaxEvaluations = false;
    public static boolean logDetailedPower;
    public static boolean logHotWater;
//	static boolean useMinDeltaFitness = false;
    public static boolean logWaterTank = true;
    public static boolean logGA = true;
    public static boolean logSmartHeater;
    /**
     * Show GUI (cruisecontrol)
     */
    public static boolean showGui = true;
    /**
     * Use SimulationLogger
     */
//	static boolean simLogger = true;
    static boolean simLogger;
    /**
     * Liebherr Freezer
     */
    static boolean useLiebherrFreezer;

    //TODO later
    static boolean intelligentLiebherrControl;
    static String comPort = "COM1";
    static final String configFilesPath = BuildingConfiguration.configFilesPath;


    /* ########################
     * # system configuration #
     * ######################## */
    static final String systemPath = BuildingConfiguration.systemPath;
    static final String simulationPath = BuildingConfiguration.simulationPath;
    static final String screenplayFileName = BuildingConfiguration.screenplayFileName;
    static String descriptorFileName = BuildingConfiguration.descriptorFileName;
    static final String iDeviceScreenplayDirectory = BuildingConfiguration.screenplayMielePath;
    static final String ealConfigFileName = BuildingConfiguration.EALConfigFileName;
    static final String calConfigFileName = BuildingConfiguration.calConfigFileName;
    static final String oshConfigFileName = BuildingConfiguration.oshConfigFileName;
    // Configuration files for baseload simulation and PV simulation
    static final String h0Filename = BuildingConfiguration.h0Filename15Min;
//	static String h0Filename = HouseholdConfiguration.h0Filename1Hour;
    static final String ocConfigFileName = BuildingConfiguration.ocConfigFileName;

    // EPS-Provider-Configuration
    static String epsProviderComDriverClass;

    // ### ComDrivers ###
    static final String epsProviderComManagerClass = BuildingConfiguration.epsComManagerClass;
    static UUID epsProviderUUID = BuildingConfiguration.comDeviceIdEPS;
    // PLS-Provider-Configuration
    static String plsProviderComDriverClass;
    static final String plsProviderComManagerClass = BuildingConfiguration.plsComManagerClass;
    static final UUID plsProviderUUID = BuildingConfiguration.comDeviceIdPLS;
    // GUI-Configuration
    static final String guiComDriverClass = BuildingConfiguration.guiComDriverClass;
    static final String guiComManagerClass = BuildingConfiguration.guiComManagerClass;



    //INFO: For Observer/Controller configuration see below and ComplexSmartHomeDeviceConfig

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    public static void main(String[] args) {
        long startTimeStamp = ZonedDateTime.now().toEpochSecond();

        constructSimulationPackage c = new constructSimulationPackage();
        constructSimulationPackage.generate(configFilesPath + "simulationPackages/" + startTimeStamp + "/");
    }

    /**
     * Let's generate...
     *
     */
    public static void generate(String packagesFilePath) {

        AddAssignedDevice.compressionType = constructSimulationPackage.compressionType;
        AddAssignedDevice.compressionValue = constructSimulationPackage.compressionValue;

        Random rand = new Random(simPackage.getInitialRandomSeed());

        System.out.println("[" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] Generation " +
                "started");
//		long startTimeStamp = 0; //always use same directory

        //check package path
        File fPackagePath = new File(packagesFilePath);

        if (fPackagePath.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do {
                System.out.println();
                System.out.println("ERROR: package already exists. Delete (y/n)?");
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (!line.equals("y") && !line.equals("n"));

            if (line.equals("y")) {
                deleteDirectory(fPackagePath);
                if (fPackagePath.exists()) throw new RuntimeException("It still exists!");
            } else {
                System.out.println("Aborting...");
                System.exit(1);
            }
        }

        // create paths
        fPackagePath.mkdirs();
        File fSimulation = new File(packagesFilePath + simulationPath);
        fSimulation.mkdir();
        File fSystem = new File(packagesFilePath + systemPath);
        fSystem.mkdir();

//		// write descriptor
//		try {
//			XMLSerialization.marshal2File(
//					packagePath + descriptorFileName, simPackage);
//		} catch (Exception e2) {
//			e2.printStackTrace();
//		}		

        // iterate to generate
        for (int currentOptimizationAlgorithm : simPackage.getOptimizationAlgorithms()) {
            for (int currentEpsOptimizationObjective : simPackage.getEPSOptimizationObjectives()) {
                for (int currentPlsOptimizationObjective : simPackage.getPLSOptimizationObjectives()) {
                    for (int currentNumberOfPersons : simPackage.getDynamicScreenplayArguments().getNumPersons()) {
                        for (int currentNumberOfRun = 0; currentNumberOfRun < simPackage.getNumberOfRuns(); currentNumberOfRun++) {

                            long currentRandomSeed;
                            if (simPackage.isAlwaysNewRandomSeed()) {
                                currentRandomSeed = rand.nextLong();
                            } else {
                                currentRandomSeed = simPackage.getInitialRandomSeed();
                            }

                            plsProviderComDriverClass = osh.comdriver.FlatPlsProviderComDriver.class.getName();

                            EALConfigurationWrapper eALConfigurationData =
                                    new EALConfigurationWrapper(
                                            currentNumberOfPersons,
                                            h0Filename,
                                            simPackage.isSimulationResultLogging());


                            CALConfigurationWrapper calConfigurationData = new CALConfigurationWrapper(
                                    showGui,
                                    guiComDriverClass,
                                    guiComManagerClass
                            );

                            OSHConfigurationWrapper oshConfigurationData = new OSHConfigurationWrapper(
                                    currentNumberOfPersons,
                                    currentRandomSeed,
                                    defaultLogPath,
                                    BuildingConfiguration.meterUUID,
                                    null
                            );

                            ScreenplayWrapper screenplayData;
                            if (simPackage.getStaticScreenplayArguments() != null) {
                                screenplayData =
                                        new ScreenplayWrapper(
                                                simPackage.getScreenplayType(),
                                                iDeviceScreenplayDirectory,
                                                simPackage.getStaticScreenplayArguments().getPriceCurveDuration(),
                                                simPackage.getStaticScreenplayArguments().getChosenPriceCurve());
                            } else {
                                screenplayData =
                                        new ScreenplayWrapper(
                                                simPackage.getScreenplayType(),
                                                iDeviceScreenplayDirectory,
                                                0,
                                                0);
                            }


                            SimulationConfigurationWrapper simConfig =
                                    ConstructSimulationHelper.constructSimulation(
                                            eALConfigurationData,
                                            calConfigurationData,
                                            null,
                                            oshConfigurationData,
                                            screenplayData);


                            Map<String, String> epsParams = new HashMap<>();

                            epsParams.put("newSignalAfterThisPeriod", String.valueOf(newSignalAfterThisPeriod));
                            epsParams.put("signalPeriod", String.valueOf(signalPeriod));


                            // add communication device for the EPS (EVU/DSM/utility)
                            if (epsType == EPSTypes.MC_FLAT) {
                                epsProviderComDriverClass = osh.comdriver.McFlatEpsProviderComDriver.class.getName();
                                epsParams.put("activePowerPrice", String.valueOf(activePowerPrice));
                                epsParams.put("reactivePowerPrice", String.valueOf(reactivePowerPrice));
                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
                                epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("signalConstantPeriod", String.valueOf(signalConstantPeriod));
                                epsParams.put("ancillaryCommodities", Arrays.toString(ancillaryCommodities));
                            } else if (epsType == EPSTypes.H0) {
                                epsProviderComDriverClass = osh.comdriver.H0EpsFziProviderComDriver.class.getName();
                                epsParams.put("h0Filename", h0Filename);
                                epsParams.put("h0Classname", h0ClassName);
                                epsParams.put("activePowerExternalSupplyMin", String.valueOf(activePowerExternalSupplyMin));
                                epsParams.put("activePowerExternalSupplyAvg", String.valueOf(activePowerExternalSupplyAvg));
                                epsParams.put("activePowerExternalSupplyMax", String.valueOf(activePowerExternalSupplyMax));
                                epsParams.put("activePowerAutoConsumptionPV", String.valueOf(activePowerAutoConsumptionPV));
                                epsParams.put("activePowerAutoConsumptionCHP", String.valueOf(activePowerAutoConsumptionCHP));
                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
                                epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("signalConstantPeriod", String.valueOf(signalConstantPeriod));
                            } else if (epsType == EPSTypes.CSV) {
                                epsProviderComDriverClass = osh.comdriver.CsvEpsProviderComDriver.class.getName();
                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
                                epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("activePowerAutoConsumptionPV", String.valueOf(activePowerAutoConsumptionPV));
                                epsParams.put("activePowerAutoConsumptionCHP", String.valueOf(activePowerAutoConsumptionCHP));
                                epsParams.put("resolutionOfPriceSignal", String.valueOf(resolutionOfPriceSignal));
                                epsParams.put("ancillaryCommodities", Arrays.toString(ancillaryCommodities));
                                epsParams.put("filePathPriceSignal", filePathPriceSignal);
                            } else if (epsType == EPSTypes.PVFEEDIN) {
                                epsProviderComDriverClass = osh.comdriver.FlexiblePVEpsProviderComDriver.class.getName();
                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
//				epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("resolutionOfPriceSignal", String.valueOf(resolutionOfPriceSignal));
                                epsParams.put("filePathActivePowerFeedInPVPriceSignal", filePathActivePowerFeedInPVPriceSignal);
                                epsParams.put("ancillaryCommodities", Arrays.toString(ancillaryCommodities));
                            } else if (epsType == EPSTypes.REMS) {
                                epsProviderComDriverClass = osh.rems.simulation.RemsEpsProviderComDriver.class.getName();
                            } else if (epsType == EPSTypes.WIKHOURLY2015 || epsType == EPSTypes.WIKHOURLY2020
                                    || epsType == EPSTypes.WIKHOURLY2025 || epsType == EPSTypes.WIK_BASED_THESIS
                                    || epsType == EPSTypes.HOURLY_ALTERNATING) {
                                epsProviderComDriverClass = osh.comdriver.WIKHourlyBasedEpsProviderComDriver.class.getName();

                                String activePrices = "";
                                if (epsType == EPSTypes.WIKHOURLY2015) {
                                    activePrices = wikHourlyPrices[0];
                                } else if (epsType == EPSTypes.WIKHOURLY2020) {
                                    activePrices = wikHourlyPrices[1];
                                } else if (epsType == EPSTypes.WIKHOURLY2025) {
                                    activePrices = wikHourlyPrices[2];
                                } else if (epsType == EPSTypes.WIK_BASED_THESIS) {
                                    activePrices = wikHourlyPrices[3];
                                } else if (epsType == EPSTypes.HOURLY_ALTERNATING) {
                                    activePrices = hourlyPrices[0];
                                }

                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
                                epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("activePowerAutoConsumptionPV", String.valueOf(activePowerAutoConsumptionPV));
                                epsParams.put("activePowerAutoConsumptionCHP", String.valueOf(activePowerAutoConsumptionCHP));
                                epsParams.put("resolutionOfPriceSignal", String.valueOf(resolutionOfPriceSignal));
                                epsParams.put("activePowerPrices", activePrices);
                                epsParams.put("reactivePowerPrice", String.valueOf(reactivePowerPrice));
                                epsParams.put("ancillaryCommodities", Arrays.toString(ancillaryCommodities));
                                epsParams.put("signalConstantPeriod", String.valueOf(signalConstantPeriod));
                            } else if (epsType == EPSTypes.WIKWEEKDAY2015
                                    || epsType == EPSTypes.WIKWEEKDAY2020
                                    || epsType == EPSTypes.WIKWEEKDAY2025) {
                                epsProviderComDriverClass = osh.comdriver.WIKWeekDayBasedEpsProviderComDriver.class.getName();

                                String activePrices = epsType == EPSTypes.WIKWEEKDAY2015 ? wikWeekDayPrices[0] :
                                        (epsType == EPSTypes.WIKWEEKDAY2020 ? wikWeekDayPrices[1] :
                                                wikWeekDayPrices[2]);

                                epsParams.put("naturalGasPowerPrice", String.valueOf(naturalGasPowerPrice));
                                epsParams.put("activePowerFeedInPV", String.valueOf(activePowerFeedInPV));
                                epsParams.put("activePowerFeedInCHP", String.valueOf(activePowerFeedInCHP));
                                epsParams.put("resolutionOfPriceSignal", String.valueOf(resolutionOfPriceSignal));
                                epsParams.put("activePowerPrices", activePrices);
                                epsParams.put("reactivePowerPrice", String.valueOf(reactivePowerPrice));
                                epsParams.put("ancillaryCommodities", Arrays.toString(ancillaryCommodities));
                                epsParams.put("signalConstantPeriod", String.valueOf(signalConstantPeriod));
                            }
                            AddAssignedComDevice.addAssignedComDevice(
                                    simConfig.getCalConfig(),
                                    epsProviderUUID,
                                    ComDeviceTypes.MULTI_COMMODITY,
                                    epsProviderComDriverClass,
                                    epsProviderComManagerClass,
                                    simPackage.getScreenplayType(),
                                    epsParams);

                            if (plsType == PLSTypes.NORMAL) {
                                plsProviderComDriverClass = osh.comdriver.FlatPlsProviderComDriver.class.getName();
                            } else if (plsType == PLSTypes.REMS) {
                                plsProviderComDriverClass = osh.rems.simulation.RemsPlsProviderComDriver.class.getName();
                            }

                            Map<String, String> plsParams = new HashMap<>();
                            plsParams.put("newSignalAfterThisPeriod", String.valueOf(newSignalAfterThisPeriod));
                            plsParams.put("signalPeriod", String.valueOf(signalPeriod));
                            plsParams.put("activeLowerLimit", String.valueOf(activeLowerLimit));
                            plsParams.put("activeUpperLimit", String.valueOf(activeUpperLimit));
                            plsParams.put("reactiveLowerLimit", String.valueOf(reactiveLowerLimit));
                            plsParams.put("reactiveUpperLimit", String.valueOf(reactiveUpperLimit));

                            AddAssignedComDevice.addAssignedComDevice(
                                    simConfig.getCalConfig(),
                                    OldSimOSHConfigurationData.comDeviceIdPLS,
                                    ComDeviceTypes.ELECTRICITY,
                                    plsProviderComDriverClass,
                                    plsProviderComManagerClass,
                                    simPackage.getScreenplayType(),
                                    plsParams);

                            simConfig.getOshConfig().setMeterUUID(BuildingConfiguration.meterUUID.toString());

                            ConfigurationParameter param = new ConfigurationParameter();
                            param.setParameterName("logH0");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logH0));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logEpsPls");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logEpsPls));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logDetailedPower");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logDetailedPower));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logIntervalls");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logIntervals));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logDevices");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logDevices));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logHotWater");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logHotWater));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logWaterTank");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logWaterTank));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logGA");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logGA));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("logSmartHeater");
                            param.setParameterType("String");
                            param.setParameterValue(String.valueOf(logSmartHeater));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            param = new ConfigurationParameter();
                            param.setParameterName("loggingIntervalls");
                            param.setParameterType("String");
                            param.setParameterValue(Arrays.toString(
                                    Arrays.stream(loggingIntervals).map(Arrays::toString).toArray(String[]::new)));
                            simConfig.getOshConfig().getEngineParameters().add(param);

                            //PATH
                            // [package=currentTime]/
                            //FILENAME
                            // [file]_[simType]_[screenplayType]_[optAlg]_[optObjective]_[noPersons]_[runNo]
//			String fileSuffix = ""
//					+ "_" + simPackage.getScreenplayType().ordinal()
//					+ "_" + currentOptimizationAlgorithm
//					+ "_" + currentEpsOptimizationObjective
//					+ "_" + currentPlsOptimizationObjective
//					+ "_" + currentNumberOfPersons
//					+ "_" + currentNumberOfRun
//					+ ".xml";
                            String fileSuffix = ".xml";

                            try {
                                XMLSerialization.marshal2File(
                                        packagesFilePath + systemPath + ealConfigFileName + fileSuffix, simConfig.getEalConfig());
                                XMLSerialization.marshal2File(
                                        packagesFilePath + systemPath + ocConfigFileName + fileSuffix, simConfig.getOcConfig());
                                XMLSerialization.marshal2File(
                                        packagesFilePath + systemPath + calConfigFileName + fileSuffix, simConfig.getCalConfig());
                                XMLSerialization.marshal2File(
                                        packagesFilePath + systemPath + oshConfigFileName + fileSuffix, simConfig.getOshConfig());
                                XMLSerialization.marshal2File(
                                        packagesFilePath + simulationPath + screenplayFileName + fileSuffix, simConfig.getMyScreenplay());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            System.out.println("[INFO] config done"
                                    + " : screenplayType=" + simPackage.getScreenplayType()
                                    + " : optimizationAlgorithm=" + currentOptimizationAlgorithm
                                    + " : optimizationObjective=" + currentEpsOptimizationObjective
                                    + " : optimizationObjective=" + currentPlsOptimizationObjective
                                    + " : numberofPersons=" + currentNumberOfPersons
                                    + " : numberOfRun=" + currentNumberOfRun);

                        } // currentNoOfRun
                    } // currentNumberOfPersons
                } // currentPlsOptimizationObjective
            } // currentPpsOptimizationObjective
        } // currentOptimizationAlgorithm

    }

}
