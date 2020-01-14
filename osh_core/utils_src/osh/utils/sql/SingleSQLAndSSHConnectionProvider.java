package osh.utils.sql;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.File;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

/**
 * @author Sebastian Kramer
 */
public class SingleSQLAndSSHConnectionProvider extends SingleSQLConnectionProvider {

    private static final int differentPortTries = 5;                //how many different ports to try
    private final String strSshUser;                                // SSH loging username
    private final String strSshPassword;                            // SSH login password
    private final String strSshHost;                                // hostname or ip or SSH server
    private final int nSshPort;                                    // remote SSH host port number
    private final int initialLocalPort;                             // local port number use to bind SSH tunnel
    private Session sess;
    private boolean useSSH;
    private final boolean useKeyFile;                                //if to connect over SSH with user/pw or keyfile
    private final String keyFilePath;                                        //path to the key file

    public SingleSQLAndSSHConnectionProvider(String databaseServerName, int databaseServerPort,
                                             String databaseServerScheme, String databaseUser,
                                             String databasePW, String strSshUser, String strSshPassword,
                                             String strSshHost, int nSshPort, int initialLocalPort, boolean useKeyFile,
                                             String keyFilePath) {
        super(databaseServerName, databaseServerPort, databaseServerScheme, databaseUser, databasePW);

        if (strSshUser != null || strSshPassword != null)
            this.useSSH = true;

        this.strSshUser = strSshUser;
        this.strSshPassword = strSshPassword;
        this.strSshHost = strSshHost;
        this.nSshPort = nSshPort;
        this.initialLocalPort = initialLocalPort;
        this.useKeyFile = useKeyFile;
        this.keyFilePath = keyFilePath;
    }

    public SingleSQLAndSSHConnectionProvider(String databaseServerName, int databaseServerPort,
                                             String databaseServerScheme, String databaseUser,
                                             String databasePW, String strSshUser, String strSshPassword,
                                             String strSshHost, int nSshPort, int initialLocalPort, boolean useKeyFile,
                                             String keyFilePath, boolean useSSL, String trustStorePath, String trustStorePW) {
        super(databaseServerName, databaseServerPort, databaseServerScheme, databaseUser, databasePW, useSSL, trustStorePath, trustStorePW);

        if (strSshUser != null || strSshPassword != null)
            this.useSSH = true;

        this.strSshUser = strSshUser;
        this.strSshPassword = strSshPassword;
        this.strSshHost = strSshHost;
        this.nSshPort = nSshPort;
        this.initialLocalPort = initialLocalPort;
        this.useKeyFile = useKeyFile;
        this.keyFilePath = keyFilePath;
    }

    private void initSession() throws JSchException {
        Random r = new Random();

        for (int i = 0; i < differentPortTries; i++) {
            try {
                int newPort = this.initialLocalPort + i * (r.nextInt(10) - 5);

                this.sess = this.doSshTunnel(newPort);
                break;
            } catch (JSchException e) {
                if (i == differentPortTries - 1)
                    throw e;
            }
        }
    }

    protected void initSSHTunnelledConnection() throws ConnectException {
        try {
            String url = "jdbc:mysql://localhost:" + this.sess.getPortForwardingL()[0].split(":")[0] + "/" + this.databaseServerScheme;

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
        } catch (ClassNotFoundException | SQLException | JSchException e) {
            throw new ConnectException("database not reachable");
        }
    }

    @Override
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

    @Override
    public Connection getConnection() throws ConnectException, JSchException {
        if (this.sess == null || this.conn == null) {
            try {
                this.initConnection();
            } catch (ConnectException e) {
                if (this.useSSH) {
                    this.initSession();
                    this.initSSHTunnelledConnection();
                }
                throw e;
            }
        }

        return this.conn;
    }

    @Override
    public void closeConnection() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (this.sess != null)
            this.sess.disconnect();

        this.conn = null;
        this.sess = null;
    }

    private Session doSshTunnel(int nLocalPort) throws JSchException {
        final JSch jsch = new JSch();
        Session session = jsch.getSession(this.strSshUser, this.strSshHost, this.nSshPort);

        if (!this.useKeyFile)
            session.setPassword(this.strSshPassword);
        else
            jsch.addIdentity(new File(this.keyFilePath).getAbsolutePath());

        final Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        session.setPortForwardingL(nLocalPort, this.databaseServerName, this.databaseServerPort);

        return session;
    }
}
