package osh.toolbox.appliance.dw;

import osh.configuration.appliance.*;
import osh.datatypes.commodity.Commodity;
import osh.toolbox.appliance.ToolApplianceConfiguration;
import osh.toolbox.appliance.ToolApplianceConfigurationProfile;
import osh.utils.csv.CSVImporter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.List;

/**
 * @author Ingo Mauser
 */
public class generate_DW_4CO_5PH_4PR_MC_EH {

    // for JAXBContext
    static final String contextPath = "osh.configuration.appliance";

    // device data
    static final ToolApplianceConfiguration[] wpConfigs = DW_4CO_5PH_4PR_MC_EH.configurations;

    static final String outputFile = "data/profiles/out/dw/DW_4CO_5PH_4PR_MC_EH.xml";


    public static void main(String[] args) {

        XsdApplianceProgramConfigurations xsdAPCs = new XsdApplianceProgramConfigurations();

        List<XsdApplianceProgramConfiguration> listCtWashingParameterConfiguration = xsdAPCs.getApplianceProgramConfiguration();

        for (ToolApplianceConfiguration wpConfig : wpConfigs) {

            XsdApplianceProgramConfiguration apc = new XsdApplianceProgramConfiguration();
            listCtWashingParameterConfiguration.add(apc);

            // attribute: washingParameterConfigurationID
            apc.setConfigurationID(wpConfig.configurationID);

            // attribute: washingParameterConfigurationName (optional)

            // element: Program (mandatory)
            XsdProgram program = new XsdProgram();
            program.setProgramID(wpConfig.program.programID);
            program.setProgramName(wpConfig.program.programName);
            XsdDescriptions desc = new XsdDescriptions();
            program.setDescriptions(desc);
            List<XsdDescription> desclist = desc.getDescription();
            {
                XsdDescription descEN = new XsdDescription();
                descEN.setLanguage("EN");
                descEN.setValue(wpConfig.program.descriptionEN);
                desclist.add(descEN);
            }
            {
                XsdDescription descDE = new XsdDescription();
                descDE.setLanguage("DE");
                descDE.setValue(wpConfig.program.descriptionDE);
                desclist.add(descDE);
            }
            apc.setProgram(program);

            // element: Extras (optional)

            // element: Parameters (optional)

            // element: LoadProfiles (mandatory)
            XsdLoadProfiles loadProfiles = new XsdLoadProfiles();
            apc.setLoadProfiles(loadProfiles);
            List<XsdLoadProfile> listCtLoadProfile = loadProfiles.getLoadProfile();
            for (ToolApplianceConfigurationProfile profile : wpConfig.profiles) {
                XsdLoadProfile loadProfile = new XsdLoadProfile();
                listCtLoadProfile.add(loadProfile);

                loadProfile.setId(profile.profileID);
                loadProfile.setName(profile.profileName);

                // Segments
                XsdPhases phases = new XsdPhases();
                loadProfile.setPhases(phases);
                List<XsdPhase> listPhases = phases.getPhase();
                int numberOfPhases = profile.phaseInputFiles.length;
                for (int i = 0; i < numberOfPhases; i++) {
                    XsdPhase phase = new XsdPhase();
                    phase.setId(i);
                    phase.setName(profile.phaseNames[i]);

                    listPhases.add(phase);

                    List<XsdTick> listPhase = phase.getTick();
                    int[][] phaseValues = CSVImporter.readInteger2DimArrayFromFile(profile.phaseInputFiles[i], ";", null);

                    if (phaseValues == null) {
                        System.out.println("ERROR");
                    }

                    for (int[] phaseValue : phaseValues) {
                        XsdTick tick = new XsdTick();
                        listPhase.add(tick);
                        List<XsdLoad> listLoad = tick.getLoad();

                        if (profile.activePowerColumn >= 0) {
                            XsdLoad ap = new XsdLoad();
                            listLoad.add(ap);
                            ap.setCommodity(Commodity.ACTIVEPOWER.toString());
                            ap.setValue(phaseValue[profile.activePowerColumn]);
                        }
                        if (profile.reactivePowerColumn >= 0) {
                            XsdLoad ap = new XsdLoad();
                            listLoad.add(ap);
                            ap.setCommodity(Commodity.REACTIVEPOWER.toString());
                            ap.setValue(phaseValue[profile.reactivePowerColumn]);
                        }
                        if (profile.naturalGasPowerColumn >= 0) {
                            XsdLoad ap = new XsdLoad();
                            listLoad.add(ap);
                            ap.setCommodity(Commodity.NATURALGASPOWER.toString());
                            ap.setValue(phaseValue[profile.naturalGasPowerColumn]);
                        }
                        if (profile.domesticHotWaterPowerColumn >= 0) {
                            XsdLoad ap = new XsdLoad();
                            listLoad.add(ap);
                            ap.setCommodity(Commodity.HEATINGHOTWATERPOWER.toString());
                            ap.setValue(phaseValue[profile.domesticHotWaterPowerColumn]);
                        }
                    }

                    // set length to min max lengths
                    phase.setMinLength(phaseValues.length);
                    phase.setMaxLength(phaseValues.length);
                    if (phaseValues.length == 1) {
                        phase.setMaxLength(86400);
                    }

                }

            }

        }

        // marshall to file
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(contextPath);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(xsdAPCs, new File(outputFile));

        } catch (JAXBException e3) {
            e3.printStackTrace();
        }

        System.out.println("DONE");

    }

}
