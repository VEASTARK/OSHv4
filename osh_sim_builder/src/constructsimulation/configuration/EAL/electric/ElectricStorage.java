package constructsimulation.configuration.EAL.electric;

import constructsimulation.configuration.EAL.electric.storage.Battery;
import osh.configuration.eal.AssignedDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the EAL electric storage.
 *
 * @author Sebastian Kramer
 */
public class ElectricStorage {

    //##########################################
    //########  Type of Storage to use  ########
    //##########################################
    public static boolean useBattery = false;


    private static void applyConfigurations() {
        //#################
        //#### Battery ####
        //#################

        //Lithium-Ionen-Battery
        Battery.batteryType = 1;
        //Blei-Gel-Battery
//        Battery.batteryType = 2;
    }

    /**
     * Generates the configuration files for all electric storage with the set parameters.
     *
     * @return the configuration files for all electric storage
     */
    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> storage = new ArrayList<>();
        if (useBattery) {
            storage.add(Battery.generateBattery());
        }

        return storage;
    }
}
