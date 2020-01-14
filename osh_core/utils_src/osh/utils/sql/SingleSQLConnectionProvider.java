package osh.utils.sql;

import com.jcraft.jsch.JSchException;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Sebastian Kramer
 */
public class SingleSQLConnectionProvider {

    //test statement for mysql that does not depend on tables
    protected static final String testStatement = "SELECT 1";
    protected final String databaseServerName;
    protected final int databaseServerPort;
    protected final String databaseServerScheme;

    protected final String databaseUser;
    protected final String databasePW;

    protected final String trustStorePath;
    protected final String trustStorePW;
    protected Connection conn;
    protected boolean useSSL;

    public SingleSQLConnectionProvider(String databaseServerName, int databaseServerPort, String databaseServerScheme, String databaseUser,
                                       String databasePW) {
        super();
        this.databaseServerName = databaseServerName;
        this.databaseServerPort = databaseServerPort;
        this.databaseServerScheme = databaseServerScheme;
        this.databaseUser = databaseUser;
        this.databasePW = databasePW;
        this.trustStorePath = null;
        this.trustStorePW = null;
    }

    public SingleSQLConnectionProvider(String databaseServerName, int databaseServerPort, String databaseServerScheme, String databaseUser,
                                       String databasePW, boolean useSSL, String trustStorePath, String trustStorePW) {
        super();
        this.databaseServerName = databaseServerName;
        this.databaseServerPort = databaseServerPort;
        this.databaseServerScheme = databaseServerScheme;
        this.databaseUser = databaseUser;
        this.databasePW = databasePW;
        this.useSSL = useSSL;
        this.trustStorePath = trustStorePath;
        this.trustStorePW = trustStorePW;
    }

    protected void initConnection() throws ConnectException {
        try {
            String url = "jdbc:mysql://" + this.databaseServerName + ":" + this.databaseServerPort + "/" + this.databaseServerScheme;

            if (this.useSSL) {
                url += "?useSSL=true&requireSSL=true";
                System.setProperty("javax.net.ssl.trustStore", this.trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", this.trustStorePW);
//				System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
                System.setProperty("https.protocols", "TLSv1.2");
//				System.setProperty("javax.net.debug","all");
            }
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, this.databaseUser,
                    this.databasePW);


            Statement stmt = conn.createStatement();
            stmt.execute(testStatement);
            stmt.close();

            this.conn = conn;
        } catch (ClassNotFoundException | SQLException e) {
            throw new ConnectException("database not reachable");
        }
    }

    public Connection getConnection() throws ConnectException, JSchException {
        if (this.conn == null)
            this.initConnection();

        return this.conn;
    }

    public void closeConnection() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.conn = null;
    }
}
