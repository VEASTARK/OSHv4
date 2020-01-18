package osh.core.logging;

import org.apache.log4j.*;

import java.io.File;
import java.io.IOException;


/**
 * central static logger class for the OSH. will be initialized at the
 * OSH_Manager ctor
 *
 * @author Florian Allerding
 */
public class OSHLoggerCore {

    /**
     * For creating a log-message use this logger like:
     * root_logger.warn("message");
     */

    public static Logger cb_Main_Logger;

    public static void removeAllAppenders() {
        if (cb_Main_Logger != null) {
            cb_Main_Logger.removeAllAppenders();
        }
    }

    /**
     * initialize a root Logger; for logging always use the root_logger !!!
     *
     * @param logFileName
     * @param logLevel
     */
    public static void initRootLogger(String logDirName, String logFileName, String logLevel, boolean useConsoleAppender) {
        initLoggers(logDirName, logFileName, logLevel, false, useConsoleAppender);
    }

    /**
     * initialize a root Logger; for logging always use the root_logger !!!
     *
     * @param logFileName
     * @param logLevel
     * @param createSingleLogfile
     */
    public static void initLoggers(String logDirName, String logFileName, String logLevel, boolean createSingleLogfile, boolean useConsoleAppender) {

        String adjustedLogFileName = logFileName;
        adjustedLogFileName += ".log";

        createLogFile(logDirName, adjustedLogFileName);

        FileAppender fileAppender = null;
        try {
            fileAppender = new FileAppender(new PatternLayout(), logDirName + "/" + adjustedLogFileName);
            fileAppender.setName("logfileappender: " + adjustedLogFileName);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        cb_Main_Logger = Logger.getLogger("Main Logger");

        cb_Main_Logger.addAppender(fileAppender);

        if (useConsoleAppender) {
            ConsoleAppender consoleAppender = new ConsoleAppender(new PatternLayout(), "System.out");
            consoleAppender.setName("console appender");

            cb_Main_Logger.addAppender(consoleAppender);
        }

        try {
            cb_Main_Logger.setLevel(Level.toLevel(logLevel));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static String getLogLevel() {
        return cb_Main_Logger.getLevel().toString();
    }

    /**
     * sets a specific loglevel like "warn", "info", ...
     *
     * @param logLevel
     */
    public static void setLogLevel(String logLevel) {
        cb_Main_Logger.setLevel(Level.toLevel(logLevel));
    }


    private static void createLogFile(String dirName, String fileName) {
        File logfile;

        File logDir = new File(dirName);
        logDir.mkdirs();
        if (!logDir.exists() || !logDir.isDirectory())
            throw new RuntimeException("log file directory or parent is a file");

        logfile = new File(dirName + "/" + fileName);

        try {
            if (!logfile.exists()) {
                logfile.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean deleteLogDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteLogDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }


}
