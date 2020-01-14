package osh.toolbox.appliance;

/**
 * @author mauser
 */
public class ToolApplianceConfiguration {

    public final int configurationID;

    public final ToolApplianceConfigurationProgram program;

    public final ToolApplianceConfigurationExtra[] extras;

    public final ToolApplianceConfigurationProfile[] profiles;


    /**
     * @param configurationID
     * @param program
     * @param extras
     */
    public ToolApplianceConfiguration(
            int configurationID,
            ToolApplianceConfigurationProgram program,
            ToolApplianceConfigurationExtra[] extras,
            ToolApplianceConfigurationProfile[] profiles) {
        this.configurationID = configurationID;
        this.program = program;
        this.extras = extras;
        this.profiles = profiles;
    }

}
