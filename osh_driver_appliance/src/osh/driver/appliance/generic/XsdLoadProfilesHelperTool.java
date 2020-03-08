package osh.driver.appliance.generic;

import osh.configuration.appliance.*;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;

import java.util.List;

/**
 * @author Ingo Mauser
 */
public class XsdLoadProfilesHelperTool {

    /**
     * SparseLoadProfiles for all Phases
     */
    public static SparseLoadProfile[][] getSparseLoadProfilesArray(
            XsdLoadProfiles profiles) {
        SparseLoadProfile[][] convertedProfiles = new SparseLoadProfile[profiles.getLoadProfile().size()][];
        for (int i = 0; i < profiles.getLoadProfile().size(); i++) {
            int noPhases = profiles.getLoadProfile().get(i).getPhases().getPhase().size();
            convertedProfiles[i] = new SparseLoadProfile[noPhases];
            for (int j = 0; j < noPhases; j++) {
                convertedProfiles[i][j] = getSparseLoadProfileForPhase(
                        profiles.getLoadProfile().get(i).getPhases().getPhase().get(j));
            }
        }
        return convertedProfiles;
    }

    /**
     * SparseLoadProfile for Phase
     */
    public static SparseLoadProfile getSparseLoadProfileForPhase(
            XsdPhase phase) {
        SparseLoadProfile returnProfile = new SparseLoadProfile();
        List<XsdTick> phaseTicksList = phase.getTick();
        for (int i = 0; i < phaseTicksList.size(); i++) {
            XsdTick loadProfileTick = phaseTicksList.get(i);
            List<XsdLoad> ctLoadList = loadProfileTick.getLoad();
            for (XsdLoad ctLoad : ctLoadList) {
                String commodityString = ctLoad.getCommodity();
                Commodity commodity = Commodity.fromString(commodityString);
                int power = ctLoad.getValue();
                returnProfile.setLoad(commodity, i, power);
            }
        }
        return returnProfile;
    }

    /**
     * Get maximum duration of all configurations (without pauses)
     */
    public static int getMaximumDurationOfAllConfigurations(XsdApplianceProgramConfigurations configurations) {
        int returnValue = 0;

        List<XsdApplianceProgramConfiguration> list = configurations.getApplianceProgramConfiguration();
        for (XsdApplianceProgramConfiguration config : list) {
            int current = getMaximumLengthOfOneConfiguration(config);
            returnValue = Math.max(returnValue, current);
        }

        return returnValue;
    }

    /**
     * Maximum length of the minimum lengths of all alternative profiles
     */
    public static int getMaximumLengthOfOneConfiguration(XsdApplianceProgramConfiguration config) {
        XsdLoadProfiles profiles = config.getLoadProfiles();
        int maxTicks = 0;
        List<XsdLoadProfile> profileList = profiles.getLoadProfile();
        for (XsdLoadProfile profile : profileList) {
            int totalTicks = 0;
            List<XsdPhase> phaseList = profile.getPhases().getPhase();
            if (phaseList != null) {
                for (XsdPhase phase : phaseList) {
                    totalTicks += phase.getMinLength();
                }
                maxTicks = Math.max(maxTicks, totalTicks);
            }
        }

        return maxTicks;
    }

}
