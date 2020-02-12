package constructsimulation.configuration.general;

import constructsimulation.configuration.CAL.CALAdditional;
import constructsimulation.configuration.EAL.HVAC.HVACConsumers;
import constructsimulation.configuration.EAL.HVAC.HVACProducers;
import constructsimulation.configuration.EAL.HVAC.HVACStorage;
import constructsimulation.configuration.EAL.electric.ElectricConsumers;
import constructsimulation.configuration.EAL.electric.ElectricProducers;
import constructsimulation.configuration.EAL.electric.ElectricStorage;

/**
 * Utility class, defining which sub-components should be included in the configuration.
 *
 * @author Sebastian Kramer
 */
public class AllComponents {

    public static void applyConfigurations() {
        //hvacProducers
        HVACProducers.useCHP = true;
        HVACProducers.useGasHeating = true;
        HVACProducers.useIHE = false;

        //hvacConsumers
        HVACConsumers.useDomestic = true;
        HVACConsumers.useSpaceHeating = true;

        //hvacStorage
        HVACStorage.useHotWaterStorage = true;
        HVACStorage.useColdWaterStorage = false;


        //electricProducers
        ElectricProducers.usePV = true;

        //electricConsumers
        ElectricConsumers.useBaseload = true;
        ElectricConsumers.useAppliances = true;

        //electricStorage
        ElectricStorage.useBattery = false;

        //GUI
        CALAdditional.useGUI = true;
    }
}
