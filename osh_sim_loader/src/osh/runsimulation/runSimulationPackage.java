package osh.runsimulation;

import osh.OSHLifeCycleManager;
import osh.core.LifeCycleStates;
import osh.core.exceptions.LifeCycleManagerException;
import osh.core.exceptions.OSHException;
import osh.core.logging.OSHLoggerCore;
import osh.datatypes.logger.SystemLoggerConfiguration;
import osh.datatypes.logging.general.SimulationResultsLogObject;
import osh.simulation.OSHSimulationResults;
import osh.simulation.database.DatabaseLoggerThread;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Ingo Mauser, Florian Allerding, Sebastian Kramer
 */
public class runSimulationPackage {

    /* ########################
     * # System configuration #
     * ########################	*/

    //for database logging (mysql)
    static protected boolean logToDatabase;
    //	static protected boolean logToDatabase = true;
    static protected final String tableName = "TABLE";
    /* in which databases to log, multiple entries will result in logging to multiple databases
     * { } : none
     *  1  : some server
     *  2  : some other server
     */
    static protected final String[] databasesToLog = {};

    static protected String configFilesDir;
    static protected String logDirName;
    static protected final String configFilesPath = "configfiles";
    static protected final int day = 1; // 1 = 1.
    static protected final int month = 1; // 7 = July
    static protected final int year = 1970;
    static protected final int simulationDuration = 31 * 86400; //simulate 31 days
    //	static private String configID = "oshsimconfig";
    static protected final String[] configIDs = {
            "example",
    };


    /* #########################
     * # General configuration #
     * ######################### */
    static final Long[][] randomSeeds = { //[0]= scenario, [1]= EA
            {0xd1ce5bL, 0xd1ce5bL},
            //		{0xd1ce5cL, 0xd1ce5cL},
            //		{0xd1ce5dL, 0xd1ce5dL},
            //		{0xd1ce5eL, 0xd1ce5eL},
            //		{0xd1ce5fL, 0xd1ce5fL},
            //
            //		{0xd1ce60L, 0xd1ce60L},
            //		{0xd1ce61L, 0xd1ce61L},
            //		{0xd1ce62L, 0xd1ce62L},
            //		{0xd1ce63L, 0xd1ce63L},
            //		{0xd1ce64L, 0xd1ce64L},
    };

    // logger for exceptions etc.
    /**
     * Logger log level
     * "DEBUG"  : nearly everything
     * "INFO"   : only important stuff (default)
     * "ERROR"  : errors only -> used for simulation recording
     * "OFF"    : nothing
     */
    static private final String globalLoggerLogLevel = "DEBUG";


    /* ########
     * # MAIN #
     * ########	*/
    public static void main(String[] args) {

        // reset starting time
        // 1.1.1970
        ZonedDateTime forcedStartTime = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.of("UTC"));

        // iterate all configs
        for (String configID : configIDs) {
            // iterate all random seeds
            for (Long[] seeds : randomSeeds) {
                long simStartTime = System.currentTimeMillis();
                String runID = "" + (simStartTime / 1000);

                Long randomSeed = seeds[0];
                Long optimizationMainRandomSeed = seeds[1];

                OSHSimulationResults simResults = null;

                if (logDirName == null) {
                    logDirName = "logs/" + configID + "/" + runID;
                }

                SystemLoggerConfiguration systemLoggingConfiguration = new SystemLoggerConfiguration(
                        globalLoggerLogLevel,
                        true, //systemLoggingToConsoleActive
                        true, //systemLoggingToFileActive
                        false,
                        true,
                        true,
                        logDirName);

                File simulationFolder;

                if (configFilesDir == null)
                    simulationFolder = new File(configFilesPath + "/osh");
                else
                    simulationFolder = new File(configFilesDir);

                if (!simulationFolder.exists()) {
                    System.out.println("[ERROR] Simulation folder does not exist: " + simulationFolder.getAbsolutePath());
                    System.exit(1);
                }

                System.out.println("[INFO] Simulation running from time " + forcedStartTime + " for " + simulationDuration + " ticks");

                String configRootPath = configFilesDir == null ? configFilesPath + "/osh/" + configID + "/" : configFilesDir + "/";

                String currentScreenplayFileName = configRootPath + "simulation/Screenplay.xml";
                String currentEalConfigFileName = configRootPath + "system/EALConfig.xml";
                String currentOCConfigFileName = configRootPath + "system/OCConfig.xml";
                String currentOSHConfigFileName = configRootPath + "system/OSHConfig.xml";
                String currentCALConfigFileName = configRootPath + "system/CALConfig.xml";

                File file1 = new File(currentScreenplayFileName);
                File file2 = new File(currentEalConfigFileName);
                File file3 = new File(currentOCConfigFileName);
                File file4 = new File(currentOSHConfigFileName);
                File file5 = new File(currentCALConfigFileName);

                // check if files exist
                if (!file1.exists() || !file2.exists() || !file3.exists()) {
                    System.out.println("[ERROR] One ore more of the required files is missing");
                    if (!file1.exists()) {
                        System.out.println("[ERROR] Screenplay file is missing : " + currentScreenplayFileName);
                    }
                    if (!file2.exists()) {
                        System.out.println("[ERROR] EALConfigFile is missing : " + currentEalConfigFileName);
                    }
                    if (!file3.exists()) {
                        System.out.println("[ERROR] OCConfigFile is missing : " + currentOCConfigFileName);
                    }
                    if (!file4.exists()) {
                        System.out.println("[ERROR] OSHConfigFile is missing : " + currentOSHConfigFileName);
                    }
                    if (!file5.exists()) {
                        System.out.println("[ERROR] CALConfigFile is missing : " + currentCALConfigFileName);
                    }
                    return;
                }

                OSHLifeCycleManager lifeCycleManager = new OSHLifeCycleManager(systemLoggingConfiguration);

                try {
                    lifeCycleManager.initOSHFirstStep(
                            currentOSHConfigFileName,
                            currentOCConfigFileName,
                            currentEalConfigFileName,
                            currentCALConfigFileName,
                            forcedStartTime,
                            randomSeed,
                            optimizationMainRandomSeed,
                            runID,
                            configID,
                            logDirName);
                } catch (LifeCycleManagerException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                //init database logger
                try {
                    lifeCycleManager.initDatabaseLogging(logToDatabase, tableName, forcedStartTime, databasesToLog);
                } catch (LifeCycleManagerException e1) {
                    e1.printStackTrace();
                    System.exit(1);
                }

                long simFinishTime = 0L;

                try {
                    lifeCycleManager.loadScreenplay(currentScreenplayFileName);
                    simResults = lifeCycleManager.startSimulation(simulationDuration);

                    simFinishTime = System.currentTimeMillis();
                    if (logToDatabase) {
                        DatabaseLoggerThread.enqueue(new SimulationResultsLogObject(null, null,
                                simResults, 0, simulationDuration - 1, (simFinishTime - simStartTime) / 1000));
                    }
                    lifeCycleManager.switchToLifeCycleState(LifeCycleStates.ON_SYSTEM_SHUTDOWN);
                } catch (LifeCycleManagerException | OSHException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                OSHLoggerCore.removeAllAppenders();

                System.out.println("[main] Simulation runtime: " + (simFinishTime - simStartTime) / 1000 + " sec");

                try {
                    String outputFileName = logDirName + "/" + configID + "_"
                            + randomSeed + "_simresults" + ".csv";
                    if (simResults != null) {
                        simResults.logCurrentStateToFile(new File(outputFileName), (simFinishTime - simStartTime) / 1000);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                logDirName = null;

            } //RANDOM SEEDS
        } //CONFIGS
    }
}
