package osh.utils.sql;

import com.jcraft.jsch.JSchException;
import osh.utils.string.ParameterConstants.Database;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * @author Sebastian Kramer
 */
public class SingleSQLConnectionProvider {

    //test statement for mysql that does not depend on tables
    protected static final String testStatement = "SELECT 1";
    protected final String serverName;
    protected final int serverPort;
    protected final String serverScheme;

    protected final String databaseUser;
    protected final String databasePW;

    protected final String trustStorePath;
    protected final String trustStorePW;
    protected Connection conn;
    protected boolean useSSL;

    public SingleSQLConnectionProvider(Map<String, String> params) {
        super();
        this.serverName = params.get(Database.serverName);
        this.serverPort = Integer.parseInt(params.get(Database.serverPort));
        this.serverScheme = params.get(Database.serverScheme);
        this.databaseUser = params.get(Database.databaseUser);
        this.databasePW = params.get(Database.databasePW);

        if (params.containsKey(Database.useSSL)) {
            this.useSSL = true;
            this.trustStorePath = params.get(Database.truststorePath);
            this.trustStorePW = params.get(Database.truststorePW);
        } else {
            this.useSSL = false;
            this.trustStorePath = null;
            this.trustStorePW = null;
        }
    }

    protected void initConnection() throws ConnectException {
        try {
            String url = "jdbc:mysql://" + this.serverName + ":" + this.serverPort + "/" + this.serverScheme;

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
