package constructsimulation.configuration.EAL;

import constructsimulation.configuration.EAL.HVAC.HVACConsumers;
import constructsimulation.configuration.EAL.HVAC.HVACProducers;
import constructsimulation.configuration.EAL.HVAC.HVACStorage;
import constructsimulation.configuration.EAL.electric.ElectricConsumers;
import constructsimulation.configuration.EAL.electric.ElectricProducers;
import constructsimulation.configuration.EAL.electric.ElectricStorage;
import osh.configuration.eal.EALConfiguration;

/**
 * Generation class for the {@link EALConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class GenerateEAL {

    /**
     * Generates the EAL configuration file.
     *
     * @return the EAL configuration file
     */
    public static EALConfiguration generateEAL(boolean applyConfigurations) {
        EALConfiguration ealConfig = new EALConfiguration();

        ealConfig.getAssignedDevices().addAll(ElectricConsumers.generateDevices(applyConfigurations));
        ealConfig.getAssignedDevices().addAll(ElectricProducers.generateDevices(applyConfigurations));
        ealConfig.getAssignedDevices().addAll(ElectricStorage.generateDevices(applyConfigurations));

        ealConfig.getAssignedDevices().addAll(HVACConsumers.generateDevices(applyConfigurations));
        ealConfig.getAssignedDevices().addAll(HVACProducers.generateDevices(applyConfigurations));
        ealConfig.getAssignedDevices().addAll(HVACStorage.generateDevices(applyConfigurations));

        return ealConfig;
    }
}
