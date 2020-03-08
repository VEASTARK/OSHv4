package constructsimulation.configuration.EAL.HVAC;

import constructsimulation.configuration.EAL.HVAC.consumers.Domestic;
import constructsimulation.configuration.EAL.HVAC.consumers.SpaceHeating;
import osh.configuration.eal.AssignedDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the EAL-HVAC consumers.
 *
 * @author Sebastian Kramer
 */
public class HVACConsumers {

    //############################################
    //########  Type of Consumers to use  ########
    //############################################
    public static boolean useDomestic = true;
    public static boolean useSpaceHeating = true;
    public static boolean useSpaceCooling = false;


    private static void applyConfigurations() {
        //##################
        //#### Domestic ####
        //##################
        //use vdi-6002 drawoff profiles
        Domestic.useVDI6002Simulator = true;


        //#################
        //#### Heating ####
        //#################
    }

    /**
     * Generates the configuration files for all HVAC-consumers with the set parameters.
     *
     * @return the configuration files for all HVAC-consumers
     */
    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> consumers = new ArrayList<>();
        if (useDomestic) {
            consumers.add(Domestic.generateDomestic());
        }
        if (useSpaceHeating) {
            consumers.add(SpaceHeating.generateHeating());
        }
        return consumers;
    }
}
