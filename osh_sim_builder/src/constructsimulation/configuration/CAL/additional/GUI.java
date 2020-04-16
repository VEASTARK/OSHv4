package constructsimulation.configuration.CAL.additional;

import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.generation.device.CreateComDevice;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;

import java.util.UUID;

/**
 * Generator and default configuration storage for the CAL GUI.
 *
 * @author Sebastian Kramer
 */
public class GUI {

    /*

        This class serves as a storage for all default values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static final UUID guiUUID = UUIDStorage.comDeviceIdGui;


    public static final String guiComManager = osh.mgmt.commanager.GuiComManager.class.getName();
    public static final String guiComDriver = osh.comdriver.simulation.GuiComDriver.class.getName();

    /**
     * Generates the configuration file for the GUI with the set parameters.
     *
     * @return the configuration file for the GUI
     */
    public static AssignedComDevice generateGUI() {

        return CreateComDevice.createComDevice(
                ComDeviceTypes.GUI,
                guiUUID,
                guiComDriver,
                guiComManager);
    }
}
