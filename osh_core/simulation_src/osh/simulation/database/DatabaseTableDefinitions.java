package osh.simulation.database;

import osh.simulation.OSHSimulationResults;
import osh.utils.functions.SQLTriConsumer;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;

/**
 * Represents a collection of table definitons used for the database logging.
 *
 * @author Sebastian Kramer
 */
public class DatabaseTableDefinitions {

    private static final String additionalMysqlOptions = "ENGINE=InnoDB ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=16";

    /**
     * Represents all possible table types in use
     *
     * @author Sebastian Kramer
     */
    public enum LogTableType {
        BASE,
        EPS,
        PLS,
        H0,
        DEVICES,
        BASELOAD,
        THERMAL,
        DETAILED_POWER,
        WATER_TANK,
        EA,
        SMART_HEATER,
    }

    public static final EnumMap<LogTableType, String> tableTypeToNameMap = new EnumMap<>(LogTableType.class);
    public static final EnumMap<LogTableType, SQLTriConsumer<Connection[], Integer, String>> tableTypeToSetupMap =
            new EnumMap<>
            (LogTableType.class);

    static {
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.BASE, DatabaseTableDefinitions::setupTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.EPS, DatabaseTableDefinitions::setupEpsTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.PLS, DatabaseTableDefinitions::setupPlsTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.H0, DatabaseTableDefinitions::setupH0Table);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.DEVICES, DatabaseTableDefinitions::setupDevicesTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.BASELOAD, DatabaseTableDefinitions::setupBaseloadTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.THERMAL, DatabaseTableDefinitions::setupThermalTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.DETAILED_POWER, DatabaseTableDefinitions::setupDetailedPowerTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.WATER_TANK, DatabaseTableDefinitions::setupWaterTankTable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.EA, DatabaseTableDefinitions::setupGATable);
        DatabaseTableDefinitions.tableTypeToSetupMap.put(LogTableType.SMART_HEATER, DatabaseTableDefinitions::setupSmartHeaterTable);
    }

    /**
     * Initializes all the table names, based on a given root name.
     *
     * @param rootName the root name
     */
    public static void initTableNames(String rootName) {
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.BASE, rootName);
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.EPS, rootName + "_EPS");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.PLS, rootName + "_PLS");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.H0, rootName + "_H0");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.DEVICES, rootName + "_Devices");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.BASELOAD, rootName + "_Baseload");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.THERMAL, rootName + "_HotWater");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.DETAILED_POWER, rootName + "_DetailedPower");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.WATER_TANK, rootName + "_WaterTank");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.EA, rootName + "_GA");
        DatabaseTableDefinitions.tableTypeToNameMap.put(LogTableType.SMART_HEATER, rootName + "_SmartHeater");
    }


    // processed simulation results for one building
    private static void setupTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        StringBuilder sqlT = new StringBuilder("CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.BASE)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " Runtime BIGINT, "
                + " StartTime BIGINT NOT NULL, "
                + " LoggingStartRelative BIGINT NOT NULL, "
                + " LoggingEndRelative BIGINT NOT NULL, ");

        for (String s : OSHSimulationResults.getDoubleArrayKeys()) {
            sqlT.append(" ").append(s).append(" DOUBLE NOT NULL, ");
        }

        sqlT.append(" PRIMARY KEY (Runname, ID, StartTime, LoggingStartRelative, LoggingEndRelative)" + ")" + additionalMysqlOptions + ";");
        stmt.executeUpdate(sqlT.toString());
    }

    private static void setupDetailedPowerTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.DETAILED_POWER)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " AncillaryCommodity VARCHAR(565) NOT NULL, "
                + " Time BIGINT NOT NULL, "
                + " Power Int NOT NULL, "
                + " PRIMARY KEY (RunName, ID, AncillaryCommodity, Time ))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupEpsTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.EPS)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " VirtualCommodity VARCHAR(565) NOT NULL, "
                + " Time BIGINT NOT NULL, "
                + " Price DOUBLE NOT NULL, "
                + " PRIMARY KEY (RunName, ID, VirtualCommodity, Time ))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupPlsTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.PLS)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " VirtualCommodity VARCHAR(565) NOT NULL, "
                + " Time BIGINT NOT NULL, "
                + " LowerLimit DOUBLE NOT NULL, "
                + " UpperLimit DOUBLE NOT NULL, "
                + " PRIMARY KEY (RunName, ID, VirtualCommodity, Time ))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupH0Table(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.H0)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " weekDay0 MEDIUMTEXT NOT NULL, "
                + " weekDay1 MEDIUMTEXT NOT NULL, "
                + " weekDay2 MEDIUMTEXT NOT NULL, "
                + " weekDay3 MEDIUMTEXT NOT NULL, "
                + " weekDay4 MEDIUMTEXT NOT NULL, "
                + " weekDay5 MEDIUMTEXT NOT NULL, "
                + " weekDay6 MEDIUMTEXT NOT NULL, "
                + " days MEDIUMTEXT NOT NULL, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupThermalTable(Connection[] conn, int preferredConnectionIndex, String hotWaterIdentifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.THERMAL) + "_" + hotWaterIdentifier
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " weekDay0 TEXT NOT NULL, "
                + " weekDay1 TEXT NOT NULL, "
                + " weekDay2 TEXT NOT NULL, "
                + " weekDay3 TEXT NOT NULL, "
                + " weekDay4 TEXT NOT NULL, "
                + " weekDay5 TEXT NOT NULL, "
                + " weekDay6 TEXT NOT NULL, "
                + " days TEXT NOT NULL, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupDevicesTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.DEVICES)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " startsDW INT DEFAULT 0, "
                + " startsIH INT DEFAULT 0, "
                + " startsOV INT DEFAULT 0, "
                + " startsTD INT DEFAULT 0, "
                + " StartsWM INT DEFAULT 0, "
                + " startsDWR INT DEFAULT 0, "
                + " startsIHR INT DEFAULT 0, "
                + " startsOVR INT DEFAULT 0, "
                + " startsTDR INT DEFAULT 0, "
                + " StartsWMR INT DEFAULT 0, "
                + " ConsumptionDW DOUBLE DEFAULT 0, "
                + " ConsumptionIH DOUBLE DEFAULT 0, "
                + " ConsumptionOV DOUBLE DEFAULT 0, "
                + " ConsumptionTD DOUBLE DEFAULT 0, "
                + " ConsumptionWM DOUBLE DEFAULT 0, "
                + " startsDWProfiles TEXT, "
                + " startsIHProfiles TEXT, "
                + " startsOVProfiles TEXT, "
                + " startsTDProfiles TEXT, "
                + " startsWMProfiles TEXT, "
                + " dofsDW TEXT, "
                + " dofsIH TEXT, "
                + " dofsOV TEXT, "
                + " dofsTD TEXT, "
                + " dofsWM TEXT, "
                + " profilesSelectedDW TEXT, "
                + " profilesSelectedIH TEXT, "
                + " profilesSelectedOV TEXT, "
                + " profilesSelectedTD TEXT, "
                + " profilesSelectedWM TEXT, "
                + " startTimesDW TEXT, "
                + " startTimesIH TEXT, "
                + " startTimesOV TEXT, "
                + " startTimesTD TEXT, "
                + " startTimesWM TEXT, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupBaseloadTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.BASELOAD)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " activePower DOUBLE NOT NULL, "
                + " reactivePower DOUBLE NOT NULL, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupWaterTankTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.WATER_TANK) + "_" + identifier
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " avgTemperature DOUBLE, "
                + " waterDemand DOUBLE, "
                + " waterSupply DOUBLE, "
                + " sourceSupply DOUBLE, "
                + " sourceStarts INT, "
                + " lastTemp DOUBLE,"
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupGATable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.EA)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " avgGenerationsUsed DOUBLE NOT NULL, "
                + " avgFitnessChange TEXT NOT NULL, "
                + " avgFitnessSpread TEXT NOT NULL, "
                + " avgHomogeneity TEXT NOT NULL, "
                + " noOfOptimizations INT NOT NULL, "
                + " cervisiaCHP DOUBLE NOT NULL, "
                + " cervisiaHWT DOUBLE NOT NULL, "
                + " cervisiaDW DOUBLE NOT NULL, "
                + " cervisiaTD DOUBLE NOT NULL, "
                + " cervisiaWM DOUBLE NOT NULL, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }

    private static void setupSmartHeaterTable(Connection[] conn, int preferredConnectionIndex, String identifier) throws SQLException {
        Statement stmt = conn[preferredConnectionIndex].createStatement();
        String sqlT = "CREATE TABLE "
                + DatabaseTableDefinitions.tableTypeToNameMap.get(LogTableType.SMART_HEATER)
                + "(RunName VARCHAR(565) NOT NULL, "
                + " ID BINARY(16) NOT NULL, "
                + " ID_TEXT VARCHAR(36) NOT NULL, "
                + " switchOns0 INT NOT NULL, "
                + " switchOns1 INT NOT NULL, "
                + " switchOns2 INT NOT NULL, "
                + " runTimes0 BIGINT NOT NULL, "
                + " runTimes1 BIGINT NOT NULL, "
                + " runTimes2 BIGINT NOT NULL, "
                + " powerTierRunTimes0 BIGINT NOT NULL, "
                + " powerTierRunTimes1 BIGINT NOT NULL, "
                + " powerTierRunTimes2 BIGINT NOT NULL, "
                + " powerTierRunTimes3 BIGINT NOT NULL, "
                + " powerTierRunTimes4 BIGINT NOT NULL, "
                + " powerTierRunTimes5 BIGINT NOT NULL, "
                + " powerTierRunTimes6 BIGINT NOT NULL, "
                + " PRIMARY KEY (RunName, ID))" + additionalMysqlOptions + ";";
        stmt.executeUpdate(sqlT);
    }
}
