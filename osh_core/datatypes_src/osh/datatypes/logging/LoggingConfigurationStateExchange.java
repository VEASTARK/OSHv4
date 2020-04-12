package osh.datatypes.logging;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * represents information about what to log in the conext of the simulation.
 *
 * @author Sebastian Kramer
 */
public class LoggingConfigurationStateExchange extends StateExchange {

    private final boolean logToDatabase;
    private final List<Long[]> loggingIntervals;
    private final boolean logDetailedPower;
    private final boolean logEpsPls;
    private final boolean logH0;
    private final boolean logIntervalls;
    private final boolean logDevices;
    private final boolean logBaseload;
    private final boolean logThermal;
    private final boolean logWaterTank;
    private final boolean logEA;
    private final boolean logSmartHeater;

    /**
     * Constructs this logging information exchnage with the given configuration.
     *
     * @param sender the sender of the exchange
     * @param timestamp the timestamp of the exchange
     * @param logToDatabase flag if ther eis logging to the database
     * @param loggingIntervals intervalls in which logging information should be persisted
     * @param logDetailedPower flag if detailed power consumption should be logged
     * @param logEpsPls flag if the eps-/pls-signal should be logged
     * @param logH0 flag if the aggregated power consumption should be logged
     * @param logIntervalls flag if there should be logging in intervalls
     * @param logDevices flag if detailed information about devices should be logged
     * @param logBaseload flag if the baseload of the simulation should be logged
     * @param logThermal flag if information about the aggregate thermal consumption should be logged
     * @param logWaterTank flag if information about the watertanks should be logged
     * @param logEA flag if information about the EA execution should be logged
     * @param logSmartHeater flag if detailed information about the operation of the smart-heater should be logged
     */
    public LoggingConfigurationStateExchange(UUID sender, ZonedDateTime timestamp, boolean logToDatabase,
                                             List<Long[]> loggingIntervals, boolean logDetailedPower,
                                             boolean logEpsPls, boolean logH0,
                                             boolean logIntervalls, boolean logDevices, boolean logBaseload, boolean logThermal, boolean logWaterTank, boolean logEA,
                                             boolean logSmartHeater) {
        super(sender, timestamp);
        this.logToDatabase = logToDatabase;
        this.loggingIntervals = loggingIntervals;
        this.logDetailedPower = logDetailedPower;
        this.logEpsPls = logEpsPls;
        this.logH0 = logH0;
        this.logIntervalls = logIntervalls;
        this.logDevices = logDevices;
        this.logBaseload = logBaseload;
        this.logThermal = logThermal;
        this.logWaterTank = logWaterTank;
        this.logEA = logEA;
        this.logSmartHeater = logSmartHeater;
    }

    public boolean isLogToDatabase() {
        return this.logToDatabase;
    }

    public List<Long[]> getLoggingIntervals() {
        return this.loggingIntervals;
    }

    public boolean isLogDetailedPower() {
        return this.logDetailedPower;
    }

    public boolean isLogEpsPls() {
        return this.logEpsPls;
    }

    public boolean isLogH0() {
        return this.logH0;
    }

    public boolean isLogIntervalls() {
        return this.logIntervalls;
    }

    public boolean isLogDevices() {
        return this.logDevices;
    }

    public boolean isLogBaseload() {
        return this.logBaseload;
    }

    public boolean isLogThermal() {
        return this.logThermal;
    }

    public boolean isLogWaterTank() {
        return this.logWaterTank;
    }

    public boolean isLogEA() {
        return this.logEA;
    }

    public boolean isLogSmartHeater() {
        return this.logSmartHeater;
    }

    @Override
    public StateExchange clone() {
        return new LoggingConfigurationStateExchange(this.getSender(), this.getTimestamp(), this.logToDatabase, this.loggingIntervals,
                this.logDetailedPower, this.logEpsPls, this.logH0, this.logIntervalls, this.logDevices, this.logBaseload,
                this.logThermal, this.logWaterTank, this.logEA, this.logSmartHeater);
    }
}
