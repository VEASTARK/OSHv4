package constructsimulation.configuration.OSH;

import constructsimulation.configuration.general.HouseConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.datatypes.GridConfigurationWrapper;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.GridConfig;
import osh.configuration.system.OSHConfiguration;
import osh.utils.string.ParameterConstants;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Generation class for the {@link OSHConfiguration}
 *
 * @author Sebastian Kramer
 */
public class GenerateOSH {

    public static Map<GridConfigurationType, GridConfigurationWrapper[]> gridMap = new EnumMap<>(GridConfigurationType.class);

    static {
        gridMap.put(GridConfigurationType.SIMULATION, new GridConfigurationWrapper[]{
                new GridConfigurationWrapper(
                        "electrical",
                        "configfiles/grids/SimulationElectricalGrid.xml"),
                new GridConfigurationWrapper(
                        "thermal",
                        "configfiles/grids/SimulationThermalGrid.xml")});

        gridMap.put(GridConfigurationType.REAL, new GridConfigurationWrapper[]{
                new GridConfigurationWrapper(
                        "electrical",
                        "configfiles/grids/ESHLElectricalGrid.xml"),
                new GridConfigurationWrapper(
                        "thermal",
                        "configfiles/grids/ESHLThermalGrid.xml")});
    }

    //loggingIntervals for database
    //FORMAT: months, weeks, days
    //only the first non-zero value will be regarded, so {1, 3, 4} is the same as {1, 0, 0} and so on
    public static int[][] loggingIntervals = {
            {0, 0, 1},    // 1 day
            {0, 1, 0},    // 1 week
            {1, 0, 0}    // 1 month
    };

    public static boolean logH0 = false;
    public static boolean logEpsPls = false;
    public static boolean logIntervals = false;
    public static boolean logDevices = true;
    public static boolean logBaseload = false;
    public static boolean logDetailedPower = false;
    public static boolean logHotWater = false;
    public static boolean logWaterTank = true;
    public static boolean logGA = true;
    public static boolean logSmartHeater = false;

    /**
     * Generates the OSH configuration file.
     *
     * @return the OSH configuration file
     */
    public static OSHConfiguration generateOSH() {
        OSHConfiguration oshConfig = new OSHConfiguration();

        oshConfig.setRandomSeed(String.valueOf(HouseConfig.mainRandomSeed));

        oshConfig.setLogFilePath(String.valueOf(HouseConfig.defaultLogPath));

        oshConfig.setMeterUUID(UUIDStorage.meterUUID.toString());
        oshConfig.setHhUUID(HouseConfig.hhUUID.toString());

        oshConfig.setRunningType(HouseConfig.runningType);

        for (GridConfigurationWrapper grid : gridMap.get(HouseConfig.gridType)) {
            GridConfig gc = new GridConfig();
            gc.setGridType(grid.gridType);
            gc.setGridLayoutSource(grid.gridLayoutSource);
            oshConfig.getGridConfigurations().add(gc);
        }

        HashMap<String, String> params = new HashMap<>();

        params.put(ParameterConstants.Logging.logH0, String.valueOf(logH0));
        params.put(ParameterConstants.Logging.logEpsPls, String.valueOf(logEpsPls));
        params.put(ParameterConstants.Logging.logDetailedPower, String.valueOf(logDetailedPower));
        params.put(ParameterConstants.Logging.logIntervals, String.valueOf(logIntervals));
        params.put(ParameterConstants.Logging.logDevices, String.valueOf(logDevices));
        params.put(ParameterConstants.Logging.logBaseload, String.valueOf(logBaseload));
        params.put(ParameterConstants.Logging.logThermal, String.valueOf(logHotWater));
        params.put(ParameterConstants.Logging.logWaterTank, String.valueOf(logWaterTank));
        params.put(ParameterConstants.Logging.logEA, String.valueOf(logGA));
        params.put(ParameterConstants.Logging.logSmartHeater, String.valueOf(logSmartHeater));
        params.put(ParameterConstants.Logging.loggingIntervals, Arrays.toString(
                Arrays.stream(loggingIntervals).map(Arrays::toString).toArray(String[]::new)));

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            oshConfig.getEngineParameters().add(cp);
        }

        return oshConfig;
    }
}
