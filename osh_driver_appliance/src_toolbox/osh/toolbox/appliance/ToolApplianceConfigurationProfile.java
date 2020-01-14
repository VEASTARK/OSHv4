package osh.toolbox.appliance;

/**
 * @author Ingo Mauser
 */
public class ToolApplianceConfigurationProfile {

    public final int profileID;
    public final String profileName;

    public final String[] phaseNames;
    public final String[] phaseInputFiles;

    public final int activePowerColumn;
    public final int reactivePowerColumn;
    public final int naturalGasPowerColumn;
    public final int domesticHotWaterPowerColumn;

    public ToolApplianceConfigurationProfile(
            int profileID,
            String profileName,
            String[] phaseNames,
            String[] phaseInputFiles,
            int activePowerColumn,
            int reactivePowerColumn,
            int domesticHotWaterColumn,
            int naturalGasPowerColumn) {

        this.profileID = profileID;
        this.profileName = profileName;

        this.phaseNames = phaseNames;
        this.phaseInputFiles = phaseInputFiles;

        this.activePowerColumn = activePowerColumn;
        this.reactivePowerColumn = reactivePowerColumn;
        this.domesticHotWaterPowerColumn = domesticHotWaterColumn;
        this.naturalGasPowerColumn = naturalGasPowerColumn;

    }

}
