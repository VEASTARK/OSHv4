package osh.utils.string;

/**
 * Centralized container for all parameter names to be used in configuration files.
 *
 * @author Sebastian Kramer
 */
public final class ParameterConstants {

    public static final class Compression {
        public static final String compressionType = "compressionType";
        public static final String compressionValue = "compressionValue";
    }

    public static final class General_Devices {
        public static final String usedCommodities = "usedcommodities";
        public static final String h0Filename = "h0filename";
        public static final String h0Classname = "h0classname";
        public static final String profileSource = "profilesource";
        public static final String filePath = "pathToFiles";
        public static final String fileExtension = "fileExtension";
    }

    public static final class Prediction {
        public static final String pastDaysPrediction = "pastDaysPrediction";
        public static final String weightForOtherWeekday = "weightForOtherWeekday";
        public static final String weightForSameWeekday = "weightForSameWeekday";
    }

    public static final class Signal {
        public static final String newSignal = "newSignalAfterThisPeriod";
        public static final String signalPeriod = "signalPeriod";
        public static final String signalConstantPeriod = "signalConstantPeriod";
    }

    public static final class Database {
        public static final String type = "type";
        public static final String tunneled = "tunneled";
        public static final String identifier = "identifier";

        public static final String serverName = "serverName";
        public static final String serverPort = "serverPort";
        public static final String serverScheme = "serverScheme";
        public static final String databaseUser = "databaseUser";
        public static final String databasePW = "databasePW";
        public static final String useSSL = "useSSL";
        public static final String truststorePath = "truststorePath";
        public static final String truststorePW = "truststorePW";

        public static final String sshUser = "sshUser";
        public static final String sshPassword = "sshPassword";
        public static final String sshHost = "sshHost";
        public static final String sshPort = "sshPort";
        public static final String initialLocalPort = "initialLocalPort";
        public static final String useKeyFile = "useKeyFile";
        public static final String keyFilePath = "keyFilePath";
    }

    public static final class EPS {
        public static final String activePrice = "activePowerPrice";
        public static final String reactivePrice = "reactivePowerPrice";
        public static final String pvFeedInPrice = "activePowerFeedInPV";
        public static final String chpFeedInPrice = "activePowerFeedInCHP";
        public static final String batteryFeedInPrice = "activePowerFeedInBattery";
        public static final String pvAutoConsumptionPrice = "activePowerAutoConsumptionPV";
        public static final String chpAutoConsumptionPrice = "activePowerAutoConsumptionCHP";
        public static final String batteryAutoConsumptionPrice = "activePowerAutoConsumptionBattery";
        public static final String batteryConsumptionPrice = "activePowerConsumptionBattery";
        public static final String gasPrice = "naturalGasPowerPrice";

        public static final String activePriceSupplyMin = "activePowerExternalSupplyMin";
        public static final String activePriceSupplyAvg = "activePowerExternalSupplyAvg";
        public static final String activePriceSupplyMax = "activePowerExternalSupplyMax";

        public static final String activePriceArray = "activePowerPrices";
        public static final String resolution = "resolutionOfPriceSignal";
        public static final String ancillaryCommodities = "ancillaryCommodities";
        public static final String filePathPVPriceSignal = "filePathActivePowerFeedInPVPriceSignal";
        public static final String filePathPriceSignal = "filePathPriceSignal";
    }

    public static final class PLS {
        public static final String activeLowerLimit = "activeLowerLimit";
        public static final String activeUpperLimit = "activeUpperLimit";
        public static final String reactiveLowerLimit = "reactiveLowerLimit";
        public static final String reactiveUpperLimit = "reactiveUpperLimit";
    }

    public static final class IPP {
        public static final String newIPPAfter = "newIPPAfter";
        public static final String rescheduleAfter = "rescheduleAfter";
        public static final String relativeHorizon = "relativeHorizonIPP";
        public static final String triggerIppIfDeltaTemp = "triggerIppIfDeltaTempBigger";
        public static final String rescheduleIfViolatedTemperature = "rescheduleIfViolatedTemperature";
        public static final String rescheduleIfViolatedDuration = "rescheduleIfViolatedDuration";
        public static final String triggerIppIfDeltaSoc = "triggerIppIfDeltaSoCBigger";

        public static final String bitsPerSlot = "bitsPerSlot";
        public static final String timePerSlot = "timePerSlot";
    }

    public static final class PV {
        public static final String nominalPower = "nominalpower";
        public static final String complexPowerMax = "complexpowermax";
        public static final String cosPhiMax = "cosphimax";
        public static final String profileNominalPower = "profileNominalPower";
    }

    public static final class Battery {
        public static final String minChargingState = "minChargingState";
        public static final String maxChargingState = "maxChargingState";
        public static final String minDischargingPower = "minDischargingPower";
        public static final String maxDischargingPower = "maxDischargingPower";
        public static final String minChargingPower = "minChargingPower";
        public static final String maxChargingPower = "maxChargingPower";
        public static final String minInverterPower = "minInverterPower";
        public static final String maxInverterPower = "maxInverterPower";

        public static final String batteryCycle = "batteryCycle";
        public static final String batteryType = "batteryType";
        public static final String roomTemperature = "roomTemperature";
    }

    public static final class Appliances {
        public static final String firstTDoF = "devicemax1stdof";
        public static final String secondTDof = "device2nddof";
        public static final String averageYearlyRuns = "averageyearlyruns";
        public static final String probabilityFile = "probabilityfilename";
        public static final String configurationShares = "configurationshares";
        public static final String hybridFactor = "hybridFactor";
    }

    public static final class Baseload {
        public static final String yearlyConsumption = "baseloadyearlyconsumption";
        public static final String cosPhi = "baseloadcosphi";
        public static final String isInductive = "baseloadisinductive";
    }

    public static final class WaterDemand {
        public static final String sourceFile = "sourcefile";
        public static final String drawOffFile = "drawOffTypesFile";
        public static final String probabilitiesFile = "weekDayHourProbabilitiesFile";
        public static final String averageYearlyDemand = "avgYearlyDemamd";
    }

    public static final class WaterTank {
        public static final String tankCapacity = "tankCapacity";
        public static final String tankDiameter = "tankDiameter";
        public static final String initialTemperature = "initialTemperature";
        public static final String ambientTemperature = "ambientTemperature";
        public static final String standingHeatLossFactor = "standingHeatLossFactor";
    }

    public static final class TemperatureRestrictions {
        public static final String hotWaterStorageMinTemp = "currentHotWaterStorageMinTemp";
        public static final String hotWaterStorageMaxTemp = "currentHotWaterStorageMaxTemp";
        public static final String forcedOnHysteresis = "forcedOnHysteresis";
    }

    public static final class CHP {
        public static final String activePower = "typicalActivePower";
        public static final String thermalPower = "typicalThermalPower";
        public static final String additionalThermalPower = "typicalAddditionalThermalPower";
        public static final String gasPower = "typicalGasPower";
        public static final String hotWaterTankUUID = "hotWaterTankUuid";
        public static final String cosPhi = "cosPhi";
        public static final String fixedCostPerStart = "fixedCostPerStart";
        public static final String forcedOnOffStepMultiplier = "forcedOnOffStepMultiplier";
        public static final String forcedOffAdditionalCost = "forcedOffAdditionalCost";
        public static final String cervisiaStepSizeMultiplier = "chpOnCervisiaStepSizeMultiplier";
        public static final String minRuntime = "minRuntime";

        public static final String dachsHost = "dachshost";
        public static final String dachsPort = "dachsport";
    }

    public static final class GasBoiler {
        public static final String maxGasPower = "maxGasPower";
        public static final String maxHotWaterPower = "maxHotWaterPower";
        public static final String activePowerOn = "typicalActivePowerOn";
        public static final String activePowerOff = "typicalActivePowerOff";
        public static final String reactivePowerOn = "typicalReactivePowerOn";
        public static final String reactivePowerOff = "typicalReactivePowerOff";
        public static final String hotWaterStorageMinTemp = "minTemperature";
        public static final String hotWaterStorageMaxTemp = "maxTemperature";
    }

    public static final class IHE {
        public static final String temperatureSetting = "temperatureSetting";
    }

    public static final class Optimization {
        public static final String epsObjective = "epsoptimizationobjective";
        public static final String plsObjective = "plsoptimizationobjective";
        public static final String varObjective = "varoptimizationobjective";
        public static final String upperOverlimitFactor = "upperOverlimitFactor";
        public static final String lowerOverlimitFactor = "lowerOverlimitFactor";
        public static final String optimizationRandomSeed = "optimizationMainRandomSeed";
        public static final String stepSize = "stepSize";
        public static final String hotWaterTankUUID = "hotWaterTankUUID";
        public static final String coldWaterTankUUID = "coldWaterTankUUID";
    }

    public static final class EA {
        public static final String populationSize = "populationSize";
        public static final String maxEvaluations = "maxEvaluations";
        public static final String minDeltaFitnessPercent = "minDeltaFitnessPerc";
        public static final String maxGenerationsDeltaFitnessViolated = "maxGenerationsDeltaFitnessViolated";

        public static final String probability = "probability";
        public static final String solutionRepair = "solutionRepair";
        public static final String distributionIndex = "distributionIndex";

        public static final String mutation = "mutation";
        public static final String crossover = "crossover";
        public static final String selection = "selection";
    }

    public static final class EA_ALGORITHM {
        public static final String particlesToInform = "particlesToInform";
        public static final String singleThreaded = "singleThreaded";
    }

    public static final class EA_RECOMBINATION {
        public static final String points = "points";
        public static final String alpha = "alpha";

        public static final String cr = "cr";
        public static final String f = "f";
        public static final String k = "k";
        public static final String variant = "variant";
    }

    public static final class EA_MUTATION {
        public static final String autoProbMutationFactor = "autoProbMutationFactor";
        public static final String perturbation = "perturbation";
        public static final String maxIterations = "maxIterations";
    }

    public static final class EA_SELECTION {
        public static final String comparator = "comparator";
        public static final String toSelect = "solutionsToSelect";
        public static final String tournaments = "numberOfTournaments";
    }

    public static final class EA_MULTI_OBJECTIVE {
        public static final String ordering = "ordering";
        public static final String objective = "objective";
        public static final String objectiveWeights = "objectiveWeights";
    }

    public static final class Logging {
        public static final String logH0 = "logH0";
        public static final String logEpsPls = "logEpsPls";
        public static final String logDetailedPower = "logDetailedPower";
        public static final String logIntervals = "logIntervalls";
        public static final String logDevices = "logDevices";
        public static final String logBaseload = "logBaseload";
        public static final String logThermal = "logThermal";
        public static final String logWaterTank = "logWaterTank";
        public static final String logEA = "logEA";
        public static final String logSmartHeater = "logSmartHeater";
        public static final String loggingIntervals = "loggingIntervalls";
    }
}
