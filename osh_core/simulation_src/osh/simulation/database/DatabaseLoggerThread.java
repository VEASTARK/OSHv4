package osh.simulation.database;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.utils.sql.SQLConnectionProvider;

import java.sql.Connection;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * generic database logger
 * <p>
 * - Logger is split into different classes:
 * <p>
 * - this class handles the general management of all logging events
 * - DatabaseTableDefinitions contains all table definitions that are used to log objects
 * - DatabaseLogMethods contains the log methods where all objects to log will be written to the database
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

    private DatabaseLoggerThread() {
        this.setName("DatabaseLoggerThread");
    }

    public static void initLogger(String tableName, String runName, long startTime,
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

    public static void enqueue(LoggingObjectStateExchange object) {
        synchronized (logQueue) {
            logQueue.add(object);
            logQueue.notify();
        }
    }

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
