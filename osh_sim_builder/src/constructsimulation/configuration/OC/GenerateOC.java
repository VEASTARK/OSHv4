package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.HouseConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.oc.GlobalControllerConfiguration;
import osh.configuration.oc.GlobalObserverConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants.Optimization;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *  Generation class for the {@link OCConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class GenerateOC {

    public static final UUID globalOCUuid = UUIDStorage.globalOCUuid;


    //step size of the optimization algorithm (calculation interval)
    public static int escStepSize = 60;

    //enforced delay between schedulings or at the start of the simulation
    public static final Duration delayBetweenScheduling = Duration.ofMinutes(1);
    public static final Duration delayAtStart = Duration.ofMinutes(1);

    public static final String globalObserverClass = osh.mgmt.globalobserver.OSHGlobalObserver.class.getName();
    public static final String globalControllerClass = osh.mgmt.globalcontroller.ModularGlobalController.class.getName();

    public static final Class<?>[] modules = new Class<?>[]{
            //communication
            osh.mgmt.globalcontroller.modules.communication.CommunicateCommandPredictionModule.class,
//            osh.mgmt.globalcontroller.modules.communication.CommunicateGUIModule.class,
            //scheduling
            osh.mgmt.globalcontroller.modules.scheduling.ExecuteSchedulingModule.class,
            osh.mgmt.globalcontroller.modules.scheduling.HandleSchedulingModule.class,
            //signals
            osh.mgmt.globalcontroller.modules.signals.HandleSignalsModule.class,
    };

    /**
     * Generates the OC configuration file.
     *
     * @return the OC configuration file
     */
    public static OCConfiguration generateOCConfig() {
        OCConfiguration ocConfig = new OCConfiguration();

        GlobalControllerConfiguration controllerConfiguration = new GlobalControllerConfiguration();
        GlobalObserverConfiguration observerConfiguration = new GlobalObserverConfiguration();

        controllerConfiguration.setClassName(globalControllerClass);
        controllerConfiguration.setOptimizationMainRandomSeed(HouseConfig.optimizationMainRandomSeed);
        controllerConfiguration.setCostConfiguration(CostConfig.generateCostConfig());
        controllerConfiguration.setEaConfiguration(EAConfig.generateEAConfig());

        for (Class<?> clazz : modules) {
            controllerConfiguration.getControllerModules().add(clazz.getName());
        }

        Map<String, String> params = new HashMap<>();

        params.put(Optimization.stepSize, String.valueOf(escStepSize));

        params.put(Optimization.hotWaterTankUUID, String.valueOf(UUIDStorage.hotWaterTankUUID));
        params.put(Optimization.coldWaterTankUUID, String.valueOf(UUIDStorage.coldWaterTankUUID));

        params.put(Optimization.delayAtStart, String.valueOf(delayAtStart.toSeconds()));
        params.put(Optimization.delayBetweenScheduling, String.valueOf(delayBetweenScheduling.toSeconds()));

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            controllerConfiguration.getGlobalControllerParameters().add(cp);
        }

        observerConfiguration.setClassName(globalObserverClass);

        ocConfig.setGlobalControllerConfiguration(controllerConfiguration);
        ocConfig.setGlobalObserverConfiguration(observerConfiguration);
        ocConfig.setGlobalOcUuid(globalOCUuid.toString());

        return ocConfig;
    }
}
