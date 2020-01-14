package osh.comdriver.logger;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;


/**
 * @author Florian Allerding, Ingo Mauser
 */
public class ValueFileLogger extends ValueLogger {

    private String prefixForLogs;
    private String suffixForLogs;

    //logger
    //TODO make generic
    private Logger powerLogger;
    private Logger powerDetailsLogger;
    private Logger ancillaryCommodityPowerDetailsLogger;
    private Logger scheduleLogger;
    private Logger temperatureLogger;
    private Logger externalSignalLogger;
    private Logger costDetailedDetailsLogger;


    /**
     * CONSTRUCTOR
     *
     * @param runID
     * @param packageID
     * @param configurationID
     * @param isSimulation
     */
    public ValueFileLogger(String runID, String packageID, String configurationID, boolean isSimulation) {
        super();

        this.initSimulationLogger(runID, packageID, configurationID);
    }


    private void initSimulationLogger(
            String runID,
            String packageID,
            String configurationID) {

        this.prefixForLogs = "logs/" + runID + "/valueLogs/" + packageID + "_" + configurationID;
        this.suffixForLogs = runID + ".csv";

        this.powerLogger = this.createFileLogger("Power");
        this.powerDetailsLogger = this.createFileLogger("PowerDetails");
        this.ancillaryCommodityPowerDetailsLogger = this.createFileLogger("AncillaryCommodityPowerDetails");
        this.scheduleLogger = this.createFileLogger("Schedule");
        this.temperatureLogger = this.createFileLogger("InhouseTemperatures");
        this.externalSignalLogger = this.createFileLogger("ExternalSignals");
        this.costDetailedDetailsLogger = this.createFileLogger("DetailedCosts");
    }


    private Logger createFileLogger(String name) {
        Logger simulationDataLogger = Logger.getLogger(name);
        FileAppender newFileAppender;
        try {
            newFileAppender = new FileAppender(
                    new PatternLayout(),
                    this.prefixForLogs + "_" + name + this.suffixForLogs);
            newFileAppender.setName("logfileappender: " + name);
        } catch (IOException e1) {
            throw new RuntimeException("Exception in simulationLogger", e1);
        }
        simulationDataLogger.addAppender(newFileAppender);
        simulationDataLogger.setLevel(Level.INFO);

        return simulationDataLogger;

    }


    //TODO make generic
    public void logPower(String entryLine) {
        this.powerLogger.log(Level.INFO, entryLine);
    }

    public void logPowerDetails(String entryLine) {
        this.powerDetailsLogger.log(Level.INFO, entryLine);
    }

    public void logSchedule(String entryLine) {
        this.scheduleLogger.log(Level.INFO, entryLine);
    }

    public void logTemperature(String entryLine) {
        this.temperatureLogger.log(Level.INFO, entryLine);
    }

    public void logExternalSignals(String entryLine) {
        this.externalSignalLogger.log(Level.INFO, entryLine);
    }

    public void logAncillaryCommodityPowerDetails(String entryLine) {
        this.ancillaryCommodityPowerDetailsLogger.log(Level.INFO, entryLine);
    }

    public void logCostDetailed(String entryLine) {
        this.costDetailedDetailsLogger.log(Level.INFO, entryLine);
    }


    @Override
    public void log(long timestamp, Object entity) {
        // TODO do logging
    }

}
