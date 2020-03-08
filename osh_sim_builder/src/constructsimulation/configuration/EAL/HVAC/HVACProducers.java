package constructsimulation.configuration.EAL.HVAC;

import constructsimulation.configuration.EAL.HVAC.producers.CHP;
import constructsimulation.configuration.EAL.HVAC.producers.GasHeating;
import constructsimulation.configuration.EAL.HVAC.producers.IHE;
import osh.configuration.eal.AssignedDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the EAL-HVAC producers.
 *
 * @author Sebastian Kramer
 */
public class HVACProducers {

    //############################################
    //########  Type of Producers to use  ########
    //############################################
    public static boolean useCHP = true;
    public static boolean useGasHeating = true;
    public static boolean useIHE = false;

    private static void applyConfigurations() {
        //#############
        //#### CHP ####
        //#############

        //non-controllable chp
        CHP.controllableCHP = true;


        //####################
        //#### GasHeating ####
        //####################

        //#############
        //#### IHE ####
        //#############
    }

    /**
     * Generates the configuration files for all HVAC-producers with the set parameters.
     *
     * @return the configuration files for all HVAC-producers
     */
    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> producers = new ArrayList<>();
        if (useCHP) {
            producers.add(CHP.generateCHP());
        }
        if (useGasHeating) {
            producers.add(GasHeating.generateGas());
        }
        if (useIHE) {
            producers.add(IHE.generateIHE());
        }

        return producers;
    }
}
