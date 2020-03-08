package constructsimulation.configuration.CAL;

import constructsimulation.configuration.CAL.additional.GUI;
import osh.configuration.cal.AssignedComDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for additional CAL devices.
 *
 * @author Sebastian Kramer
 */
public class CALAdditional {

    //#########################################################
    //########  Type of additional CAL-Drivers to use  ########
    //#########################################################
    public static boolean useGUI = true;


    private static void applyConfigurations() {
        //####################
        //#### GUI ####
        //####################

    }

    /**
     * Generates the configuration files for all CAL additional devices with the set parameters.
     *
     * @return the configuration files for all additional devices
     */
    public static List<AssignedComDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedComDevice> additional = new ArrayList<>();
        if (useGUI) {
            additional.add(GUI.generateGUI());
        }

        return additional;
    }
}
