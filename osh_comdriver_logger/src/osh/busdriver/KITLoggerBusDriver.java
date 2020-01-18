package osh.busdriver;

import osh.comdriver.logger.KITValueDatabaseLogger;
import osh.comdriver.logger.ValueDatabaseLogger;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exchange.IHALExchange;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser
 */
public class KITLoggerBusDriver extends LoggerBusDriver {

    protected ValueDatabaseLogger legacyRawLog;
    protected ValueDatabaseLogger legacySmartHomeLog1;
    private long lastPriceSignalLoggedAt;

    public KITLoggerBusDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        // legacy logger
//		if ( valueLoggerConfiguration.getIsValueLoggingToDatabaseActive() ) {
//			legacyRawLog = new MeregioMobilLegacyDatabaseLogger(getGlobalLogger());
//			legacySmartHomeLog1 = new SmartHomeLog1LegacyDatabaseLogger(getGlobalLogger());
//			initSmartHome1Logging();
//		}

        if (this.valueLoggerConfiguration.getIsValueLoggingToDatabaseActive()) {
            this.databaseLog = new KITValueDatabaseLogger(this.getGlobalLogger());
        }

    }

    /**
     * Register to Timer for timed logging operations (logger gets data to log by itself)<br>
     * Register to DriverRegistry for logging operations trigger by Drivers
     */
    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getTimer().registerComponent(this, 1);
    }

    /**
     * Pull-logging
     */
    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();
    }


    private boolean isWagoMeter(UUID id) {
        return (id.getMostSignificantBits() >> 32) == 0x75086001 /* Wago Meter */;
    }


    private int getController(UUID id) {
        int controller = -1;
        if (id.getLeastSignificantBits() == 0x23c3c0a80134L) {
            controller = 2;
        } else if (id.getLeastSignificantBits() == 0x23c3c0a80132L) {
            controller = 3;
        } else if (id.getLeastSignificantBits() == 0x23c3c0a80133L) {
            controller = 1;
        }
        return controller;
    }

    private int getPort(UUID id) {
        return (int) (id.getMostSignificantBits() & 0xFFFF);
    }

    private int getMeter(UUID id) {
        return (int) ((id.getMostSignificantBits() >> 16) & 0xFFFF);
    }


    /**
     * Get things to log from O/C-layer
     *
     * @param exchangeObject
     */
    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        //NOTHING
    }

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }
}
