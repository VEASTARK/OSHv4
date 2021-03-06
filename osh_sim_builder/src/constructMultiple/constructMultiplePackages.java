package constructMultiple;

import constructsimulation.configuration.CAL.CALAdditional;
import constructsimulation.configuration.CAL.signals.EPS;
import constructsimulation.configuration.EAL.HVAC.HVACProducers;
import constructsimulation.configuration.EAL.HVAC.producers.CHP;
import constructsimulation.configuration.EAL.HVAC.storage.HotWaterStorage;
import constructsimulation.configuration.EAL.electric.ElectricProducers;
import constructsimulation.configuration.EAL.electric.ElectricStorage;
import constructsimulation.configuration.EAL.electric.consumers.Appliances;
import constructsimulation.configuration.EAL.electric.producers.PV;
import constructsimulation.configuration.OC.CostConfig;
import constructsimulation.configuration.OC.EAConfig;
import constructsimulation.configuration.OC.GenerateOC;
import constructsimulation.configuration.OSH.GenerateOSH;
import constructsimulation.configuration.general.Generate;
import constructsimulation.configuration.general.HouseConfig;
import constructsimulation.datatypes.EPSTypes;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class constructMultiplePackages {

    private static final String saveDirectory = "multiplePackages";
    private static PrintWriter pw;

    public static void main(String[] args) throws FileNotFoundException {

        new File(saveDirectory).mkdirs();

        HashMap<String, ArrayList<?>> totalValues = constructMultipleData.produceMap();
        ArrayList<Entry<String, ArrayList<?>>> entries = mapToEntryList(totalValues);
        ArrayList<HashMap<String, ?>> permutedMapList = new ArrayList<>();
        permuteMap(permutedMapList, 0, entries, new HashMap<>());

        File printFile = new File(saveDirectory + "/configNames.txt");
        if (printFile.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do {
                System.out.println();
                System.out.println("ERROR: file for config names already exists. Delete (y/n)?");
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (!line.equals("y") && !line.equals("n"));

            if (line.equals("y")) {
                deleteDirectory(printFile);
                if (printFile.exists()) throw new RuntimeException("It still exists!");
            } else {
                System.out.println("Aborting...");
                System.exit(1);
            }
        }
        pw = new PrintWriter(new File(saveDirectory + "/configNames.txt"));

        for (HashMap<String, ?> config : permutedMapList) {
            buildForConfig(config);
        }

        pw.flush();
        pw.close();
    }

    private static void buildForConfig(HashMap<String, ?> config) {
        CALAdditional.useGUI = false;

        if (config.containsKey("eps"))
            EPS.epsType = (EPSTypes) config.get("eps");
        if (config.containsKey("devices"))
            Appliances.applianceTypesToUse =
                    DeviceConfiguration.getAppliancesValues((DeviceConfiguration) config.get("devices"));
        if (config.containsKey("heatingOrBattery")) {
            switch ((BatteryOrHeating) config.get("heatingOrBattery")) {
                case BATTERY:
                    ElectricStorage.useBattery = true;
                    HVACProducers.useIHE = false;
                    break;
                case INSERTHEATING:
                    ElectricStorage.useBattery = false;
                    HVACProducers.useIHE = true;
                    break;
                case NONE:
                    ElectricStorage.useBattery = false;
                    HVACProducers.useIHE = false;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }
        if (config.containsKey("tankSizes"))
            HotWaterStorage.tankSize = (Integer) config.get("tankSizes");
        if (config.containsKey("pvType")) {
            PVConfiguration pvConfig = (PVConfiguration) config.get("pvType");
            PV.pvComplexPowerMax = pvConfig.pvComplexPowerMax;
            PV.pvCosPhiMax = pvConfig.pvCosPhiMax;
            PV.wattsPeak = pvConfig.pvNominalPower;
            PV.usePVHOLL = pvConfig.usePVRealHOLL;
            ElectricProducers.usePV = pvConfig.usePVRealHOLL;
        }
        if (config.containsKey("chpType")) {
            CHPConfiguration chpConfig = (CHPConfiguration) config.get("chpType");
            switch (chpConfig) {
                case NONE: {
                    HVACProducers.useCHP = false;
                    CHP.controllableCHP = false;
                    HVACProducers.useGasHeating = true;
                    break;
                }
                case DUMB: {
                    HVACProducers.useCHP = true;
                    CHP.controllableCHP = false;
                    HVACProducers.useGasHeating = false;
                    break;
                }
                case INTELLIGENT: {
                    HVACProducers.useCHP = true;
                    CHP.controllableCHP = true;
                    HVACProducers.useGasHeating = false;
                    break;
                }
                default:
                    throw new IllegalArgumentException();
            }
        }
        if (config.containsKey("persons")) {
            HouseConfig.personCount = (Integer) config.get("persons");
        }
        if (config.containsKey("escResolution")) {
            GenerateOC.escStepSize = (Integer) config.get("escResolution");
        }
        if (config.containsKey("autoProbFactor")) {
            EAConfig.autoProbMutationFactor = (Double) config.get("autoProbFactor");
        }
        if (config.containsKey("crossoverProb")) {
            EAConfig.crossoverProbability = (Double) config.get("crossoverProb");
        }
        if (config.containsKey("epsOptimisationObjective")) {
            CostConfig.epsOptimizationObjective = (Integer) config.get("epsOptimisationObjective");
        }
        if (config.containsKey("pls")) {
            PLSType plsType = (PLSType) config.get("pls");

            if (plsType != PLSType.NONE) {
                CostConfig.plsOptimizationObjective = 1;
            } else {
                CostConfig.plsOptimizationObjective = 0;
            }

            if (plsType == PLSType.HALF_POS) {
                CostConfig.upperOverLimitFactor = 1.0;
                CostConfig.lowerOverLimitFactor = 0.0;
            } else if (plsType == PLSType.HALF_NEG) {
                CostConfig.upperOverLimitFactor = 0.0;
                CostConfig.lowerOverLimitFactor = 1.0;
            } else if (plsType == PLSType.FULL) {
                CostConfig.upperOverLimitFactor = 1.0;
                CostConfig.lowerOverLimitFactor = 1.0;
            } else {
                CostConfig.upperOverLimitFactor = 0.0;
                CostConfig.lowerOverLimitFactor = 0.0;
            }
        }


        setLogValues();

        String name = configToString(config);
        pw.println("\"" + name + "\",");

        Generate.generate(saveDirectory + "/" + name + "/");
    }

    private static void setLogValues() {
        GenerateOSH.logH0 = constructMultipleData.logH0;
        GenerateOSH.logEpsPls = constructMultipleData.logEpsPls;
        GenerateOSH.logIntervals = constructMultipleData.logIntervals;
        GenerateOSH.logDevices = constructMultipleData.logDevices;
        GenerateOSH.logDetailedPower = constructMultipleData.logDetailedPower;
        GenerateOSH.logHotWater = constructMultipleData.logHotWater;
        GenerateOSH.logWaterTank = constructMultipleData.logWaterTank;
        GenerateOSH.logGA = constructMultipleData.logGA;
        GenerateOSH.logSmartHeater = constructMultipleData.logSmartHeater;
    }

    private static String configToString(HashMap<String, ?> config) {
        String name = "";
        if (config.containsKey("persons"))
            name += config.get("persons") + "pax_";
        if (config.containsKey("devices"))
            name += DeviceConfiguration.toShortName((DeviceConfiguration) config.get("devices")) + "_";
        if (config.containsKey("heatingOrBattery"))
            name += BatteryOrHeating.toShortString((BatteryOrHeating) config.get("heatingOrBattery")) + "_";
        if (config.containsKey("tankSizes"))
            name += config.get("tankSizes") + "_";
        if (config.containsKey("pvType")) {
            PVConfiguration pvConfig = (PVConfiguration) config.get("pvType");
            name += pvConfig.toShortName() + "_";
        }
        if (config.containsKey("chpType"))
            name += CHPConfiguration.toShortName((CHPConfiguration) config.get("chpType")) + "_";
        if (config.containsKey("eps"))
            name += typeToName((EPSTypes) config.get("eps")) + "_";
//		if (config.containsKey("compression")) {
//			name += ((CompressionConfiguration) config.get("compression")).toShortName() + "_";
//		}
//		if (config.containsKey("escResolution")) {
//			name += epsResToName((int) config.get("escResolution")) + "_";
//		}
//		if (config.containsKey("autoProbFactor")) {
//			name += "mu" + (double) config.get("autoProbFactor") + "_";
//		}
//		if (config.containsKey("crossoverProb")) {
//			name += "xo" + (double) config.get("crossoverProb");
//		}
        if (config.containsKey("pls")) {
            name += PLSType.toShortString(((PLSType) config.get("pls")));
        }

        if (name.endsWith("_")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    private static ArrayList<Entry<String, ArrayList<?>>> mapToEntryList(HashMap<String, ArrayList<?>> map) {
        return new ArrayList<>(map.entrySet());
    }

    private static void permuteMap(
            ArrayList<HashMap<String, ?>> result,
            int depth,
            ArrayList<Entry<String, ArrayList<?>>> src,
            HashMap<String, ?> current) {

        if (depth == src.size()) {
            result.add(current);
            return;
        }

        Entry<String, ArrayList<?>> varEntry = src.get(depth);
        for (Object varValue : varEntry.getValue()) {
            HashMap<String, Object> copy = copyMap(current);
            copy.put(varEntry.getKey(), varValue);
            permuteMap(result, depth + 1, src, copy);
        }
    }

    private static HashMap<String, Object> copyMap(HashMap<String, ?> src) {
        return new HashMap<>(src);
    }

    private static String typeToName(EPSTypes type) {
        switch (type) {
            case CSV:
                return "csv";
            case H0:
                return "h0";
            case MC_FLAT:
                return "mcflat";
            case REMS:
                return "rems";
            case STEPS:
                return "steps";
            case WIK_BASED_THESIS:
                return "wik-based-thesis";
            case WIKHOURLY2015:
                return "wik-hourly-2015";
            case WIKHOURLY2020:
                return "wik-hourly-2020";
            case WIKHOURLY2025:
                return "wik-hourly-2025";
            case WIKWEEKDAY2015:
                return "wik-weekly-2015";
            case WIKWEEKDAY2020:
                return "wik-weekly-2020";
            case WIKWEEKDAY2025:
                return "wik-weekly-2025";
            case HOURLY_ALTERNATING:
                return "alt";
            default:
                return null;
        }
    }

    @SuppressWarnings("unused")
    private static String epsResToName(int epsResolution) {
        return "escRes-" + epsResolution;
    }

    private static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return (path.delete());
    }

}
