package osh.core.logging;

import org.apache.log4j.Level;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHStatus;
import osh.datatypes.logger.SystemLoggerConfiguration;

/**
 * Global logger for the OSH. Please use this logger for all OSH logging issues
 *
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class OSHGlobalLogger implements IGlobalLogger {

    private IOSH osh;

    //	// if true please check for required changes
    private boolean createSingleLogfile;

    private boolean consoleSystemMessagesEnabled;
    private boolean logSystemMessagesEnabled;

    private boolean systemLoggingActive;
    private boolean messageCallerTrace;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public OSHGlobalLogger(IOSH osh, SystemLoggerConfiguration systemLoggerConfiguration) {

        if (osh == null) throw new NullPointerException("osh is null");
        this.osh = osh;

        this.createSingleLogfile = systemLoggerConfiguration.isCreateSingleLogfile();

        this.consoleSystemMessagesEnabled = systemLoggerConfiguration.isSystemLoggingToConsoleActive();
        this.logSystemMessagesEnabled = systemLoggerConfiguration.isSystemLoggingToFileActive();

        this.systemLoggingActive = systemLoggerConfiguration.isSystemLoggingActive();
        this.messageCallerTrace = systemLoggerConfiguration.isShowMessageCallerTrace();

        this.initGlobalLogger(systemLoggerConfiguration.getLogDirName(), systemLoggerConfiguration.getGlobalLoggerLogLevel());
    }

    public OSHGlobalLogger(IOSH osh, SystemLoggerConfiguration systemLoggerConfiguration, boolean dontInitLogger) {

        if (osh == null) throw new NullPointerException("osh is null");
        this.osh = osh;

        this.createSingleLogfile = systemLoggerConfiguration.isCreateSingleLogfile();

        this.consoleSystemMessagesEnabled = systemLoggerConfiguration.isSystemLoggingToConsoleActive();
        this.logSystemMessagesEnabled = systemLoggerConfiguration.isSystemLoggingToFileActive();

        this.systemLoggingActive = systemLoggerConfiguration.isSystemLoggingActive();
        this.messageCallerTrace = systemLoggerConfiguration.isShowMessageCallerTrace();

        if (!dontInitLogger)
            this.initGlobalLogger(systemLoggerConfiguration.getLogDirName(), systemLoggerConfiguration.getGlobalLoggerLogLevel());
    }

    private void initGlobalLogger(String logDirName, String logLevel) {
        if (!this.createSingleLogfile) {
            String timeStamp = String.valueOf(System.currentTimeMillis() / 1000L);
            OSHLoggerCore.initLoggers(logDirName, "controllerBoxLog_" + timeStamp, logLevel, true, this.consoleSystemMessagesEnabled);
        } else {

            OSHLoggerCore.initLoggers(logDirName, "controllerBoxLog", logLevel, false, this.consoleSystemMessagesEnabled);
        }
    }


    @Override
    public boolean isSystemLoggingActive() {
        return this.systemLoggingActive;
    }

    @Override
    public void setSystemLoggingActive(boolean systemLoggingActive) {
        this.systemLoggingActive = systemLoggingActive;
    }


    private long currentTime() {
        if (this.osh.getTimer() == null) return -1; //timer is not initialized yet
        return this.osh.getTimer().getUnixTime();
    }

    /**
     * create a single file for logging and override it every restart
     * disable this variable and a logfile with timestamp will be created every restart
     *
     * @return the createSingleLogfile
     */
    @Override
    public boolean isCreateSingleLogfile() {
        return this.createSingleLogfile;
    }

    /**
     * create a single file for logging and override it every restart
     * disable this variable and a logfile with timestamp will be created every restart
     *
     * @param createSingleLogfile the createSingleLogfile to set
     */
    @Override
    public void setCreateSingleLogfile(boolean createSingleLogfile) {
        this.createSingleLogfile = createSingleLogfile;
    }

    /**
     * log system messages in the logfile
     *
     * @return the logSystemMessages
     */
    @Override
    public boolean isLogSystemMessages() {
        return this.logSystemMessagesEnabled;
    }

    /**
     * report the caller class of the log-message while logging
     *
     * @return the messageCallerTrace
     */
    @Override
    public boolean isMessageCallerTrace() {
        return this.messageCallerTrace;
    }

    /**
     * report the caller class of the log-message while logging
     *
     * @param messageCallerTrace the messageCallerTrace to set
     */
    @Override
    public void setMessageCallerTrace(boolean messageCallerTrace) {
        this.messageCallerTrace = messageCallerTrace;
    }

    /**
     * print system messages on the console
     *
     * @return the systemMessagesEnable
     */
    @Override
    public boolean isSystemMessagesEnable() {
        return this.consoleSystemMessagesEnabled;
    }

    public String getLogLevel() {
        return OSHLoggerCore.getLogLevel();
    }

    public void setLogLevel(String logLevel) {
        OSHLoggerCore.setLogLevel(logLevel);
    }

    /**
     * print system messages on the console
     *
     * @param consoleSystemMessagesEnabled the systemMessagesEnable to set
     */
    @Override
    public void setConsoleSystemMessagesEnabled(boolean consoleSystemMessagesEnabled) {
        this.consoleSystemMessagesEnabled = consoleSystemMessagesEnabled;
    }

    /**
     * log system messages in the logfile
     *
     * @param logSystemMessagesEnabled the logSystemMessages to set
     */
    @Override
    public void setLogSystemMessagesEnabled(boolean logSystemMessagesEnabled) {
        this.logSystemMessagesEnabled = logSystemMessagesEnabled;
    }


    // ALL > TRACE > DEBUG > INFO > WARN > ERROR > FATAL > OFF

    public void logError(Object message) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.error(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [ERROR] [" + callerClassName + "] : [" + message + "]");
            } else {
                OSHLoggerCore.cb_Main_Logger.error(message);
            }
        }
    }

    public void logError(Object message, Throwable throwable) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.error(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [ERROR] [" + callerClassName + "] : [" + message + "]", throwable);
            } else {
                OSHLoggerCore.cb_Main_Logger.error(message, throwable);
            }
        }
    }

    public void logWarning(Object message) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.warn(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [WARN] [" + callerClassName + "] : [" + message + "]");
            } else {
                OSHLoggerCore.cb_Main_Logger.warn(message);
            }
        }
    }

    public void logWarning(Object message, Throwable throwable) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.warn(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [WARN] [" + callerClassName + "] : [" + message + "]", throwable);
            } else {
                OSHLoggerCore.cb_Main_Logger.warn(message, throwable);
            }
        }
    }

    public void logInfo(Object message) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.info(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [INFO] [" + callerClassName + "] : [" + message + "]");
            } else {
                OSHLoggerCore.cb_Main_Logger.info(message);
            }
        }
    }

    public void logInfo(Object message, Throwable throwable) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.info(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [INFO] [" + callerClassName + "] : [" + message + "]", throwable);
            } else {
                OSHLoggerCore.cb_Main_Logger.info(message, throwable);
            }
        }
    }


    public void logDebug(Object message) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.debug(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [DEBUG] [" + callerClassName + "] : [" + message + "]");
            } else {
                OSHLoggerCore.cb_Main_Logger.debug(message);
            }
        }
    }

    public void logDebug(Object message, Throwable throwable) {
        if (this.systemLoggingActive) {
            if (this.messageCallerTrace) {
                String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                OSHLoggerCore.cb_Main_Logger.debug(this.getHhUUIDLogString() + "[LOGGING] [" + this.currentTime() + "] [DEBUG] [" + callerClassName + "] : [" + message + "]", throwable);
            } else {
                OSHLoggerCore.cb_Main_Logger.debug(message, throwable);
            }
        }
    }

    /**
     * System messages can be printed at the console to show the current state of the contollerbox
     *
     * @param message
     */
    @Override
    public void printSystemMessage(Object message) {
        if (this.systemLoggingActive) {
            if (this.consoleSystemMessagesEnabled) {
                if (this.messageCallerTrace) {
                    String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
                    String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
                    System.out.println(this.getHhUUIDLogString() + "[CONSOLE] [" + this.currentTime() + "] [SYSTEM] [" + callerClassName + "] : [" + message + "]");
                } else {
                    System.out.println("[osh] " + message);
                }
            }
            if (this.logSystemMessagesEnabled) {
                this.logInfo("[osh] : " + message);
            }
        }
    }

    /**
     * System messages which will be printed out but never logged. Messages are only printed out in DEBUG level!
     *
     * @param message
     */
    @Override
    public void printDebugMessage(Object message) {
        // print only in debug level
        if (OSHLoggerCore.cb_Main_Logger.getLevel() == Level.DEBUG) {
            String[] callerClassNameSpace = Thread.currentThread().getStackTrace()[2].getClassName().split("\\.");
            String callerClassName = callerClassNameSpace[callerClassNameSpace.length - 1];
            System.out.println(this.getHhUUIDLogString() + "[CONSOLE] [" + this.currentTime() + "] [DEBUG] [" + callerClassName + "] : [" + message + "]");
        }
    }

    private String getHhUUIDLogString() {
        IOSHStatus status = this.osh.getOSHStatus();
        if (status.getHhUUID() != null) {
            String uuid = status.getHhUUID().toString();
            return "[" + uuid.substring(uuid.length() - 4) + "] ";
        }
        return "";
    }
}
