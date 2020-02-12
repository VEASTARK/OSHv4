package constructsimulation.configuration.CAL.signals;

import constructsimulation.configuration.general.GeneralConfig;
import constructsimulation.configuration.general.UUIDStorage;
import constructsimulation.datatypes.PLSTypes;
import constructsimulation.generation.device.CreateComDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;

import java.time.Duration;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generator and default configuration storage for the CAL power-limit-signals (pls).
 *
 * @author Sebastian Kramer
 */
public class PLS {

    /*

        This class serves as a storage for all default values and a producer of the finalized config
        DO NOT change anything if you merely wish to produce a new configuration file!

    */
    public static UUID plsUuid = UUIDStorage.plsSignalUuid;


    public static PLSTypes plsType = PLSTypes.NORMAL;
//	public static PLSTypes plsType = PLSTypes.REMS;

    public static Duration newSignalAfter = GeneralConfig.newSignalAfter;
    public static Duration signalPeriod = GeneralConfig.signalPeriod;

    //PLS
    public static int activeLowerLimit = -3000;
    public static int activeUpperLimit = 3000;
    public static int reactiveLowerLimit = -3000;
    public static int reactiveUpperLimit = 3000;

    public static EnumMap<PLSTypes, String> driverMap = new EnumMap<>(PLSTypes.class);
    public static String comManager = osh.mgmt.commanager.PlsProviderComManager.class.getName();

    static {
        driverMap.put(PLSTypes.NORMAL, osh.comdriver.FlatPlsProviderComDriver.class.getName());
        driverMap.put(PLSTypes.REMS, osh.rems.simulation.RemsPlsProviderComDriver.class.getName());
    }

    /**
     * Generates the configuration file for the pls with the set parameters.
     *
     * @return the configuration file for the pls
     */
    public static AssignedComDevice generatePLS() {

        Map<String, String> params = new HashMap<>();
        params.put("newSignalAfterThisPeriod", String.valueOf(newSignalAfter.toSeconds()));
        params.put("signalPeriod", String.valueOf(signalPeriod.toSeconds()));
        params.put("activeLowerLimit", String.valueOf(activeLowerLimit));
        params.put("activeUpperLimit", String.valueOf(activeUpperLimit));
        params.put("reactiveLowerLimit", String.valueOf(reactiveLowerLimit));
        params.put("reactiveUpperLimit", String.valueOf(reactiveUpperLimit));

        AssignedComDevice dev = CreateComDevice.createComDevice(
                ComDeviceTypes.ELECTRICITY,
                plsUuid,
                driverMap.get(plsType),
                comManager);

        for (Map.Entry<String, String> en : params.entrySet()) {
            ConfigurationParameter cp = CreateConfigurationParameter.createConfigurationParameter(
                    en.getKey(),
                    "String",
                    en.getValue());
            dev.getComDriverParameters().add(cp);
        }
        return dev;
    }
}
