package constructsimulation.configuration.EAL.electric;

import constructsimulation.configuration.EAL.electric.consumers.ApplianceType;
import constructsimulation.configuration.EAL.electric.consumers.Appliances;
import constructsimulation.configuration.EAL.electric.consumers.Baseload;
import osh.configuration.eal.AssignedDevice;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Kramer
 */
public class ElectricConsumers {

    //############################################
    //########  Type of Consumers to use  ########
    //############################################
    public static boolean useBaseload = true;
    public static boolean useAppliances = true;

    private static void applyConfigurations() {
        //##################
        //#### Baseload ####
        //##################


        //####################
        //#### Appliances ####
        //####################

        //Deferrable
        Appliances.applianceTypesToUse = new ApplianceType[]{
                ApplianceType.DELAYABLE,    //DW
                ApplianceType.STANDARD,     //IH
                ApplianceType.STANDARD,     //OV
                ApplianceType.DELAYABLE,    //TD
                ApplianceType.DELAYABLE     //WM
        };

        //Interruptible
//        Appliances.applianceTypesToUse = new ApplianceType[]{
//                ApplianceType.INTERRUPTIBLE,    //DW
//                ApplianceType.STANDARD,         //IH
//                ApplianceType.STANDARD,         //OV
//                ApplianceType.INTERRUPTIBLE,    //TD
//                ApplianceType.INTERRUPTIBLE     //WM
//        };

        //Hybrid
//        Appliances.applianceTypesToUse = new ApplianceType[]{
//                ApplianceType.HYBRID,    //DW
//                ApplianceType.HYBRID,    //IH
//                ApplianceType.HYBRID,    //OV
//                ApplianceType.HYBRID,    //TD
//                ApplianceType.HYBRID     //WM
//        };

        //Hybrid-Deferrable
//        Appliances.applianceTypesToUse = new ApplianceType[]{
//                ApplianceType.HYBRID_DELAYABLE,     //DW
//                ApplianceType.HYBRID,               //IH
//                ApplianceType.HYBRID,               //OV
//                ApplianceType.HYBRID_DELAYABLE,     //TD
//                ApplianceType.HYBRID_DELAYABLE      //WM
//        };

        //Hybrid-Interruptible
//        Appliances.applianceTypesToUse = new ApplianceType[]{
//                ApplianceType.HYBRID_INTERRUPTIBLE,     //DW
//                ApplianceType.HYBRID,                   //IH
//                ApplianceType.HYBRID,                   //OV
//                ApplianceType.HYBRID_INTERRUPTIBLE,     //TD
//                ApplianceType.HYBRID_INTERRUPTIBLE      //WM
//        };

        //Hybrid-Single
//        Appliances.applianceTypesToUse = new ApplianceType[]{
//                ApplianceType.HYBRID_SINGLE,     //DW
//                ApplianceType.HYBRID_SINGLE,                   //IH
//                ApplianceType.HYBRID_SINGLE,                   //OV
//                ApplianceType.HYBRID_SINGLE,     //TD
//                ApplianceType.HYBRID_SINGLE      //WM
//        };

        //1st-tdof
        Appliances.firstTDOF = new Duration[]{
                Duration.ofHours(12),
                Duration.ZERO,
                Duration.ZERO,
                Duration.ofHours(12),
                Duration.ofHours(12)
        };
    }

    public static List<AssignedDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedDevice> consumers = new ArrayList<>();
        if (useBaseload) {
            consumers.add(Baseload.generateBaseload());
        }
        if (useAppliances) {
            consumers.addAll(Appliances.generateAppliances());
        }

        return consumers;
    }
}
