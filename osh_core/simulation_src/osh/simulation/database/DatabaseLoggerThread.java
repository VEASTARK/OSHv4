package osh.simulation.database;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.utils.sql.SQLConnectionProvider;

import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Represents a generic database logger.
 * <p>
 * - Logger is split into different classes:
 * <p>
 * - this class handles the general management of all logging events
 * - {@link DatabaseTableDefinitions} contains all table definitions that are used to log objects
 * - {@link DatabaseLogMethods} contains the log methods where all objects to log will be written to the database
 * <p>
 * Logging works on a separate thread as a queued logging system:
 * <p>
 * - After initialising this class will start a new thread so logging wont block the simulation
 * - incoming log objects will be put into a queue
 * - objects from the queue will be popped and handled based on their class by the DatabaseLogMethods
 *
 * @author Sebastian Kramer
 */
public final class DatabaseLoggerThread extends Thread {

    private static boolean running = true;

    private static boolean logToDatabase;
    private static final Queue<LoggingObjectStateExchange> logQueue = new LinkedBlockingQueue<>();
    private static DatabaseLoggerThread loggerThread;

    private static boolean logDevices;
    private static boolean logThermal;
    private static boolean logWaterTank;
    private static boolean logEA;
    private static boolean logSmartHeater;

    public static boolean isLogDevices() {
        return logDevices;
    }

    public static void setLogDevices(final boolean logDevices) {
        DatabaseLoggerThread.logDevices = logDevices;
    }

    public static boolean isLogThermal() {
        return logThermal;
    }

    public static void setLogThermal(final boolean logThermal) {
        DatabaseLoggerThread.logThermal = logThermal;
    }

    public static boolean isLogWaterTank() {
        return logWaterTank;
    }

    public static void setLogWaterTank(final boolean logWaterTank) {
        DatabaseLoggerThread.logWaterTank = logWaterTank;
    }

    public static boolean isLogEA() {
        return logEA;
    }

    public static void setLogEA(final boolean logEA) {
        DatabaseLoggerThread.logEA = logEA;
    }

    public static boolean isLogSmartHeater() {
        return logSmartHeater;
    }

    public static void setLogSmartHeater(final boolean logSmartHeater) {
        DatabaseLoggerThread.logSmartHeater = logSmartHeater;
    }

    private DatabaseLoggerThread() {
        this.setName("DatabaseLoggerThread");
    }

    /**
     * Initiliazes this logger thread with information about the database to log to and starts the thread.
     *
     * @param tableName the root-tablename to log to
     * @param runName the runName of the simulation
     * @param startTime the starttime of the simulation
     * @param preferredConnection the identifiers of the preferred database connections to log to
     */
    public static void initLogger(String tableName, String runName, ZonedDateTime startTime,
                                  String[] preferredConnection) {
        DatabaseLogMethods.runName = runName;
        DatabaseLogMethods.startTime = startTime;
        DatabaseLogMethods.preferredConnection = preferredConnection;

        DatabaseTableDefinitions.initTableNames(tableName);
        DatabaseLogMethods.conn = new Connection[preferredConnection.length];

        logToDatabase = true;

        loggerThread = new DatabaseLoggerThread();
        loggerThread.start();
    }

    /**
     * Signals this thread to shut down after every pending log request has been handled.
     */
    public static void shutDown() {
        running = false;

        if (logToDatabase) {
            while (!logQueue.isEmpty()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (loggerThread != null){
            while (!logQueue.isEmpty()) {
                logQueue.remove();
            }
        }
    }

    /**
     * Commits the given log object to the queue of objects to log.
     *
     * @param logObject the new log object
     */
    public static void enqueue(LoggingObjectStateExchange logObject) {
        synchronized (logQueue) {
            logQueue.add(logObject);
            logQueue.notify();
        }
    }

    /**
     * Returns if the logging to the database is active.
     *
     * @return true if the logging to the database is active
     */
    public static boolean isLogToDatabase() {
        return logToDatabase;
    }

    @Override
    public void run() {
        while (running || !logQueue.isEmpty()) {
            try {
                LoggingObjectStateExchange work;

                synchronized (logQueue) {
                    while (logQueue.isEmpty())
                        logQueue.wait();

                    // Get the next work item off of the queue
                    work = logQueue.remove();
                }
                DatabaseLogMethods.logObject(work);
            } catch (InterruptedException ie) {
                break;  // Terminate
            }
        }
        SQLConnectionProvider.closeConnection();
    }
}
