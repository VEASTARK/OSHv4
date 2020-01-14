package constructsimulation.datatypes;

/**
 * @author Ingo Mauser, Till Schuberth
 */
public class CALConfigurationWrapper {

    public final boolean showGui;
    public final String guiComDriverClassName;
    public final String guiComManagerClassName;


    public CALConfigurationWrapper(boolean showGui, String guiComDriverClassName, String guiComManagerClassName) {
        this.showGui = showGui;
        this.guiComDriverClassName = guiComDriverClassName;
        this.guiComManagerClassName = guiComManagerClassName;
    }
}
