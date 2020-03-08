package constructsimulation.configuration.CAL;

import constructsimulation.configuration.CAL.signals.EPS;
import constructsimulation.configuration.CAL.signals.PLS;
import constructsimulation.datatypes.EPSTypes;
import constructsimulation.datatypes.PLSTypes;
import osh.configuration.cal.AssignedComDevice;
import osh.datatypes.commodity.AncillaryCommodity;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the CAL signals.
 *
 * @author Sebastian Kramer
 */
public class CALSignals {

    //##########################################
    //########  Type of Signals to use  ########
    //##########################################
    public static final boolean useEPS = true;
    public static final boolean usePLS = true;

    private static void applyConfigurations() {
        //#############
        //#### EPS ####
        //#############

        // Multi-Commodity
//        EPS.epsType = EPSTypes.H0;
//        EPS.epsType = EPSTypes.STEPS; // HOCH_TIEF
//        EPS.epsType = EPSTypes.PVFEEDIN;
//        EPS.epsType = EPSTypes.CSV;
//        EPS.epsType = EPSTypes.MC_FLAT; // FLAT-GERMAN-2015-TARIFF
//        EPS.epsType = EPSTypes.WIKHOURLY2015; // WIK-2015 Hourly Based Tariff
//        EPS.epsType = EPSTypes.WIKHOURLY2020; // WIK-2020 Hourly Based Tariff
        EPS.epsType = EPSTypes.WIKHOURLY2025; // WIK-2025 Hourly Based Tariff
//        EPS.epsType = EPSTypes.WIKWEEKDAY2015; // WIK-2015 Hourly Based Tariff
//        EPS.epsType = EPSTypes.WIKWEEKDAY2020; // WIK-2020 Hourly Based Tariff
//        EPS.epsType = EPSTypes.WIKWEEKDAY2025; // WIK-2025 Hourly Based Tariff
//        EPS.epsType = EPSTypes.WIK_BASED_THESIS; // WIK based Tariff in Thesis
//        EPS.epsType = EPSTypes.HOURLY_ALTERNATING; // fluctuating between 2 prices every 2 hours
        EPS.ancillaryCommodities = new AncillaryCommodity[]{
                AncillaryCommodity.ACTIVEPOWEREXTERNAL,
                AncillaryCommodity.REACTIVEPOWEREXTERNAL,
                AncillaryCommodity.NATURALGASPOWEREXTERNAL,
                AncillaryCommodity.PVACTIVEPOWERFEEDIN,
                AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
                AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
                AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
        };

        //#############
        //#### PLS ####
        //#############

        PLS.plsType = PLSTypes.NORMAL;
//        PLS.plsType = PLSTypes.REMS;
    }

    /**
     * Generates the configuration files for all CAL signal producers with the set parameters.
     *
     * @return the configuration files for all signal producers
     */
    public static List<AssignedComDevice> generateDevices(boolean applyConfigurations) {
        if (applyConfigurations) applyConfigurations();

        List<AssignedComDevice> signals = new ArrayList<>();
        if (useEPS) {
            signals.add(EPS.generateEps());
        }
        if (usePLS) {
            signals.add(PLS.generatePLS());
        }

        return signals;
    }
}
