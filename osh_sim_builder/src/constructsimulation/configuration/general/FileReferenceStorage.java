package constructsimulation.configuration.general;

/**
 * Static storage for references to file paths.
 *
 * @author Sebastian Kramer
 */
public class FileReferenceStorage {

    //domestic
    public static final String vdi_drawoffProfileFileName = "configfiles/dhw/eu/eu_drawofftypesfile.csv";
    public static final String vdi_weekDayHourProbabilityFileName =
            "configfiles/dhw/vdi_6002_weekday_hour_drawoff_probabilities.csv";
    public static final String eshl_drawOffProfileName = "configfiles/dhw/domestichotwater_%d.csv";
    //heating
    public static final String eshl_heatingDrawOffFileName = "configfiles/heating/heating_demand_%d.csv";
    //pv
    public static final String ev0_filePath = "configfiles/pv/EV0Profile";
    public static final String holl_filePath = "configfiles/pv/holl2013cleaned";
    //eps
    public static final String csvPriceDynamicFilePath = "configfiles/externalSignal/priceDynamic.csv";
    public static final String pvFeedInEPEXFilePath = "configfiles/externalSignal/pricePVFeedInEPEX.csv";
    //system
    public static final String configFilesPath = "configfiles/";
    public static final String systemPath = "system/";
    public static final String EALConfigFileName = "EALConfig";
    public static final String OCConfigFileName = "OCConfig";
    public static final String CALConfigFileName = "CALConfig";
    public static final String OSHConfigFileName = "OSHConfig";
    //grids
    public static final String simulationElelectricalGrid = "configfiles/grids/SimulationElectricalGrid.xml";
    public static final String simulationThermalGrid = "configfiles/grids/SimulationThermalGrid.xml";
    public static final String eshlElelectricalGrid = "configfiles/grids/ESHLElectricalGrid.xml";
    public static final String eshlThermalGrid = "configfiles/grids/ESHLThermalGrid.xml";
    //h0
    public static final String h0Filename15Min = "configfiles/h0/H0Profile15MinWinterSummerIntermediate.csv";
    //appliances
    public static final String[] probabilityFilePaths = {
            "configfiles/appliance/dw/DW_prob.csv", //DW
            "configfiles/appliance/ih/IH_prob.csv", //COOKTOP
            "configfiles/appliance/ov/OV_prob.csv", //OVEN
            "configfiles/appliance/td/TD_prob.csv", //TD
            "configfiles/appliance/wm/WM_prob.csv"  //WM
    };
    public static final String[][] profileSourcesPaths = {
            //DW
            {
                    null,
                    "configfiles/appliance/dw/DW_4CO_1PH_1PR_SC_E.xml",
                    "configfiles/appliance/dw/DW_4CO_3PH_1PR_SC_E.xml",
                    "configfiles/appliance/dw/DW_4CO_5PH_1PR_SC_E.xml",
                    "configfiles/appliance/dw/DW_4CO_1PH_2PR_MC_EH.xml",
                    "configfiles/appliance/dw/DW_4CO_3PH_2PR_MC_EH.xml",
                    "configfiles/appliance/dw/DW_4CO_5PH_4PR_MC_EH.xml",
                    "configfiles/appliance/dw/DW_4CO_1PH_1PR_SC_H.xml",
            },
            //IH
            {
                    null,
                    "configfiles/appliance/ih/IH_3CO_1PH_1PR_SC_E.xml",
                    null,
                    null,
                    "configfiles/appliance/ih/IH_3CO_1PH_2PR_MC_EG.xml",
                    null,
                    null,
                    "configfiles/appliance/ih/IH_3CO_1PH_1PR_SC_G.xml"
            },
            //OV
            {
                    null,
                    "configfiles/appliance/ov/OV_3CO_1PH_1PR_SC_E.xml",
                    null,
                    null,
                    "configfiles/appliance/ov/OV_3CO_1PH_2PR_MC_EG.xml",
                    null,
                    null,
                    "configfiles/appliance/ov/OV_3CO_1PH_1PR_SC_G.xml"
            },
            //TD
            {
                    null,
                    "configfiles/appliance/td/TD_3CO_1PH_1PR_SC_E.xml",
                    "configfiles/appliance/td/TD_3CO_3PH_1PR_SC_E.xml",
                    "configfiles/appliance/td/TD_3CO_9PH_1PR_SC_E.xml",
                    "configfiles/appliance/td/TD_3CO_1PH_2PR_MC_EH.xml",
                    "configfiles/appliance/td/TD_3CO_3PH_2PR_MC_EH.xml",
                    "configfiles/appliance/td/TD_3CO_9PH_2PR_MC_EH.xml",
                    "configfiles/appliance/td/TD_3CO_1PH_1PR_SC_H.xml"
            },
            //WM
            {
                    null,
                    "configfiles/appliance/wm/WM_3CO_1PH_1PR_SC_E.xml",
                    "configfiles/appliance/wm/WM_3CO_3PH_1PR_SC_E.xml",
                    "configfiles/appliance/wm/WM_3CO_5PH_1PR_SC_E.xml",
                    "configfiles/appliance/wm/WM_3CO_1PH_2PR_MC_EH.xml",
                    "configfiles/appliance/wm/WM_3CO_3PH_2PR_MC_EH.xml",
                    "configfiles/appliance/wm/WM_3CO_5PH_2PR_MC_EH.xml",
                    "configfiles/appliance/wm/WM_3CO_1PH_1PR_SC_H.xml"
            },
    };
}
