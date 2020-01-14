package osh.comdriver.dof;

import osh.comdriver.DatabaseDofProviderComDriver;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


/**
 * @author ???
 */
public class DofDBThread extends Thread {

    private static Connection dofSQLConnection;

    private final IGlobalLogger globalLogger;
    private final HashMap<UUID, Integer> appliance1stDof;
    private final HashMap<UUID, Integer> appliance2ndDof;
    private final DatabaseDofProviderComDriver userInteractionProvider;

    private final String dbHost;
    private final String dbPort;
    private final String dbName;
    private final String dbUser;
    private final String dbPassword;

    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param userInteractionProvider
     */
    public DofDBThread(
            IGlobalLogger globalLogger,
            DatabaseDofProviderComDriver userInteractionProvider,
            String dbHost, String dbPort, String dbName,
            String dbUser, String dbPassword) {
        super();

        this.globalLogger = globalLogger;
        this.appliance1stDof = new HashMap<>();
        this.appliance2ndDof = new HashMap<>();
        this.userInteractionProvider = userInteractionProvider;

        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbName = dbName;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    /**
     * @return the dofSQLConnection
     */
    public Connection getDofSQLConnection() {
        return dofSQLConnection;
    }

    /**
     * @throws SQLException
     */
    public void setUpSQLConnection() throws SQLException {

        this.globalLogger
                .logDebug("* establishing SQL connection for DoF driver...");

        // set db adress DB
        String host_url = "jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/"
                + this.dbName;

        DofDBThread.dofSQLConnection = DriverManager.getConnection(host_url, this.dbUser,
                this.dbPassword);

        this.globalLogger.logDebug("* ...SQL connection for DoF driver OK");

    }

    @Override
    public void run() {
        super.run();

        while (true) {

            try {
                this.getDofData();
                this.userInteractionProvider.processDofInformation(
                        this.appliance1stDof,
                        this.appliance2ndDof);
                this.renewApplianceScheduleTable();
            } catch (Exception e) {
                this.globalLogger.logError(
                        "transferring user interaction data failed", e);
            }
            try {
                sleep(1000);
            } catch (InterruptedException ignored) {
            } // ignore
        }
    }

    private void renewApplianceScheduleTable() {
        Statement statement = null;
        String query;
        try {
            // get the connection
            statement = dofSQLConnection.createStatement();
            // set the autocommit to false
            dofSQLConnection.setAutoCommit(false);
            // first delete old data
            query = "DELETE FROM `" + "appliance_schedule" + "`";
            statement.execute(query);

            ArrayList<ExpectedStartTimeExchange> applianceSchedule = this.userInteractionProvider
                    .triggerComManager();

            if (applianceSchedule != null) {

                for (ExpectedStartTimeExchange expectedStartTimeExchange : applianceSchedule) {

                    String devId = expectedStartTimeExchange.getSender()
                            .toString();
                    int startTime = (int) expectedStartTimeExchange
                            .getExpectedStartTime();

                    query = "INSERT INTO `" + "appliance_schedule" + "`"
                            + "(`uuid`, `scheduled_at`, `scheduled_to`) "
                            + "VALUES (" + "'" + devId + "', " + "'"
                            + (System.currentTimeMillis() / 1000L) + "', "
                            + "'" + startTime + "'" + ")";
                    statement.execute(query);
                }
                // commit the query
                dofSQLConnection.commit();

                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

        } catch (Exception ex) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            this.globalLogger.logError("update of the DOF table failed", ex);
            try {
                dofSQLConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            // accept now the auto commit again
            try {
                dofSQLConnection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void getDofData() {
        Statement statement = null;
        String query;
        ResultSet resultSet;

        try {
            statement = dofSQLConnection.createStatement();

            query = "SELECT * FROM appliance_dof";
            statement.execute(query);
            // dofSQLconnection.commit();

            resultSet = statement.getResultSet();

            while (resultSet.next()) {

                UUID appId = UUID.fromString(resultSet.getString("uuid"));
                Integer app1stDof = resultSet.getInt("firstdof");
                Integer app2ndDof = resultSet.getInt("seconddof");

                this.appliance1stDof.put(appId, app1stDof);
                this.appliance2ndDof.put(appId, app2ndDof);
            }
            statement.close();
        } catch (Exception ex) {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                dofSQLConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.globalLogger.logError("getting DofData failed!", ex);
        }
    }

}
