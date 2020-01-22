package osh.busdriver;

import osh.comdriver.logger.*;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.logger.IAnnotatedForLogging;
import osh.datatypes.logger.LogThis;
import osh.datatypes.registry.AbstractExchange;
import osh.eal.hal.HALBusDriver;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public abstract class LoggerBusDriver extends HALBusDriver implements IDataRegistryListener {

    protected static final boolean logAll = true;
    protected final ValueLoggerConfiguration valueLoggerConfiguration;
    // Config
    final boolean valueLoggingToConsoleActive = true;
    final int valueLoggingToConsoleResolution = 60;
    final int valueLoggingToFileResolution = 60;
    final boolean valueLoggingToDatabaseActive = true;
//	int valueLoggingToDatabaseResolution = 10;
    final int valueLoggingToDatabaseResolution = 60;
    final boolean valueLoggingToRrdDatabaseActive = true;
    final int valueLoggingToRrdDatabaseResolution = 5;
    //
    protected ValueConsoleLogger consoleLog;
    protected ValueFileLogger fileLog;
    protected ValueDatabaseLogger databaseLog;
    protected HashMap<UUID, List<String>> loggerUuidAndClassesToLogMap;
    protected long lastLoggingToConsoleAt;
    protected long lastLoggingToFileAt;
    protected long lastLoggingToDatabaseAt;
    protected long lastLoggingToRrdDatabaseAt;
    boolean valueLoggingToFileActive;


    /**
     * CONSTRUCTOR
     */
    public LoggerBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        this.valueLoggerConfiguration = new ValueLoggerConfiguration(
                this.valueLoggingToConsoleActive,
                this.valueLoggingToConsoleResolution,
                this.valueLoggingToFileActive,
                this.valueLoggingToFileResolution,
                this.valueLoggingToDatabaseActive,
                this.valueLoggingToDatabaseResolution,
                this.valueLoggingToRrdDatabaseActive,
                this.valueLoggingToRrdDatabaseResolution);


        if (this.valueLoggerConfiguration.getIsValueLoggingToFileActive()) {
            this.fileLog = new ValueFileLogger(
                    this.getOSH().getOSHStatus().getRunID(),
                    "0",
                    this.getOSH().getOSHStatus().getConfigurationID(),
                    this.getOSH().getOSHStatus().isSimulation());
        }

        //TODO: add option for "log all"
        String loggerUuidAndClassesToLogMapString = this.getDriverConfig().getParameter("loggeruuidandclassestolog");
        if (loggerUuidAndClassesToLogMapString != null
                && !loggerUuidAndClassesToLogMapString.isEmpty()) {
            String[] splitLoggerUuidAndClassesToLogMapString = loggerUuidAndClassesToLogMapString.split(";");
            if (splitLoggerUuidAndClassesToLogMapString.length > 0) {

                this.loggerUuidAndClassesToLogMap = new HashMap<>();

                for (String s : splitLoggerUuidAndClassesToLogMapString) {
                    UUID uuid = UUID.fromString(s.split(":")[0]);
                    String classes = s.split(":")[1];
                    String[] splitClasses = classes.split(",");
                    List<String> listedClasses = new ArrayList<>(Arrays.asList(splitClasses));
                    this.loggerUuidAndClassesToLogMap.put(uuid, listedClasses);
                }
            }
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getDriverRegistry().subscribe(LogThis.class, this.getUUID(),this);
    }

    /**
     * Pull-logging
     */
    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        ArrayList<ValueLogger> activeLoggers = new ArrayList<>();

        long currentTime = this.getTimer().getUnixTime();

        if (this.consoleLog != null
                && (currentTime - this.lastLoggingToConsoleAt) >= this.valueLoggerConfiguration.getValueLoggingToConsoleResolution()) {
            activeLoggers.add(this.consoleLog);
            this.lastLoggingToConsoleAt = currentTime;
        }

        if (this.fileLog != null
                && (currentTime - this.lastLoggingToFileAt) >= this.valueLoggerConfiguration.getValueLoggingToFileResolution()) {
            activeLoggers.add(this.fileLog);
            this.lastLoggingToFileAt = currentTime;
        }

        if (this.databaseLog != null
                && (currentTime - this.lastLoggingToDatabaseAt) >= this.valueLoggerConfiguration.getValueLoggingToDatabaseResolution()) {
            activeLoggers.add(this.databaseLog);
            this.lastLoggingToDatabaseAt = currentTime;
            this.getGlobalLogger().logDebug("last logging at: " + this.lastLoggingToDatabaseAt);
        }

        //TODO: log all (allow partial logging)
        if (!activeLoggers.isEmpty()) {
            if (logAll) {
                for (Class<? extends AbstractExchange> type : this.getDriverRegistry().getDataTypes()) {
                    for (Entry<UUID, ? extends AbstractExchange> ent : this.getDriverRegistry().getData(type).entrySet()) {
                        for (ValueLogger vLog : activeLoggers) {
                            vLog.log(currentTime, ent.getValue());
                        }
                    }
                }
            } else {
                for (Entry<UUID, List<String>> e : this.loggerUuidAndClassesToLogMap.entrySet()) {
                    UUID uuid = e.getKey();
                    for (String className : e.getValue()) {
                        try {
                            @SuppressWarnings("rawtypes")
                            Class realClass = Class.forName(className);

                            @SuppressWarnings("unchecked")
                            AbstractExchange a = this.getDriverRegistry().getData(realClass, uuid);

                            for (ValueLogger vLog : activeLoggers) {
                                vLog.log(currentTime, a);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                } /* for */
            } /* if( logAll )  */
        } /* if (updateNecessary) */
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof LogThis) {

            LogThis logThis = (LogThis) exchange;

            // get content to log
            IAnnotatedForLogging toLog = logThis.getToLog();

            ArrayList<ValueLogger> activeLoggers = new ArrayList<>();

            long currentTime = this.getTimer().getUnixTime();

            if (this.consoleLog != null) {
                activeLoggers.add(this.consoleLog);
            }

            if (this.fileLog != null
                    && (currentTime - this.lastLoggingToFileAt) >= this.valueLoggerConfiguration.getValueLoggingToFileResolution()) {
                activeLoggers.add(this.fileLog);
            }

            if (this.databaseLog != null) {
                activeLoggers.add(this.databaseLog);
            }

            for (ValueLogger vLog : activeLoggers) {
                vLog.log(currentTime, toLog);
            }
        }
    }
}
