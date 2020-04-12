package constructsimulation.configuration.CAL;

import constructsimulation.configuration.CAL.additional.EnergyLogger;
import constructsimulation.configuration.CAL.additional.GUI;
import constructsimulation.configuration.CAL.additional.GeneralLogger;
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
    public static boolean useGUI = false;
    public static final boolean useEnergyLogger = true;
    public static final boolean useLoggerCom = true;


    private static void applyConfigurations() {
        //####################
        //#### GUI ####
        //####################

        //######################
        //#### EnergyLogger ####
        //######################

        //###################
        //#### LoggerCom ####
        //###################

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
        if (useEnergyLogger) {
            additional.add(EnergyLogger.generateEnergyLogger());
        }
        if (useLoggerCom) {
            additional.add(GeneralLogger.generateLogger());
        }

        return additional;
    }
}
