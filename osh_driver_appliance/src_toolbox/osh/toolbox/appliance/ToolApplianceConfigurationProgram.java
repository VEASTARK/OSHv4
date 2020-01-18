package osh.toolbox.appliance;

/**
 * @author Ingo Mauser
 */
public class ToolApplianceConfigurationProgram {

    public final int programID;
    public final String programName;
    public final String descriptionEN;
    public final String descriptionDE;


    public ToolApplianceConfigurationProgram(
            int programID,
            String programName,
            String descriptionEN,
            String descriptionDE) {

        this.programID = programID;
        this.programName = programName;
        this.descriptionEN = descriptionEN;
        this.descriptionDE = descriptionDE;
    }

}
