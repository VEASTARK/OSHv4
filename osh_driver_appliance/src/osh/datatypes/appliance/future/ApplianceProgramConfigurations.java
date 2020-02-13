package osh.datatypes.appliance.future;

import osh.configuration.appliance.XsdApplianceProgramConfiguration;
import osh.configuration.appliance.XsdApplianceProgramConfigurations;
import osh.configuration.appliance.XsdLoadProfile;
import osh.configuration.appliance.XsdPhase;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.appliance.generic.XsdLoadProfilesHelperTool;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Native wrapper for the {@link XsdApplianceProgramConfigurations} class.
 *
 * @author Sebastian Kramer
 */
public class ApplianceProgramConfigurations {

    private final SparseLoadProfile[][][] loadProfiles;
    private final int[][][] minLengths;
    private final int[][][] maxLengths;
    private final int[] ids;

    private final int noOfConfigurations;
    private final int overallMaxLength;
    private final int overallMinLength;

    /**
     * Constructs this wrapper around the given {@link XsdApplianceProgramConfigurations}.
     *
     * @param baseConfigurations the configurations to wrap
     */
    public ApplianceProgramConfigurations(XsdApplianceProgramConfigurations baseConfigurations) {

        int dim0Size = baseConfigurations.getApplianceProgramConfiguration().size();

        this.loadProfiles = new SparseLoadProfile[dim0Size][][];
        this.minLengths = new int[dim0Size][][];
        this.maxLengths = new int[dim0Size][][];
        this.ids = new int[dim0Size];

        for (int dim0 = 0; dim0 < dim0Size; dim0++) {
            XsdApplianceProgramConfiguration configuration = baseConfigurations.getApplianceProgramConfiguration()
                    .get(dim0);

            int dim1Size = configuration.getLoadProfiles().getLoadProfile().size();

            this.loadProfiles[dim0] = new SparseLoadProfile[dim1Size][];
            this.minLengths[dim0] = new int[dim1Size][];
            this.maxLengths[dim0] = new int[dim1Size][];

            this.ids[dim0] = configuration.getConfigurationID();

            for (int dim1 = 0; dim1 < dim1Size; dim1++) {
                XsdLoadProfile loadProfile = configuration.getLoadProfiles().getLoadProfile().get(dim1);

                int dim2Size = loadProfile.getPhases().getPhase().size();

                this.loadProfiles[dim0][dim1] = new SparseLoadProfile[dim2Size];
                this.minLengths[dim0][dim1] = new int[dim2Size];
                this.maxLengths[dim0][dim1] = new int[dim2Size];

                for (int dim2 = 0; dim2 < dim2Size; dim2++) {
                    XsdPhase phase = loadProfile.getPhases().getPhase().get(dim2);

                    this.loadProfiles[dim0][dim1][dim2] = XsdLoadProfilesHelperTool.getSparseLoadProfileForPhase(phase);
                    this.minLengths[dim0][dim1][dim2] = phase.getMinLength();
                    this.maxLengths[dim0][dim1][dim2] = phase.getMaxLength();
                }
            }
        }

        this.noOfConfigurations = dim0Size;
        this.overallMaxLength = IntStream.range(0, dim0Size).map(this::getMaxLengthOfConfiguration).max().orElse(0);
        this.overallMinLength = IntStream.range(0, dim0Size).map(this::getMinLengthOfConfiguration).min().orElse(0);
    }

    /**
     * Returns the maximum of all minimum length of all the configurations matching the given id contained in this
     * wrapper.
     *
     * @param configurationId the id
     * @return the maximin length of all the configurations matching the given id
     */
    public int getMaxLengthOfConfiguration(int configurationId) {
        return Arrays.stream(this.minLengths[configurationId]).mapToInt(a -> Arrays.stream(a).sum()).max().orElse(0);
    }

    /**
     * Returns the minimum of all maximum length of all the configurations matching the given id contained in this
     * wrapper.
     *
     * @param configurationId the id
     * @return the minimax length of all the configurations matching the given id
     */
    public int getMinLengthOfConfiguration(int configurationId) {
        return Arrays.stream(this.maxLengths[configurationId]).mapToInt(a -> Arrays.stream(a).sum()).min().orElse(0);
    }

    /**
     * Returns the count of all configurations.
     *
     * @return the count of all configurations
     */
    public int getNumberOfConfigurations() {
        return this.noOfConfigurations;
    }

    /**
     * Returns the number of profiles contained in the configuration matching the given id.
     *
     * @param configurationId the id
     * @return the number of profiles contained in the configuration matching the id
     */
    public int getNumberOfProfiles(int configurationId) {
        return this.loadProfiles[configurationId].length;
    }

    /**
     * Returns the number of phases contained in the profile and configuration matching the given ids.
     *
     * @param configurationId the id of the configuration
     * @param profileId the id of the profile
     * @return the number of phases contained in the profile and configuration matching the given ids
     */
    public int getNumberOfPhases(int configurationId, int profileId) {
        return this.loadProfiles[configurationId][profileId].length;
    }

    /**
     * Returns the load profiles contained in configuration matching the given id.
     *
     * @param configurationId the id
     *
     * @return the load profiles contained in configuration matching the given id
     */
    public SparseLoadProfile[][] getLoadProfilesOfConfiguration(int configurationId) {
        return this.loadProfiles[configurationId];
    }

    /**
     * Returns the maximum length of all contained configurations.
     *
     * @return the maximum length of all contained configurations
     */
    public int getMaxLengthOfAllConfigurations() {
        return this.overallMaxLength;
    }

    /**
     * Returns the minimum length of all contained configurations.
     *
     * @return the minimum length of all contained configurations
     */
    public int getMinLengthOfAllConfigurations() {
        return this.overallMinLength;
    }

    /**
     * Returns the maximum length of the phase, profile and configuration matching the given ids.
     *
     * @param configurationId the id of the configuration
     * @param profileId the id of the profile
     * @param phaseId the id of the phase
     *
     * @return the maximum length of the phase, profile and configuration matching the ids
     */
    public int getMaxLengthOfPhase(int configurationId, int profileId, int phaseId) {
        return this.maxLengths[configurationId][profileId][phaseId];
    }

    /**
     * Returns the minimum length of the phase, profile and configuration matching the given ids.
     *
     * @param configurationId the id of the configuration
     * @param profileId the id of the profile
     * @param phaseId the id of the phase
     *
     * @return the minimum length of the phase, profile and configuration matching the ids
     */
    public int getMinLengthOfPhase(int configurationId, int profileId, int phaseId) {
        return this.minLengths[configurationId][profileId][phaseId];
    }

    /**
     * Returns the corresponding configuration id to the given index.
     *
     * @param id the index
     *
     * @return the corresponding configuration id to the index
     */
    public int getProgramConfigurationsId(int id) {
        return this.ids[id];
    }
}
