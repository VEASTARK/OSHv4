package constructsimulation.configuration.EAL.electric;

import constructsimulation.configuration.EAL.electric.producers.PV;
import osh.configuration.eal.AssignedDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the EAL electric producers.
 *
 * @author Sebastian Kramer
 */
public class ElectricProducers {

    //############################################
    //########  Type of Producers to use  ########
    //############################################
    public static boolean usePV = true;

    private static void applyConfigurations() {

        //############
        //#### PV ####
        //############

        //holl-data
        PV.usePVHOLL = true;
        PV.usePVEv0 = false;

        //nominal power
        PV.wattsPeak = 4000;
    }

    /**
     * Generates the configuration files for all electric producers with the set parameters.
     *
     * @return the configuration files for all electric producers
     */
    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> producers = new ArrayList<>();
        if (usePV) {
            producers.add(PV.generatePV());
        }

        return producers;
    }
}
