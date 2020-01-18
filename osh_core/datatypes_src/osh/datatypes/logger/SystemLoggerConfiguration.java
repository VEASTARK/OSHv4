package osh.datatypes.logger;

/**
 * @author Ingo Mauser
 */
public class SystemLoggerConfiguration {

    // system logger for system messages etc.

    private final String globalLoggerLogLevel;

    private final boolean systemLoggingToConsoleActive;

    private final boolean systemLoggingToFileActive;
    private final boolean createSingleLogfile;

    private final boolean systemLoggingActive;
    private final boolean showMessageCallerTrace;

    private final String logDirName;

    public SystemLoggerConfiguration(
            String globalLoggerLogLevel,
            boolean systemLoggingToConsoleActive,
            boolean systemLoggingToFileActive,
            boolean createSingleLogfile,
            boolean systemLoggingActive,
            boolean showMessageCallerTrace,
            String logDirName) {
        super();

        this.globalLoggerLogLevel = globalLoggerLogLevel;

        this.systemLoggingToConsoleActive = systemLoggingToConsoleActive;
        this.systemLoggingToFileActive = systemLoggingToFileActive;

        this.createSingleLogfile = createSingleLogfile;

        this.systemLoggingActive = systemLoggingActive;
        this.showMessageCallerTrace = showMessageCallerTrace;

        this.logDirName = logDirName;
    }


    public String getGlobalLoggerLogLevel() {
        return this.globalLoggerLogLevel;
    }

    public boolean isSystemLoggingToConsoleActive() {
        return this.systemLoggingToConsoleActive;
    }

    public boolean isSystemLoggingToFileActive() {
        return this.systemLoggingToFileActive;
    }

    public boolean isCreateSingleLogfile() {
        return this.createSingleLogfile;
    }

    public boolean isShowMessageCallerTrace() {
        return this.showMessageCallerTrace;
    }

    public boolean isSystemLoggingActive() {
        return this.systemLoggingActive;
    }

    public String getLogDirName() {
        return this.logDirName;
    }

}
