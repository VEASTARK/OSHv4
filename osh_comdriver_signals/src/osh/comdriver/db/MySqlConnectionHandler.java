package osh.comdriver.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author ???
 */
public class MySqlConnectionHandler {
    private final String dbHost;
    private final String dbPort;
    private final String dbUser;
    private final String dbPassword;
    private final String dbName;
    private Connection connection;

    public MySqlConnectionHandler(String dbHost, String dbPort, String dbName, String dbUser,
                                  String dbPassword) throws ClassNotFoundException {
        super();
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.dbName = dbName;

        Class.forName("com.mysql.jdbc.Driver");
    }

    public void connect() throws SQLException {
        String host_url = "jdbc:mysql://" + this.dbHost + ":" + this.dbPort + "/" + this.dbName;

        this.connection = DriverManager.getConnection(host_url, this.dbUser, this.dbPassword);
    }

    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
                // ignore; we tried our best
            }
            this.connection = null;
        }
    }

    public Connection getConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed())
            this.connect();
        return this.connection;
    }
}
