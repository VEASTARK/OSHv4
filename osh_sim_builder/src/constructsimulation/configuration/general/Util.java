package constructsimulation.configuration.general;

import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.system.ConfigurationParameter;

/**
 * A collection of helper methods for configuration parameters.
 *
 * @author Sebastian Kramer
 */
public class Util {

    public static ConfigurationParameter generateClassedParameter(Object parameterName, Object parameterValue) {
        return CreateConfigurationParameter.createConfigurationParameter(
                String.valueOf(parameterName),
                parameterValue.getClass().getName(),
                String.valueOf(parameterValue));
    }

    public static ConfigurationParameter generateParameter(Object parameterName, Object parameterValue) {
        return CreateConfigurationParameter.createConfigurationParameter(
                String.valueOf(parameterName),
                String.class.getName(),
                String.valueOf(parameterValue));
    }
}
