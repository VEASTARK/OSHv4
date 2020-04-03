package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.HouseConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *  Generation class for the {@link OCConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class GenerateOC {

    public static UUID globalOCUuid = UUIDStorage.globalOCUuid;


    //step size of the optimization algorithm (calculation interval)
    public static int escStepSize = 60;

    public static String globalObserverClass = osh.mgmt.globalobserver.OSHGlobalObserver.class.getName();

    public static String globalControllerClass = osh.mgmt.globalcontroller.OSHGlobalControllerJMetal.class.getName();

    /**
     * Generates the OC configuration file.
     *
     * @return the OC configuration file
     */
    public static OCConfiguration generateOCConfig() {
        OCConfiguration ocConfig = new OCConfiguration();

        ocConfig.setGlobalObserverClass(globalObserverClass);
        ocConfig.setGlobalControllerClass(globalControllerClass);

        ocConfig.setOptimizationMainRandomSeed(String.valueOf(HouseConfig.optimizationMainRandomSeed));


        ocConfig.setGlobalOcUuid(globalOCUuid.toString());
        ocConfig.setEaConfiguration(EAConfig.generateEAConfig());
        ocConfig.setCostConfiguration(CostConfig.generateCostConfig());

        Map<String, String> params = new HashMap<>();

        params.put(ParameterConstants.Optimization.overlimitFactor, String.valueOf(CostConfig.overLimitFactor));

        params.put(ParameterConstants.Optimization.stepSize, String.valueOf(escStepSize));

        params.put(ParameterConstants.Optimization.hotWaterTankUUID, String.valueOf(UUIDStorage.hotWaterTankUUID));
        params.put(ParameterConstants.Optimization.coldWaterTankUUID, String.valueOf(UUIDStorage.coldWaterTankUUID));

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            ocConfig.getGlobalControllerParameters().add(cp);
        }

        return ocConfig;
    }
}
