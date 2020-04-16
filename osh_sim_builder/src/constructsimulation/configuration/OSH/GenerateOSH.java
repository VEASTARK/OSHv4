package constructsimulation.configuration.OSH;

import constructsimulation.configuration.general.FileReferenceStorage;
import constructsimulation.configuration.general.HouseConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.datatypes.GridConfigurationWrapper;
import osh.configuration.system.GridConfig;
import osh.configuration.system.OSHConfiguration;

import java.util.EnumMap;
import java.util.Map;

/**
 * Generation class for the {@link OSHConfiguration}
 *
 * @author Sebastian Kramer
 */
public class GenerateOSH {

    public static final Map<GridConfigurationType, GridConfigurationWrapper[]> gridMap = new EnumMap<>(GridConfigurationType.class);

    static {
        gridMap.put(GridConfigurationType.SIMULATION, new GridConfigurationWrapper[]{
                new GridConfigurationWrapper(
                        "electrical",
                        FileReferenceStorage.simulationElelectricalGrid),
                new GridConfigurationWrapper(
                        "thermal",
                        FileReferenceStorage.simulationThermalGrid)});

        gridMap.put(GridConfigurationType.REAL, new GridConfigurationWrapper[]{
                new GridConfigurationWrapper(
                        "electrical",
                        FileReferenceStorage.eshlElelectricalGrid),
                new GridConfigurationWrapper(
                        "thermal",
                        FileReferenceStorage.eshlThermalGrid)});
    }

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

        return oshConfig;
    }
}
