package osh.utils.sql;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.JSchException;
import osh.utils.string.ParameterConstants.Database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Sebastian Kramer
 */
public class SQLConnectionProvider {

    private static SingleSQLConnectionProvider connection;

    private static final HashMap<String, SingleSQLConnectionProvider> connectionMap = new HashMap<>();

    static {

        ObjectMapper mapper = new ObjectMapper();

        File folder = new File("configfiles/database/config");

        for (File child : Objects.requireNonNull(folder.listFiles())) {
            HashMap<String, String> map;
            try {
                BufferedReader br = new BufferedReader(new FileReader(child));
                String value = br.lines().collect(Collectors.joining());
                br.close();

                map = mapper.readValue(value, new TypeReference<HashMap<String, String>>() {
                });

                if (map.containsKey(Database.type) && map.get(Database.type).equalsIgnoreCase(Database.tunneled)) {
                    connectionMap.put(map.get(Database.identifier), new SingleSQLAndSSHConnectionProvider(map));
                } else {
                    connectionMap.put(map.get(Database.identifier), new SingleSQLConnectionProvider(map));
                }
            } catch (IllegalArgumentException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void initSQLConnection() throws ConnectException {

        for (SingleSQLConnectionProvider provider : connectionMap.values()) {
            try {
                provider.getConnection();
                connection = provider;
                return;

            } catch (ConnectException | JSchException ignored) {
            }
        }
        throw new ConnectException("No database reachable");
    }


    private static void initSQLConnection(String preferredConnection) throws ConnectException {

        if (preferredConnection != null && !preferredConnection.isEmpty() && connectionMap.containsKey(preferredConnection)) {
            try {
                Connection conn = connectionMap.get(preferredConnection).getConnection();
                connection = connectionMap.get(preferredConnection);

                if (conn != null && !conn.isClosed() && conn.isValid(1000)) {
                    return;
                }

            } catch (ConnectException | JSchException | SQLException ignored) {
            }
        }

        initSQLConnection();
    }

    /**
     * tries to get a connection from the array of possible connections
     *
     * @return a connection to a mysql-server
     * @throws ConnectException if no connection is available
     */
    public static Connection getConnection() throws ConnectException, JSchException {
        if (connection == null)
            initSQLConnection();

        return connection.getConnection();
    }

    /**
     * tries to get a connection but tests a preferred connection first
     *
     * @param preferredConnection the preferred connection
     * @return a connection to a mysql-server (if the preferred connection is available it will be returned, otherwise the first successful connection)
     * @throws ConnectException if no connection is available
     */
    public static Connection getConnection(String preferredConnection) throws ConnectException, JSchException {
        initSQLConnection(preferredConnection);

        return connection.getConnection();
    }

    public static void closeConnection() {
        if (connection != null) {
            connection.closeConnection();
            connection = null;
        }
    }
}
