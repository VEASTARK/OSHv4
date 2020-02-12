package constructsimulation.configuration.CAL;

import osh.configuration.cal.CALConfiguration;

/**
 *  Generation class for the {@link CALConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class GenerateCAL {

    /**
     * Generates the CAL configuration file.
     *
     * @return the CAL configuration file
     */
    public static CALConfiguration generateCAL(boolean applyConfigurations) {
        CALConfiguration calConfig = new CALConfiguration();

        calConfig.getAssignedComDevices().addAll(CALSignals.generateDevices(applyConfigurations));
        calConfig.getAssignedComDevices().addAll(CALAdditional.generateDevices(applyConfigurations));
        
        return calConfig;
    }
}
