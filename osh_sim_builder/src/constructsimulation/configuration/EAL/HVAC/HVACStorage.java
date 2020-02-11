package constructsimulation.configuration.EAL.HVAC;

import constructsimulation.configuration.EAL.HVAC.storage.ColdWaterStorage;
import constructsimulation.configuration.EAL.HVAC.storage.HotWaterStorage;
import osh.configuration.eal.AssignedDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the EAL storage.
 *
 * @author Sebastian Kramer
 */
public class HVACStorage {

    //##########################################
    //########  Type of Storage to use  ########
    //##########################################
    public static boolean useHotWaterStorage = true;
    public static boolean useColdWaterStorage = false;


    private static void applyConfigurations() {
        //#########################
        //#### HotWaterStorage ####
        //#########################

        //seasonal storage
        HotWaterStorage.initialTemperature = 70.0;


        //##########################
        //#### ColdWaterStorage ####
        //##########################
        ColdWaterStorage.tankSize = 3000.0;
    }

    /**
     * Generates the configuration files for all HVAC-storage with the set parameters.
     *
     * @return the configuration files for all HVAC-storage
     */
    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> storage = new ArrayList<>();

        if (useHotWaterStorage) {
            storage.add(HotWaterStorage.generateHotStorage());
        }
        if (useColdWaterStorage) {
            storage.add(ColdWaterStorage.generateColdStorage());
        }

        return storage;
    }
}
