package constructsimulation;

import constructsimulation.SimulationPackage.DynamicScreenplayArguments;
import constructsimulation.data.BuildingConfiguration;
import constructsimulation.data.OldSimOSHConfigurationData;
import constructsimulation.datatypes.*;
import constructsimulation.generation.ConstructSimulationHelper;
import constructsimulation.generation.device.appliance.AddGenericApplianceDevice;
import constructsimulation.generation.device.chp.AddDachsDevice;
import constructsimulation.generation.utility.AddAssignedComDevice;
import constructsimulation.generation.utility.AddAssignedDevice;
import constructsimulation.generation.utility.CreateGAConfiguration;
import osh.configuration.oc.GAConfiguration;
import osh.configuration.system.*;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.simulation.screenplay.ScreenplayType;
import osh.utils.xml.XMLSerialization;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
    public static Long optimizationMainRandomSeed = 0xd1ce5bL;

    public static final UUID hhUUID = UUID.randomUUID();
    public static final RunningType runningType = RunningType.SIMULATION;

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
    public static double upperOverlimitFactor = simPackage.plsOverLimitFactor;
    public static double lowerOverlimitFactor = 1.0;
    /* VAR Optimization Objective:
     *
     * 0 -> no reactive Power Pricing
     * 1 -> reactive Power Pricing by EPS
     */
    public static int varOptimizationObjective;
    public static double crossoverProbability = 0.7;
//	public static int varOptimizationObjective = 1;
    public static final int crossoverPoints = 2;
    //	public static double mutationProbability = 0.01;
//	public static double mutationProbability = 0.005;
    public static final double mutationProbability = 0.001;
//	static int popSize = 50;
    public static double autoProbMutationFactor = 1.0;
    //step size of the optimization algorithm (calculation interval)
    public static int stepSizeESCinOptimization = 60;
    //grid values
    //electrica grids at index 0, thermal at 1
    public static final GridConfigurationWrapper[] grids = {
            //SimulationElectrical
            new GridConfigurationWrapper(
                    "electrical",
                    "configfiles/grids/SimulationElectricalGrid.xml"),
            //SimulationThermal
            new GridConfigurationWrapper(
                    "thermal",
                    "configfiles/grids/SimulationThermalGrid.xml"),
            //ESHLThermal
//		new GridConfigurationWrapper(
//				"thermal",
//				"configfiles/grids/ESHLThermalGrid.xml"),
//		//FZIThermal
//		new GridConfigurationWrapper(
//				"thermal",
//				"configfiles/grids/FZIThermalGrid.xml")
    };
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
     * Defines whether a Generic Device will be simulated<br>
     * STANDARD
     * 0 : COOKTOP, 		3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 1 : DISHWASHER, 	4 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 2 : STOVE, 			3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 3 : TUMBLE_DRYER, 	3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 4 : WASHINGMACHINE,	3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * <p>
     * DELAYABLE
     * // : COOKTOP, 		3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 6 : DISHWASHER, 	4 Configurations, 3 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * // : STOVE, 			3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 8 : TUMBLE_DRYER, 	3 Configurations, 3 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 9 : WASHINGMACHINE,	3 Configurations, 3 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * <p>
     * INTERRUPTIBLE
     * // : COOKTOP, 		3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 11 : DISHWASHER, 	4 Configurations, 5 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * // : STOVE, 			3 Configurations, 1 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 13 : TUMBLE_DRYER, 	3 Configurations, ? Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * 14 : WASHINGMACHINE, 3 Configurations, 5 Phase, 1 Profile each, ELECTRIC (MI-MI)<br>
     * <p>
     * HYBRID
     * 15 : COOKTOP, 		3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 16 : DISHWASHER, 	4 Configurations, 1 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * 17 : STOVE, 			3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 18 : TUMBLE_DRYER, 	3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * 19 : WASHINGMACHINE, 3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * <p>
     * HYBRID DELAYABLE
     * // : COOKTOP, 		3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 21 : DISHWASHER, 	4 Configurations, 3 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * // : STOVE, 			3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 23 : TUMBLE_DRYER, 	3 Configurations, 3 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * 24 : WASHINGMACHINE, 3 Configurations, 3 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * <p>
     * HYBRID INTERRUPTIBLE
     * // : COOKTOP, 		3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 26 : DISHWASHER, 	4 Configurations, 5 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * // : STOVE, 			3 Configurations, 1 Phase, 2 Profile each, ELECTRIC and GAS (MI-MI)<br>
     * 28 : TUMBLE_DRYER, 	3 Configurations, ? Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * 29 : WASHINGMACHINE, 3 Configurations, 5 Phase, 2 Profile each, ELECTRIC and HOTWATER (MI-MI)<br>
     * <p>
     * HYBRID SINGLE
     * 30 : COOKTOP, 		3 Configurations, 1 Phase, 1 Profile each, GAS (MI-MI)<br>
     * 31 : DISHWASHER, 	4 Configurations, 5 Phase, 1 Profile each, HOTWATER (MI-MI)<br>
     * 32 : STOVE, 			3 Configurations, 1 Phase, 1 Profile each, GAS (MI-MI)<br>
     * 33 : TUMBLE_DRYER, 	3 Configurations, ? Phase, 1 Profile each, HOTWATER (MI-MI)<br>
     * 34 : WASHINGMACHINE, 3 Configurations, 5 Phase, 1 Profile each, HOTWATER (MI-MI)<br>
     */
    public static boolean[] genericAppliancesToSimulate = {

            // STUPID

//		true, // available (IH)
//		true, // available (DW)
//		true, // available (OV)
//		true, // available (TD)
//		true, // available (WM)
//
//		false, // NONSENSE
//		false, // available
//		false, // NONSENSE
//		false, // available
//		false, // available

            // INTEllIGENT
            true,    // available (IH)
            false,    // available (DW)
            true,    // available (OV)
            false,    // available (TD)
            false,    // available (WM)

            false,    // NONSENSE
            true,    // available
            false,    // NONSENSE
            true,    // available
            true,    // available

            //INTERRUPTIBLE

            false,    // NONSENSE
            false,    // available
            false,    // NONSENSE
            false,
            false,

            //HYBRID

            false, // available
            false, // available
            false, // available
            false, // available
            false, // available

            //HYBRID DELAYABLE

            false, // NONSENSE
            false, // available
            false, // NONSENSE
            false, // available
            false, // available

            //HYBRID INTERRUPTIBLE

            false, // NONSENSE
            false, // available
            false, // NONSENSE
            false, // available
            false, // available

            //SINGLE HYBRID

            false, // available
            false, // available
            false, // available
            false, // available
            false, // available
    };
    /**
     * Simulate real PV from logged data of KIT ESHL PV
     */
//	static boolean usePVRealESHL = true;
    public static boolean usePVRealESHL;
    /**
     * Simulate real PV from logged data of FZI HoLL PV
     */
    public static boolean usePVRealHOLL = true;
    //	public static int pvNominalPower = 2000;
    public static int pvNominalPower = 4000;
    public static final int pastDaysPrediction = 14;
    //Pv.ESHL
    public static final String pathToFilesESHL = "configfiles/pv/eshl/1s/cleaned_20112012_";
    public static final String fileExtensionESHL = ".csv";
    public static final double profileNominalPowerESHL = -4600.0;
    //Pv.HOLL
    public static final String pathToFilesHOLL = "configfiles/pv/holl2013cleaned";
    public static final String fileExtensionHOLL = ".csv";
//	public static boolean showGui = false;
    //positive due to profile having positive values
    public static final double profileNominalPowerHOLL = 5307.48; // positive value! [sic!]

    // ######### 1 #######
    /**
     * Maximum ComplexPower of PV device in VA (SMA inverter: Smax = 10000 VA)
     */
    public static String pvComplexPowerMax = "10000";
    /**
     * Maximum cosPhi of PV device (SMA  inverter: cosPhi < 0 -0.8)
     */
    public static String pvCosPhiMax = "-0.8";
    /**
     * Battery Storage
     */
//	public static boolean useBatteryStorage = true;
    public static boolean useBatteryStorage;
    public static final int batteryCycle = 10000;
    //Normal storage Model without any Loss
    public static final int batteryType = 1;
//	public static boolean usePVRealHOLL = false;
    //	//Lithium-Ionen-Battery
//	public static int batterytype = 1;
//	//Blei-Gel-Battery
//	public static int batterytype = 2;
    public static final int roomTemperature = 20;
//	public static int pvNominalPower = 6000;
//	public static int pvNominalPower = HouseholdConfiguration.pvNominalPower[numberOfPersons - 1];
    public static final int batteryRescheduleAfter = 4 * 3600; //4 hours
    public static final long batteryNewIppAfter = 1 * 3600; // 1 hour
    public static final int batteryTriggerIppIfDeltaSoCBigger = 5000;
    //	static boolean baseloadDeviceToSimulate = false;
    public static final int baseloadPastDaysPrediction = 14;
    public static final float baseloadWeightForOtherWeekday = 1.0f;
    public static final float baseloadWeightForSameWeekday = 5.0f;
    /**
     * gas heating device (condensing boiler)
     */
    public static boolean useGasHeating = true;


    /**
     * kWh/kWp
     * IMPORTANT: Currently not in use
     * TODO: Implement
     */
//	public static String[] pvKWhPerNominalPowerInW = {"1000"};
    //	public static boolean useGasHeating = false;
    public static final double minTemperature = 60.0;
    public static final double maxTemperature = 80.0;
    public static final int maxHotWaterPower = 15000; //15 kW
    public static final int maxGasPower = 15000; //15 kW
//	static boolean intelligentBatteryStorageControl = false;
    public static final long gasNewIppAfter = 1 * 3600; // 1 hour
    //	public static int gasTypicalActivePowerOn = 0; //W
    public static final int gasTypicalActivePowerOn = 100; //W
    public static int gasTypicalActivePowerOff; //W
    public static int gasTypicalReactivePowerOn; //W
    public static int gasTypicalReactivePowerOff; //W
    /**
     * Defines whether the Dachs-CHP will be simulated
     */
    public static boolean useDachsCHP = true;
    public static boolean intelligentCHPControl = true; //####
    public static final int typicalActivePower = -5500;
    public static final int typicalThermalPower = -12500;
    public static int typicalAdditionalThermalPower;
    public static final int typicalGasPower = 20500;
    public static final UUID hotWaterTankUuid = BuildingConfiguration.hotWaterTankUUID;
    public static final int rescheduleAfter = 4 * 3600; // 4 hours
    public static final long newIPPAfter = 1 * 3600; // 1 hour
    public static final int relativeHorizonIPP = 24 * 3600; // 24 hours
    //	public static double currentHotWaterStorageMinTemp = 55;
    public static final double currentHotWaterStorageMinTemp = 60;
    public static final double currentHotWaterStorageMaxTemp = 80;
    public static final double forcedOnHysteresis = 5.0;
    public static final double chpCosPhi = 0.9;
    public static final double fixedCostPerStart = 8.0;
    public static final double forcedOnOffStepMultiplier = 0.1;
//	public static boolean useDachsCHP = false;
    public static final int forcedOffAdditionalCost = 10;
//	public static boolean intelligentCHPcontrol = false;
    public static final double chpOnCervisiaStepSizeMultiplier = 0.0000001;
    public static final int dachsMinRuntime = 15 * 60; //no short runtimes
    //	static boolean useHotWaterTank = false;
//	public static int tankSize = 150;
    public static int tankSize = 350;
    //	public static int tankSize = 750;
    public static final long hotWaterTankNewIPPAfter = 1 * 3600; // 1 hour
    public static final double hotWaterTankTriggerIfDeltaTemp = 0.25;
    public static final double tankDiameter = 0.5;
    public static final double initialTemperature = 70.0;
    public static final double ambientTemperature = 20.0;
    /**
     * insert heating element (max. power 3.5 kW) : E.G.O. Smart Heater
     */
//	public static boolean useIHESmartHeater = true; //####
    public static boolean useIHESmartHeater;
    public static final int smartHeaterTemperatureSetting = 80;
    public static final long smartHeaterNewIPPAfter = 1 * 3600; // 1 hour
    public static final long smartHeaterTriggerIfDeltaTemp = 1;
    public static final boolean useVDIDomesticHotWater = true;
    //values for the VDI6002DomesticHotWaterSimulationDriver
    public static final int[] yearlyDomesticHotWaterEnergyUsed = {
            700,    //1pax
            1400,    //2pax
            2100,    //3pax
            2800,    //4pax
            3500    //5pax
    };
    public static final String drawoffProfileFileName = "configfiles/dhw/eu/eu_drawofftypesfile.csv";
    public static final String weekDayHourProbabilityFileName = "configfiles/dhw/vdi_6002_weekday_hour_drawoff_probabilities.csv";
    public static final int domesticPastDaysPrediction = 14;
    public static final float domesticWeightForOtherWeekday = 1.0f;
    public static final float domesticWeightForSameWeekday = 5.0f;
    public static final int spacePastDaysPrediction = 14;
    public static final float spaceWeightForOtherWeekday = 1.0f;
    public static final float spaceWeightForSameWeekday = 5.0f;
    // OptimizationAlgorithm specific variables
    static final int numEvaluations = 20000;
    //	static int numEvaluations = 10000;
    static final int popSize = 100;
    //	static String crossOverOperator = "SingleBinarySinglePointCrossover";
    static final String crossOverOperator = "SingleBinaryNPointsCrossover";
    //	static String mutationOperator = "BitFlipMutation";
//  automatically adjusts the mutation rate to 1/(numberOfBits)
    static final String mutationOperator = "BitFlipAutoProbMutation";
    static final String selectionOperator = "BinaryTournament";
    //stopping Rules
    //should never be false
    static final boolean useMaxEvaluations = true;
    //how many generations the minDeltaFitness can be violated before stopping
    static final int maxGenerationsViolations = 20;
//	static boolean useDomesticHotWaterDemand = false;
    static final boolean useMinDeltaFitness = true;
//	public static boolean useVDIDomesticHotWater = false;
    //min. perc. amount of fitness change required
    static final double minDeltaFitnessPercent = 5.0E-11;
    /**
     * Use SimulationLogger
     */
//	static boolean simLogger = true;
    static boolean simLogger;
    /**
     * Defines whether a Miele Device will be simulated
     * 0 : COOKTOP (648Wh))
     * 1 : DISHWASHER (927Wh)
     * 2 : STOVE (1094Wh)
     * 3 : TUMBLE_DRYER (1457Wh)
     * 4 : WASHINGMACHINE (654Wh)
     */
    //FIXME: Miele appliances do currently not work
//	static boolean[] mieleDevicesToSimulate = { // DO NOT USE
//		true,	//COOKTOP (IH)
//		true,	//DISHWASHER (DW)
//		true,	//STOVE (OV)
//		true,	//TUMBLE_DRYER (TD)
//		true	//WASHINGMACHINE (WM)
//	};
    static boolean[] mieleDevicesToSimulate = { // DO NOT USE
            false,    //COOKTOP (648Wh))
            false,    //DISHWASHER (927Wh)
            false,    //STOVE (1094Wh)
            false,    //TUMBLE_DRYER (1457Wh)
            false    //WASHINGMACHINE (654Wh)
    };
    /**
     * IH, DW, OV, TD, WM
     */
    static String[] genericAppliances1stTDOF = {
//		"0",
//		"0",
//		"0",
//		"0",
//		"0"
            "" + 0,
            "" + 12 * 3600,
            "" + 0,
            "" + 12 * 3600,
            "" + 12 * 3600
    };
    static final boolean intelligentBatteryStorageControl = true;
    /**
     * Defines whether the baseload (based on H0) will be simulated
     */
    static final boolean baseloadDeviceToSimulate = true;
    /**
     * Simulate hot water tank
     */
    static final boolean useHotWaterTank = true;
//	static boolean useSpaceHeatingHotWaterDemand = false;
    /**
     * Simulate Domestic Hot Water Demand
     */
    static final boolean useDomesticHotWaterDemand = true;
    /**
     * Simulate Space Heating Hot Water Demand
     */
    static final boolean useSpaceHeatingHotWaterDemand = true;
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
    static String mieleDeviceProfilesPath = BuildingConfiguration.mieleDeviceProfilesPath;
    static String pvDeviceProfileFile = BuildingConfiguration.ev0Filename;
    static final String simulationPath = BuildingConfiguration.simulationPath;
    static final String screenplayFileName = BuildingConfiguration.screenplayFileName;
    static String descriptorFileName = BuildingConfiguration.descriptorFileName;
    static final String iDeviceScreenplayDirectory = BuildingConfiguration.screenplayMielePath;
    static final String ealConfigFileName = BuildingConfiguration.EALConfigFileName;
    static final String calConfigFileName = BuildingConfiguration.calConfigFileName;
    static final String oshConfigFileName = BuildingConfiguration.oshConfigFileName;
    // Configuration files for baseload simulation and PV simulation
    static final String h0Filename = BuildingConfiguration.h0Filename15Min;
    static String ev0Filename = BuildingConfiguration.ev0Filename;
//	static String h0Filename = HouseholdConfiguration.h0Filename1Hour;
    static final String ocConfigFileName = BuildingConfiguration.ocConfigFileName;

    // ### Global OC-unit ###
    static final String globalObserverClass = osh.mgmt.globalobserver.OSHGlobalObserver.class.getName();
    static final String globalControllerClass = osh.mgmt.globalcontroller.OSHGlobalControllerJMetal.class.getName();
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
    // Logger-Configuration
    static final String loggerBusDriverClass = osh.busdriver.simulation.SimulationLoggerBusDriver.class.getName();
    static final String loggerBusManagerClass = osh.mgmt.busmanager.simulation.SimulationLoggerBusManager.class.getName();

    static {
        /* Random seed if fixed random seed */
        simPackage.setInitialRandomSeed(0xd1ce5bL);

        /* Used to generate random seed for every run */
        simPackage.setAlwaysNewRandomSeed(true);

        /*
         * Number of runs with same configuration
         * but different randomSeeds (if alwaysNewRandomSeed = true) */
        simPackage.setNumberOfRuns(1);

        /* The simulation is recorded by the simulation logger */
        simPackage.setSimulationResultLogging(true);

        /* Determines duration of simulation in ticks (usually: 1 tick = 1 second) */
//		simPackage.setSimulationDuration(5079965);				// available real data length (from "Wohnphase")
//		simPackage.setSimulationDuration(365 * 24 * 60 * 60);	// 1 year
        simPackage.setSimulationDuration(86400 * 28);            // 28 days
//		simPackage.setSimulationDuration(604800 * 2);			// 2 weeks
//		simPackage.setSimulationDuration(86400);				// 1 day
//		simPackage.setSimulationDuration(3600);					// 1 hour

        try {
            XMLGregorianCalendar simulationStartTime = DatatypeFactory.newInstance()
//					.newXMLGregorianCalendar(1970, 1, 1, 0, 0, 0, 0, 0);
                    .newXMLGregorianCalendar(1970, 7, 1, 0, 0, 0, 0, 0);
            simPackage.setSimulationStartTime(simulationStartTime);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }


        /*
         * Screenplay types to simulate<br>
         * 0: static (screenplay.xml) - UNSUPPORTED in this version!<br>
         * 1: dynamic (screenplay_dynamic.xml) with dynamic Generation of Actions */
//		simPackage.setScreenplayType(ScreenplayType.STATIC);
        simPackage.setScreenplayType(ScreenplayType.DYNAMIC);

        // ScreenplayType specific variables
        // 1: Variables for screenplayType = 1
        simPackage.setDynamicScreenplayArguments(new DynamicScreenplayArguments());
        /* number of persons in simulated household (1-5)<br>
         *  determines yearly electricity consumption and consumption share of devices */
        simPackage.getDynamicScreenplayArguments().getNumPersons().add(numberOfPersons);

        /*
         * Optimization Objective<br>
         * AncillaryCommodity PowerPriceSignals (eps)<br>
         * <br>
         * 0: "ACTIVEPOWEREXTERNAL
         * 		+ NATURALGASPOWEREXTERNAL" : <br>
         * > sum of all activePowers * ACTIVEPOWEREXTERNAL-Price<br>
         * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
         * <br>
         * 1: "ACTIVEPOWEREXTERNAL
         * 		+ PVACTIVEPOWERFEEDIN
         * 		+ NATURALGASPOWEREXTERNAL" : <br>
         * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
         * > if (sum of all activePowers < 0) -> Math.max(pvPower,(sum of all activePowers)) * PVACTIVEPOWERFEEDIN<br>
         * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
         * <br>
         * 2: "ACTIVEPOWEREXTERNAL
         * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
         * 		+ NATURALGASPOWEREXTERNAL"<br>
         * > sum of all activePowers except PV * ACTIVEPOWEREXTERNAL-Price<br>
         * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
         * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
         * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
         * <br>
         * 3: "ACTIVEPOWEREXTERNAL
         * 		+ PVACTIVEPOWERFEEDIN
         * 		+ CHPACTIVEPOWERFEEDIN
         * 		+ NATURALGASPOWEREXTERNAL" : <br>
         * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
         * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
         * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
         * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
         * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
         * <br>
         * 4: "ACTIVEPOWEREXTERNAL
         * 		+ PVACTIVEPOWERFEEDIN + PVACTIVEPOWERAUTOCONSUMPTION
         * 		+ CHPACTIVEPOWERFEEDIN + CHPACTIVEPOWERAUTOCONSUMPTION
         * 		+ NATURALGASPOWEREXTERNAL"<br>
         * > if (sum of all activePowers > 0) -> (sum of all activePowers) * ACTIVEPOWEREXTERNAL-Price<br>
         * > pvPowerToGrid * PVACTIVEPOWERFEEDIN<br>
         * > pvPowerAutoConsumption * PVACTIVEPOWERAUTOCONSUMPTION<br>
         * > chpPowerToGrid * CHPACTIVEPOWERFEEDIN<br>
         * > chpPowerAutoConsumption * CHPACTIVEPOWERAUTOCONSUMPTION<br>
         * > gasPower * NATURALGASPOWEREXTERNAL-Price<br>
         * > IMPORTANT: PV and CHP to grid depending on their power proportionally!<br>
         * <br>
         * TODO: reactivePower pricing... */
//		simPackage.getEPSOptimizationObjectives().add(0);
//		simPackage.getEPSOptimizationObjectives().add(1);
//		simPackage.getEPSOptimizationObjectives().add(2);
//		simPackage.getEPSOptimizationObjectives().add(3);
        simPackage.getEPSOptimizationObjectives().add(4);

        /*
         * Optimization Objective<br>
         * PowerLimitSignals (pls)<br>
         * determines which LBS-constraint violations are being priced<br>
         * 0: none<br>
         * 1: additional costs (overLimitFactor * ACTIVEPOWEREXTERNAL-price) for ACTIVEPOWEREXTERNAL limit violations<br>
         * 2: additional costs (overLimitFactor * POWEREXTERNAL-price) for ACTIVEPOWEREXTERNAL and REACTIVEPOWEREXTERNAL limit violations<br>
         * 3: ... */
//		simPackage.getPLSOptimizationObjectives().add(0);
        simPackage.getPLSOptimizationObjectives().add(1);

        /*
         * TODO: merge with previous
         * Optimization Objective<br>
         * LBS<br>
         * determines additional costs if electricity consumption is above power limit<br>
         * 0: no additional costs
         * !0: additional costs */
//		simPackage.setPLSOverLimitFactor(0);
        simPackage.setPLSOverLimitFactor(1);

        /*
         * Algorithm used for optimization in GlobalController<br>
         * 0: JMetal (GA) */
        simPackage.getOptimizationAlgorithms().add(0);

    }


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
        Random optRand = new Random(optimizationMainRandomSeed);

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
                            long currentOptimizationMainRandomSeed;
                            if (simPackage.isAlwaysNewRandomSeed()) {
                                currentRandomSeed = rand.nextLong();
                                currentOptimizationMainRandomSeed = optRand.nextLong();
                            } else {
                                currentRandomSeed = simPackage.getInitialRandomSeed();
                                currentOptimizationMainRandomSeed = optimizationMainRandomSeed;
                            }

                            plsProviderComDriverClass = osh.comdriver.FlatPlsProviderComDriver.class.getName();

                            EALConfigurationWrapper eALConfigurationData =
                                    new EALConfigurationWrapper(
                                            currentNumberOfPersons,
                                            h0Filename,
                                            simPackage.isSimulationResultLogging(),
                                            loggerBusDriverClass,
                                            loggerBusManagerClass);

                            //GA configuration
                            //ONLY EVER use datatypes that have a String constructor, everything else will ecplode in your face
                            HashMap<String, Object> crossOverParams = new HashMap<>();
                            crossOverParams.put("probability", crossoverProbability);
                            if (crossOverOperator.equals("SingleBinaryNPointsCrossover"))
                                crossOverParams.put("points", crossoverPoints);
                            HashMap<String, Object> mutationParams = new HashMap<>();
                            mutationParams.put("probability", mutationProbability);
                            mutationParams.put("autoProbMuatationFactor", autoProbMutationFactor);
                            HashMap<String, Object> selectionParams = new HashMap<>();

                            Map<String, Map<String, ?>> stoppingRules = new HashMap<>();
                            HashMap<String, Object> maxEvalParams = new HashMap<>();
                            maxEvalParams.put("populationSize", popSize);
                            maxEvalParams.put("maxEvaluations", numEvaluations);

                            HashMap<String, Object> deltaFitnessParams = new HashMap<>();
                            deltaFitnessParams.put("minDeltaFitnessPerc", minDeltaFitnessPercent);
                            deltaFitnessParams.put("maxGenerationsDeltaFitnessViolated", maxGenerationsViolations);

                            if (useMaxEvaluations)
                                stoppingRules.put("EvaluationsStoppingRule", maxEvalParams);
                            if (useMinDeltaFitness)
                                stoppingRules.put("DeltaFitnessStoppingRule", deltaFitnessParams);

                            GAConfiguration gaConfig = CreateGAConfiguration.createGAConfiguration(
                                    numEvaluations,
                                    popSize,
                                    crossOverOperator,
                                    mutationOperator,
                                    selectionOperator,
                                    crossOverParams,
                                    mutationParams,
                                    selectionParams,
                                    stoppingRules);

                            OCConfigurationWrapper ocConfigurationData =
                                    new OCConfigurationWrapper(
                                            currentOptimizationMainRandomSeed,
                                            globalObserverClass,
                                            globalControllerClass,
                                            currentEpsOptimizationObjective,
                                            currentPlsOptimizationObjective,
                                            varOptimizationObjective,
                                            upperOverlimitFactor,
                                            lowerOverlimitFactor,
                                            gaConfig,
                                            stepSizeESCinOptimization,
                                            hotWaterTankUuid);

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
                                    hhUUID
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
                                            ocConfigurationData,
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


                            // <4> create baseload device
                            if (baseloadDeviceToSimulate) {
                                Map<String, String> baseloadParams = new HashMap<>();
                                baseloadParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER]");
                                baseloadParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                baseloadParams.put("h0filename", h0Filename);
                                baseloadParams.put("h0classname", h0ClassName);
                                baseloadParams.put("baseloadyearlyconsumption", String.valueOf(BuildingConfiguration.avgYearlyBaseloadConsumption[numberOfPersons - 1]));
                                baseloadParams.put("baseloadcosphi", String.valueOf(BuildingConfiguration.baseloadCosPhi));
                                baseloadParams.put("baseloadisinductive", String.valueOf(BuildingConfiguration.baseloadIsInductive));

                                baseloadParams.put("pastDaysPrediction", String.valueOf(baseloadPastDaysPrediction));
                                baseloadParams.put("weightForOtherWeekday", String.valueOf(baseloadWeightForOtherWeekday));
                                baseloadParams.put("weightForSameWeekday", String.valueOf(baseloadWeightForSameWeekday));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.baseloadUUID,
                                        osh.driver.simulation.BaseloadSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.BaseloadLocalObserver.class.getName(),
                                        null,
                                        false,
                                        DeviceTypes.BASELOAD,
                                        DeviceClassification.BASELOAD,
                                        baseloadParams);
                            }
                            // </4> create baseload device


                            // ### GenericAppliances ###
                            // IMPORTANT: ALWAYS ADD DEVICES IN THE SAME ORDER (RANDOM SEED!!!)

                            // COOKTOP / IH

                            if (genericAppliancesToSimulate[0]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.INDUCTIONCOOKTOP,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[1],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[1][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ih/IH_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[1]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ih/IH_3CO_1PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[15]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.INDUCTIONCOOKTOP,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[1],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[1][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ih/IH_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[1]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ih/IH_3CO_1PH_2PR_MC_EG.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,NATURALGASPOWER]");
                            }

                            if (genericAppliancesToSimulate[30]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.INDUCTIONCOOKTOP,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[1],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[1][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ih/IH_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[1]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ih/IH_3CO_1PH_1PR_SC_G.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,NATURALGASPOWER]");
                            }


                            // DISHWASHER / DW

                            if (genericAppliancesToSimulate[1]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_1PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[6]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[1],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_3PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[11]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[1],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_5PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[16]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_1PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[21]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[1],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_3PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[26]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[1],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_5PH_4PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[31]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DISHWASHER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[0],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[0][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/dw/DW_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[0]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/dw/DW_4CO_1PH_1PR_SC_H.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            // STOVE / OV

                            if (genericAppliancesToSimulate[2]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.ELECTRICSTOVE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[2],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[2][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ov/OV_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[2]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ov/OV_3CO_1PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[17]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.ELECTRICSTOVE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[2],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[2][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ov/OV_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[2]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ov/OV_3CO_1PH_2PR_MC_EG.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,NATURALGASPOWER]");
                            }

                            if (genericAppliancesToSimulate[32]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.ELECTRICSTOVE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[2],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[2][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/ov/OV_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[2]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/ov/OV_3CO_1PH_1PR_SC_G.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,NATURALGASPOWER]");
                            }

                            // DRYER / TD

                            if (genericAppliancesToSimulate[3]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_1PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[8]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[3],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_3PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[13]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[3],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_9PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[18]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_1PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[23]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[3],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_3PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[28]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[3],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_9PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[33]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.DRYER,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[3],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[3][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/td/TD_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[3]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/td/TD_3CO_1PH_1PR_SC_H.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }


                            // WASHER / WM

                            if (genericAppliancesToSimulate[4]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_1PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[9]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[4],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_3PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[14]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[4],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_5PH_1PR_SC_E.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER]");
                            }

                            if (genericAppliancesToSimulate[19]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_1PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[24]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[4],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_3PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[29]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        genericAppliances1stTDOF[4],
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_5PH_2PR_MC_EH.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }

                            if (genericAppliancesToSimulate[34]) {
                                AddGenericApplianceDevice.addGenericApplianceDevice(
                                        simConfig.getEalConfig(),
                                        DeviceTypes.WASHINGMACHINE,
                                        DeviceClassification.APPLIANCE,
                                        BuildingConfiguration.applianceUUID[4],
                                        osh.driver.simulation.GenericFutureApplianceSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.FutureApplianceLocalObserver.class.getName(),
                                        true,
                                        osh.mgmt.localcontroller.FutureApplianceLocalController.class.getName(),
                                        /* ScreenplayType screenplayType, */
                                        simPackage.getScreenplayType(),
                        /* String deviceMax1stDof,
                        String device2ndDof, */
                                        "0",
                                        "0",
                                        /* averageyearlyruns, */
                                        BuildingConfiguration.averageYearlyRuns[4][numberOfPersons - 1],
                                        /* String h0Filename,*/
                                        h0Filename,
                                        h0ClassName,
                                        /*String probabilityfilename,*/
                                        "configfiles/appliance/wm/WM_prob.csv",
                                        /*String configurationShares,*/
                                        Arrays.toString(
                                                BuildingConfiguration.configurationShares[4]),
                                        /*String profileSource,	*/
                                        "configfiles/appliance/wm/WM_3CO_1PH_1PR_SC_H.xml",
                                        /*String usedcommodities*/
                                        "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                            }


                            // OTHER


                            if (usePVRealESHL) {
                                Map<String, String> pvParams = new HashMap<>();
                                pvParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER]");
                                pvParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                pvParams.put("nominalpower", String.valueOf(pvNominalPower));
                                pvParams.put("pastDaysPrediction", String.valueOf(pastDaysPrediction));
                                pvParams.put("pathToFiles", pathToFilesESHL);
                                pvParams.put("fileExtension", fileExtensionESHL);
                                pvParams.put("profileNominalPower", String.valueOf(profileNominalPowerESHL));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        UUID.fromString("7fc1f1d9-39c3-4e5f-8907-aeb0cd1ee84c"),
                                        osh.driver.simulation.PvSimulationDriverESHLData.class.getName(),
                                        osh.mgmt.localobserver.PvLocalObserver.class.getName(),
                                        osh.mgmt.localcontroller.PvLocalController.class.getName(),
                                        false,
                                        DeviceTypes.PVSYSTEM,
                                        DeviceClassification.PVSYSTEM,
                                        pvParams);
                            }

                            if (usePVRealHOLL) {
                                Map<String, String> pvParams = new HashMap<>();
                                pvParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER]");
                                pvParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                pvParams.put("nominalpower", String.valueOf(pvNominalPower));
                                pvParams.put("pastDaysPrediction", String.valueOf(pastDaysPrediction));
                                pvParams.put("pathToFiles", pathToFilesHOLL);
                                pvParams.put("fileExtension", fileExtensionHOLL);
                                pvParams.put("profileNominalPower", String.valueOf(profileNominalPowerHOLL));
                                pvParams.put("complexpowermax", String.valueOf(pvComplexPowerMax));
                                pvParams.put("cosphimax", String.valueOf(pvCosPhiMax));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.pvUUID,
                                        osh.driver.simulation.PvSimulationDriverHollData.class.getName(),
                                        osh.mgmt.localobserver.PvLocalObserver.class.getName(),
                                        osh.mgmt.localcontroller.PvLocalController.class.getName(),
                                        false,
                                        DeviceTypes.PVSYSTEM,
                                        DeviceClassification.PVSYSTEM,
                                        pvParams);
                            }

                            if (useBatteryStorage) {
                                Map<String, String> batteryParams = new HashMap<>();
                                batteryParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER]");
                                batteryParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                batteryParams.put("minChargingState", String.valueOf(BuildingConfiguration.batteryMinChargingState));
                                batteryParams.put("maxChargingState", String.valueOf(BuildingConfiguration.batteryMaxChargingState));
                                batteryParams.put("minDischargingPower", String.valueOf(BuildingConfiguration.batteryMinDischargePower));
                                batteryParams.put("maxDischargingPower", String.valueOf(BuildingConfiguration.batteryMaxDischargePower));
                                batteryParams.put("minChargingPower", String.valueOf(BuildingConfiguration.batteryMinChargePower));
                                batteryParams.put("maxChargingPower", String.valueOf(BuildingConfiguration.batteryMaxChargePower));
                                batteryParams.put("minInverterPower", String.valueOf(BuildingConfiguration.batteryMinInverterPower));
                                batteryParams.put("maxInverterPower", String.valueOf(BuildingConfiguration.batteryMaxInverterPower));


                                batteryParams.put("newIppAfter", String.valueOf(batteryNewIppAfter));
                                batteryParams.put("triggerIppIfDeltaSoCBigger", String.valueOf(batteryTriggerIppIfDeltaSoCBigger));
                                batteryParams.put("rescheduleAfter", String.valueOf(batteryRescheduleAfter));
                                batteryParams.put("batteryCycle", String.valueOf(batteryCycle));
                                batteryParams.put("batteryType", String.valueOf(batteryType));
                                batteryParams.put("roomTemperature", String.valueOf(roomTemperature));


                                if (intelligentBatteryStorageControl) {
//					AddAssignedDevice.addAssignedDevice(
//							simConfig.getEalconfig(),
//							BuildingConfiguration.batteryUUID,
//							osh.driver.simulation.BatterySimulationDriver.class.getName(),
//							osh.mgmt.localobserver.InverterBatteryStorageObserver.class.getName(),
//							osh.mgmt.localcontroller.BatteryStorageLocalController.class.getName(),
//							true, 
//							DeviceTypes.BATTERYSTORAGE, 
//							DeviceClassification.BATTERYSTORAGE, 
//							batteryParams);		
                                } else {
                                    AddAssignedDevice.addAssignedDevice(
                                            simConfig.getEalConfig(),
                                            BuildingConfiguration.batteryUUID,
                                            osh.driver.simulation.NonControllableBatterySimulationDriver.class.getName(),
                                            osh.mgmt.localobserver.NonControllableInverterBatteryStorageObserver.class.getName(),
                                            null,
                                            false,
                                            DeviceTypes.BATTERYSTORAGE,
                                            DeviceClassification.BATTERYSTORAGE,
                                            batteryParams);
                                }
                            }

                            if (useDachsCHP) {
                                Map<String, String> dachsParams = new HashMap<>();

                                /*String usedcommodities*/
                                dachsParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER,NATURALGASPOWER]");
                                dachsParams.put("screenplaytype", "" + simPackage.getScreenplayType());

                                dachsParams.put("typicalActivePower", String.valueOf(typicalActivePower));
                                dachsParams.put("typicalThermalPower", String.valueOf(typicalThermalPower));
                                dachsParams.put("typicalAddditionalThermalPower", String.valueOf(typicalAdditionalThermalPower));
                                dachsParams.put("typicalGasPower", String.valueOf(typicalGasPower));
                                dachsParams.put("hotWaterTankUuid", String.valueOf(hotWaterTankUuid));
                                dachsParams.put("rescheduleAfter", String.valueOf(rescheduleAfter));
                                dachsParams.put("newIPPAfter", String.valueOf(newIPPAfter));
                                dachsParams.put("relativeHorizonIPP", String.valueOf(relativeHorizonIPP));
                                dachsParams.put("currentHotWaterStorageMinTemp", String.valueOf(currentHotWaterStorageMinTemp));
                                dachsParams.put("currentHotWaterStorageMaxTemp", String.valueOf(currentHotWaterStorageMaxTemp));
                                dachsParams.put("forcedOnHysteresis", String.valueOf(forcedOnHysteresis));
                                dachsParams.put("cosPhi", String.valueOf(chpCosPhi));

                                dachsParams.put("fixedCostPerStart", String.valueOf(fixedCostPerStart));
                                dachsParams.put("forcedOnOffStepMultiplier", String.valueOf(forcedOnOffStepMultiplier));
                                dachsParams.put("forcedOffAdditionalCost", String.valueOf(forcedOffAdditionalCost));
                                dachsParams.put("chpOnCervisiaStepSizeMultiplier", String.valueOf(chpOnCervisiaStepSizeMultiplier));
                                dachsParams.put("minRuntime", String.valueOf(dachsMinRuntime));

                                if (intelligentCHPControl) {
                                    AddDachsDevice.addDachsDevice(
                                            simConfig.getEalConfig(),
                                            BuildingConfiguration.chpUUID,
                                            osh.driver.simulation.DachsChpSimulationDriver.class.getName(),
                                            osh.mgmt.localobserver.DachsChpLocalObserver.class.getName(),
                                            osh.mgmt.localcontroller.DachsChpLocalController.class.getName(),
                                            dachsParams);
                                } else {
                                    AddDachsDevice.addDachsDevice(
                                            simConfig.getEalConfig(),
                                            BuildingConfiguration.chpUUID,
                                            osh.driver.simulation.DachsChpSimulationDriver.class.getName(),
                                            osh.mgmt.localobserver.NonControllableDachsChpLocalObserver.class.getName(),
                                            osh.mgmt.localcontroller.NonControllableDachsChpLocalController.class.getName(),
                                            dachsParams);
                                }
                            }

                            if (useHotWaterTank) {
                                Map<String, String> hotWaterParams = new HashMap<>();
                                hotWaterParams.put("usedcommodities", "[HEATINGHOTWATERPOWER]");
                                hotWaterParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                hotWaterParams.put("tankCapacity", String.valueOf(tankSize));
                                hotWaterParams.put("tankDiameter", String.valueOf(tankDiameter));
                                hotWaterParams.put("initialTemperature", String.valueOf(initialTemperature));
                                hotWaterParams.put("ambientTemperature", String.valueOf(ambientTemperature));
                                hotWaterParams.put("newIppAfter", String.valueOf(hotWaterTankNewIPPAfter));
                                hotWaterParams.put("triggerIppIfDeltaTempBigger", String.valueOf(hotWaterTankTriggerIfDeltaTemp));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.hotWaterTankUUID,
                                        osh.driver.simulation.HotWaterTankSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.HotWaterTankLocalObserver.class.getName(),
                                        null,
                                        false,
                                        DeviceTypes.HOTWATERSTORAGE,
                                        DeviceClassification.HVAC,
                                        hotWaterParams);
                            }

                            if (useDomesticHotWaterDemand) {
                                String domHotWaterSimDriverClassName;
                                String domHotWaterObserverClassName;
                                Map<String, String> dWaterParams = new HashMap<>();
                                dWaterParams.put("usedcommodities", "[DOMESTICHOTWATERPOWER]");
                                dWaterParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));

                                if (!useVDIDomesticHotWater) {
                                    domHotWaterSimDriverClassName = osh.driver.simulation.dhw.ESHLDomesticHotWaterSimulationDriver.class.getName();
                                    domHotWaterObserverClassName = osh.mgmt.localobserver.dhw.DomesticHotWaterLocalObserver.class.getName();

                                    dWaterParams.put("sourcefile", "configfiles/dhw/domestichotwater_" + numberOfPersons + ".csv");
                                    dWaterParams.put("pastDaysPrediction", String.valueOf(domesticPastDaysPrediction));
                                    dWaterParams.put("weightForOtherWeekday", String.valueOf(domesticWeightForOtherWeekday));
                                    dWaterParams.put("weightForSameWeekday", String.valueOf(domesticWeightForSameWeekday));
                                } else {
                                    domHotWaterSimDriverClassName = osh.driver.simulation.dhw.VDI6002DomesticHotWaterSimulationDriver.class.getName();
                                    domHotWaterObserverClassName = osh.mgmt.localobserver.dhw.VDI6002DomesticHotWaterLocalObserver.class.getName();
                                    dWaterParams.put("drawOffTypesFile", drawoffProfileFileName);
                                    dWaterParams.put("weekDayHourProbabilitiesFile", weekDayHourProbabilityFileName);
                                    dWaterParams.put("avgYearlyDemamd", String.valueOf(yearlyDomesticHotWaterEnergyUsed[numberOfPersons - 1]));
                                }

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.dhwUsageUUID,
                                        domHotWaterSimDriverClassName,
                                        domHotWaterObserverClassName,
                                        null,
                                        false,
                                        DeviceTypes.DOMESTICHOTWATER,
                                        DeviceClassification.HVAC,
                                        dWaterParams);
                            }

                            if (useSpaceHeatingHotWaterDemand) {
                                Map<String, String> shWaterParams = new HashMap<>();
                                shWaterParams.put("usedcommodities", "[HEATINGHOTWATERPOWER]");
                                shWaterParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                shWaterParams.put("sourcefile", "configfiles/heating/heating_demand_" + numberOfPersons + ".csv");
                                shWaterParams.put("pastDaysPrediction", String.valueOf(spacePastDaysPrediction));
                                shWaterParams.put("weightForOtherWeekday", String.valueOf(spaceWeightForOtherWeekday));
                                shWaterParams.put("weightForSameWeekday", String.valueOf(spaceWeightForSameWeekday));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.spaceHeatingUUID,
                                        osh.driver.simulation.heating.ESHLSpaceHeatingSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.heating.SpaceHeatingLocalObserver.class.getName(),
                                        null,
                                        false,
                                        DeviceTypes.SPACEHEATING,
                                        DeviceClassification.HVAC,
                                        shWaterParams);
                            }

                            if (useGasHeating) {
                                Map<String, String> gasParams = new HashMap<>();
                                gasParams.put("usedcommodities", "[ACTIVEPOWER,HEATINGHOTWATERPOWER,NATURALGASPOWER]");
                                gasParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                gasParams.put("minTemperature", String.valueOf(minTemperature));
                                gasParams.put("maxTemperature", String.valueOf(maxTemperature));
                                gasParams.put("maxHotWaterPower", String.valueOf(maxHotWaterPower));
                                gasParams.put("maxGasPower", String.valueOf(maxGasPower));
                                gasParams.put("newIppAfter", String.valueOf(gasNewIppAfter));
                                gasParams.put("typicalActivePowerOn", String.valueOf(gasTypicalActivePowerOn));
                                gasParams.put("typicalActivePowerOff", String.valueOf(gasTypicalActivePowerOff));
                                gasParams.put("typicalReactivePowerOn", String.valueOf(gasTypicalReactivePowerOn));
                                gasParams.put("typicalReactivePowerOff", String.valueOf(gasTypicalReactivePowerOff));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.gasHeatingUUID,
                                        osh.driver.simulation.GasBoilerSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.NonControllableGasBoilerLocalObserver.class.getName(),
                                        null,
                                        false,
                                        DeviceTypes.GASHEATING,
                                        DeviceClassification.HVAC,
                                        gasParams);
                            }

                            if (useIHESmartHeater) {
                                Map<String, String> smartHeaterParams = new HashMap<>();
                                smartHeaterParams.put("usedcommodities", "[ACTIVEPOWER,REACTIVEPOWER,HEATINGHOTWATERPOWER]");
                                smartHeaterParams.put("screenplaytype", String.valueOf(simPackage.getScreenplayType()));
                                smartHeaterParams.put("temperatureSetting", String.valueOf(smartHeaterTemperatureSetting));
                                smartHeaterParams.put("newIppAfter", String.valueOf(smartHeaterNewIPPAfter));
                                smartHeaterParams.put("triggerIppIfDeltaTempBigger", String.valueOf(smartHeaterTriggerIfDeltaTemp));

                                AddAssignedDevice.addAssignedDevice(
                                        simConfig.getEalConfig(),
                                        BuildingConfiguration.iheUUID,
                                        osh.driver.simulation.SmartHeaterSimulationDriver.class.getName(),
                                        osh.mgmt.localobserver.SmartHeaterLocalObserver.class.getName(),
                                        null,
                                        false,
                                        DeviceTypes.INSERTHEATINGELEMENT,
                                        DeviceClassification.HVAC,
                                        smartHeaterParams);
                            }

                            //gridsetup
                            for (GridConfigurationWrapper grid : grids) {
                                GridConfig gc = new GridConfig();
                                gc.setGridType(grid.gridType);
                                gc.setGridLayoutSource(grid.gridLayoutSource);
                                simConfig.getOshConfig().getGridConfigurations().add(gc);
                            }

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

                            simConfig.getOshConfig().setRunningType(runningType);

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
