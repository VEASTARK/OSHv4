package osh.comdriver.db;

import osh.comdriver.DBPriceSignalProviderComDriver;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneId;
import java.time.ZonedDateTime;


/**
 * @author ???
 */
public class PriceSignalThread extends Thread {

    private final AncillaryCommodity commodity;

    private final IGlobalLogger globalLogger;
    private final DBPriceSignalProviderComDriver priceSignalProvider;

    private MySqlConnectionHandler database;

    public PriceSignalThread(IGlobalLogger globalLogger, DBPriceSignalProviderComDriver priceSignalProvider) {
        super();

        this.commodity = AncillaryCommodity.ACTIVEPOWEREXTERNAL;

        this.globalLogger = globalLogger;
        this.priceSignalProvider = priceSignalProvider;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                PriceSignal price = this.getPriceSignalData();
                PowerLimitSignal limit = this.getPowerLimitSignalData();
                if (price != null)
                    this.priceSignalProvider.processPriceSignal(price, limit);
                try {
                    sleep(120000); // 120 sec.
                } catch (InterruptedException e) {
                    this.globalLogger.logError(e);
                }
            }
        } catch (Exception e) {
            this.globalLogger.logError(e);
        }
    }

    public void setUpSQLConnection(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword) throws ClassNotFoundException {
        this.database = new MySqlConnectionHandler(dbHost, dbPort, dbName, dbUser, dbPassword);

        this.globalLogger.logDebug("* establishing SQL connection for priceSignal driver...");
        try {
            this.database.connect();
            this.globalLogger.logDebug("* ...SQL connection for priceSignal driver OK");
        } catch (SQLException e) {
            this.globalLogger.logError("* ...SQL connection for priceSignal driver FAILED", e);
        }
    }

    private long getStartOfHourInSeconds() {
        return ZonedDateTime.now(ZoneId.systemDefault()).withNano(0).withSecond(0).withMinute(0).toEpochSecond();
    }

    private PriceSignal getPriceSignalData() {
        Statement statement;
        String query;
        ResultSet resultSet;
        PriceSignal pricesignal = new PriceSignal();

        long startTimeForSPS = this.getStartOfHourInSeconds();
        long endTimeForSPS = startTimeForSPS + 36 * 3600; //get the sps for the next 36h!

        try {
            Connection connection = this.database.getConnection();
            statement = connection.createStatement();
        } catch (SQLException e) {
            this.globalLogger.logError("SQL connection error", e);
            this.database.closeConnection();
            return null;
        }

        try {
            query = "SELECT * FROM sps WHERE timestamp >= " + startTimeForSPS + " AND timestamp < " + endTimeForSPS;
            statement.execute(query);

            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                int timeStamp = resultSet.getInt("timestamp");
                double price = resultSet.getInt("price");
                pricesignal.setPrice(timeStamp, price);
            }

            pricesignal.setKnownPriceInterval(startTimeForSPS, endTimeForSPS);

            statement.close();
        } catch (SQLException ex) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
            this.globalLogger.logError("getting DofData failed!", ex);
            this.database.closeConnection();
        }

        return pricesignal;
    }

    private PowerLimitSignal getPowerLimitSignalData() {
        Statement statement;
        String query;
        ResultSet resultSet;
        PowerLimitSignal powerLimit = new PowerLimitSignal();

        long startTimeForEPS = this.getStartOfHourInSeconds();
        long endTimeForEPS = startTimeForEPS + 36 * 3600; //get the eps for the next 24h!

        Connection connection;

        try {
            connection = this.database.getConnection();
            statement = connection.createStatement();
        } catch (SQLException e) {
            this.globalLogger.logError("SQL connection error", e);
            this.database.closeConnection();
            return null;
        }

        try {
            // last load signal

            query = "SELECT * FROM lbs WHERE timestamp <= " + startTimeForEPS + " ORDER BY timestamp DESC LIMIT 1";
            statement.execute(query);
            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                long timeStamp = resultSet.getInt("timestamp");
                double limit = resultSet.getInt("price");

                powerLimit.setPowerLimit(timeStamp, limit);
            }

            statement.close();
            statement = connection.createStatement();

            // future load signals

            query = "SELECT * FROM lbs WHERE timestamp > " + startTimeForEPS + " AND timestamp < " + endTimeForEPS;
            statement.execute(query);

            resultSet = statement.getResultSet();

            while (resultSet.next()) {
                int timeStamp = resultSet.getInt("timestamp");
                double limit = resultSet.getInt("price");

                powerLimit.setPowerLimit(timeStamp, limit);
            }

            powerLimit.setKnownPowerLimitInterval(startTimeForEPS, endTimeForEPS);

            statement.close();
        } catch (SQLException ex) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
            this.globalLogger.logError("getting PowerLimitData failed!", ex);
            this.database.closeConnection();
        }

        return powerLimit;
    }
}
