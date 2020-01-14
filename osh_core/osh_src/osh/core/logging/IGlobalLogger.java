package osh.core.logging;

public interface IGlobalLogger {

    boolean isSystemLoggingActive();

    void setSystemLoggingActive(boolean systemLoggingActive);

    /**
     * create a single file for logging and override it every restart
     * disable this variable and a logfile with timestamp will be created every restart
     *
     * @return the createSingleLogfile
     */
    boolean isCreateSingleLogfile();

    /**
     * create a single file for logging and override it every restart
     * disable this variable and a logfile with timestamp will be created every restart
     *
     * @param createSingleLogfile the createSingleLogfile to set
     */
    void setCreateSingleLogfile(boolean createSingleLogfile);

    /**
     * log system messages in the logfile
     *
     * @return the logSystemMessages
     */
    boolean isLogSystemMessages();

    /**
     * report the caller class of the log-message while logging
     *
     * @return the messageCallerTrace
     */
    boolean isMessageCallerTrace();

    /**
     * report the caller class of the log-message while logging
     *
     * @param messageCallerTrace the messageCallerTrace to set
     */
    void setMessageCallerTrace(boolean messageCallerTrace);

    /**
     * print system messages on the console
     *
     * @return the systemMessagesEnable
     */
    boolean isSystemMessagesEnable();

    String getLogLevel();

    void setLogLevel(String logLevel);

    /**
     * print system messages on the console
     *
     * @param consoleSystemMessagesEnabled the systemMessagesEnable to set
     */
    void setConsoleSystemMessagesEnabled(
            boolean consoleSystemMessagesEnabled);

    /**
     * log system messages in the logfile
     *
     * @param logSystemMessagesEnabled the logSystemMessages to set
     */
    void setLogSystemMessagesEnabled(
            boolean logSystemMessagesEnabled);

    void logError(Object message);

    void logError(Object message, Throwable throwable);

    void logWarning(Object message);

    void logWarning(Object message, Throwable throwable);

    void logInfo(Object message);

    void logInfo(Object message, Throwable throwable);

    void logDebug(Object message);

    void logDebug(Object message, Throwable throwable);

    /**
     * System messages can be printed at the console to show the current state of the contollerbox
     *
     * @param message
     */
    void printSystemMessage(Object message);

    /**
     * System messages which will be printed out but never logged. Messages are only printed out in DEBUG level!
     *
     * @param message
     */
    void printDebugMessage(Object message);

}