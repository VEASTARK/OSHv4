package osh.simulation.database;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.datatypes.logging.devices.BaseloadLogObject;
import osh.datatypes.logging.devices.DevicesLogObject;
import osh.datatypes.logging.devices.SmartHeaterLogObject;
import osh.datatypes.logging.devices.WaterTankLogObject;
import osh.datatypes.logging.electrical.DetailedPowerLogObject;
import osh.datatypes.logging.electrical.H0LogObject;
import osh.datatypes.logging.general.EALogObject;
import osh.datatypes.logging.general.PowerLimitSignalLogObject;
import osh.datatypes.logging.general.PriceSignalLogObject;
import osh.datatypes.logging.general.SimulationResultsLogObject;
import osh.datatypes.logging.thermal.ThermalLoggingObject;
import osh.datatypes.logging.thermal.ThermalSupplyLogObject;
import osh.datatypes.power.PowerInterval;
import osh.simulation.OSHSimulationResults;
import osh.utils.dataStructures.fastutil.Long2IntTreeMap;
import osh.utils.functions.TriFunction;
import osh.utils.sql.SQLConnectionProvider;
import osh.utils.string.StringConversions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import static osh.simulation.database.DatabaseTableDefinitions.LogTableType.*;

/**
 * Represents a collection of log-method definitons used for the database logging.
 *
 * @author Sebastian Kramer
 */
public class DatabaseLogMethods {

    protected static Connection[] conn;
    protected static String[] preferredConnection;
    protected static String runName;
    protected static long startTime;

    /**
     * Trys to setup the sql-connection, first trying the given preferred connection.
     *
     * @param preferredConnectionIndex the index of the preferred connection
     */
    protected static void trySetupConnection(int preferredConnectionIndex) {
        try {
            conn[preferredConnectionIndex] = SQLConnectionProvider.getConnection(preferredConnection[preferredConnectionIndex]);
            if (conn == null) {
                throw new Exception("Connection is null, should not happen");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String[] logSimulationResults(SimulationResultsLogObject logObj) {
        String sql;

        OSHSimulationResults results = logObj.getSimResults();

        sql = "REPLACE INTO "
                + "%s"
                + "(RunName, "
                + "ID, "
                + "ID_TEXT, "
                + (logObj.getSimRuntime() != null ? "Runtime, " : "")
                + "StartTime, "
                + "LoggingStartRelative, "
                + "LoggingEndRelative, ";

        sql += String.join(", ", OSHSimulationResults.getDoubleArrayKeys()) + ")";

        sql += "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + (logObj.getSimRuntime() != null ? ", " + logObj.getSimRuntime() : "")
                + ", " + startTime
                + ", " + logObj.getRelativeStart()
                + ", " + logObj.getRelativeEnd() + ", ";
        sql += Arrays.stream(results.getContentsAsDoubleArray()).map(Object::toString).collect(Collectors.joining
                (", ")) + ");";
        return new String[]{sql};
    }

    private static String[] logDetailedPower(DetailedPowerLogObject logObj) {

        AncillaryCommodity[] toLog = {
                AncillaryCommodity.ACTIVEPOWEREXTERNAL,
                AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION,
                AncillaryCommodity.CHPACTIVEPOWERFEEDIN,
                AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION,
                AncillaryCommodity.PVACTIVEPOWERFEEDIN
        };

        List<String> allSql = new ArrayList<>();

        for (AncillaryCommodity a : toLog) {

            long time = Long.MIN_VALUE;
            time = logObj.getLoadProfile().getNextLoadChange(a, time);

            while (time != Long2IntTreeMap.INVALID_KEY) {
                int power = logObj.getLoadProfile().getLoadAt(a, time);

                allSql.add("REPLACE INTO "
                        + "%s"
                        + "(RunName, "
                        + "ID,"
                        + "ID_TEXT,"
                        + "AncillaryCommodity,"
                        + "Time,"
                        + "Power)"

                        + "VALUES ('" + runName + "'"
                        + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                        + ", '" + logObj.getSender().toString() + "'"
                        + ", '" + a
                        + "', " + time
                        + ", " + power + ");");


                time = logObj.getLoadProfile().getNextLoadChange(a, time);
            }
        }

        return allSql.toArray(new String[0]);
    }

    private static String[] logEps(PriceSignalLogObject logObj) {

        if (logObj.getEps().isEmpty()) {
            return new String[0];
        }

        String sqlBase = "REPLACE INTO %s (RunName," + "ID," +  "ID_TEXT, " + "VirtualCommodity, " + "Time, "
                + "Price)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "') ";
        String sqlValues = ", '%s'" + ", %d" + ", %f);";

        List<String> allSqls = new ArrayList<>();

        for (Entry<AncillaryCommodity, PriceSignal> en : logObj.getEps().entrySet()) {
            for (Long2DoubleMap.Entry price : en.getValue().getPrices().long2DoubleEntrySet()) {
                //forces decimal point
                allSqls.add(sqlBase + String.format(Locale.ENGLISH, sqlValues, en.getKey().getCommodity(),
                        price.getLongKey(), price.getDoubleValue()));
            }
        }

        return allSqls.toArray(new String[0]);
    }

    private static String[] logPls(PowerLimitSignalLogObject logObj) {

        if (logObj.getPls().isEmpty()) {
            return new String[0];
        }

        String sqlBase = "REPLACE INTO %s (RunName," + "ID," + "ID_TEXT, " + "VirtualCommodity, " + "Time, "
                + "LowerLimit, " + "UpperLimit)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "' )";
        String sqlValues =  ", '%s'" + ", %d" + ", %f" + ", %f);";

        List<String> allSqls = new ArrayList<>();

        for (Entry<AncillaryCommodity, PowerLimitSignal> en : logObj.getPls().entrySet()) {
            for (Long2ObjectMap.Entry<PowerInterval> power : en.getValue().getLimits().long2ObjectEntrySet()) {
                allSqls.add(sqlBase + String.format(Locale.ENGLISH, sqlValues, en.getKey().getCommodity(),
                        power.getLongKey(), power.getValue().getPowerLowerLimit(),
                        power.getValue().getPowerUpperLimit()));
            }
        }

        return allSqls.toArray(new String[0]);
    }

    private static String[] logH0(H0LogObject logObj) {

        String sql = "REPLACE INTO %s (RunName," + "ID," + "ID_TEXT, " +  "weekDay0, " + "weekDay1, "
                + "weekDay2, " + "weekDay3, " + "weekDay4, " + "weekDay5, " + "weekDay6, " + "days)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[0])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[1])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[2])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[3])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[4])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[5])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateWeekdayPower()[6])
                + "'" + ", '"
                + StringConversions.from2DimDoubleArrayToString(logObj.getAggregateDayPower())
                + "'" + ");";
        return new String[]{sql};
    }

    private static String[] logThermal(ThermalLoggingObject logObj) {

        String hotWaterIdentifier = getWaterTankIdentifier(logObj.getCommodity());

        String sql = "REPLACE INTO %s_" + hotWaterIdentifier + "(RunName," + "ID," + "ID_TEXT, " + "weekDay0, " + "weekDay1, "
                + "weekDay2, " + "weekDay3, " + "weekDay4, " + "weekDay5, " + "weekDay6, " + "days)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", '" + Arrays.toString(logObj.getAggregateWeekdayPower()[0])
                + "'" + ", '" + Arrays.toString(logObj.getAggregateWeekdayPower()[1]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateWeekdayPower()[2]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateWeekdayPower()[3]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateWeekdayPower()[4]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateWeekdayPower()[5]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateWeekdayPower()[6]) + "'" + ", '"
                + Arrays.toString(logObj.getAggregateDayPower()) + "'" + ");";

        return new String[]{sql};
    }

    private static String[] logDevices(DevicesLogObject logObj) {

        String deviceIdentifier = "";
        switch (logObj.getDeviceIdentifier()) {
            case DISHWASHER:
                deviceIdentifier = "DW";
                break;
            case DRYER:
                deviceIdentifier = "TD";
                break;
            case INDUCTIONCOOKTOP:
                deviceIdentifier = "IH";
                break;
            case WASHINGMACHINE:
                deviceIdentifier = "WM";
                break;
            case ELECTRICSTOVE:
                deviceIdentifier = "OV";
                break;
            default:
        }

        String columnIdent = "starts" + deviceIdentifier;
        String columnIdent2 = "Consumption" + deviceIdentifier;
        String columnIdent3 = "dofs" + deviceIdentifier;
        String columnIdent4 = "startTimes" + deviceIdentifier;
        String columnIdent5 = "profilesSelected" + deviceIdentifier;

        String sql = "INSERT INTO %s (RunName," + "ID," + "ID_TEXT, " + columnIdent + "," + columnIdent
                + "R," + columnIdent2 + "," + columnIdent + "Profiles," + columnIdent3 + ","
                + columnIdent4 + ", " + columnIdent5 + ")"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getPlannedDeviceStarts()
                + ", " + logObj.getActualDeviceStarts()
                + ", " + logObj.getActivePowerConsumption()
                + ", '" + Arrays.toString(logObj.getProfileStarts())
                + "', '" + Arrays.toString(logObj.getDofs())
                + "', '" + Arrays.toString(logObj.getStartTimes())
                + "', '" + Arrays.toString(logObj.getProfilesSelected())
                + "') " + "ON DUPLICATE KEY UPDATE "
                + columnIdent + "=VALUES(" + columnIdent
                + "), " + columnIdent + "R=VALUES(" + columnIdent
                + "R), " + columnIdent2 + "=VALUES(" + columnIdent2
                + "), " + columnIdent + "Profiles=VALUES(" + columnIdent + "PROFILES"
                + ")," + columnIdent3 + "=VALUES(" + columnIdent3
                + "), " + columnIdent4 + "=VALUES(" + columnIdent4
                + "), " + columnIdent5 + "=VALUES(" + columnIdent5
                + ");";

        return new String[]{sql};
    }

    private static String[] logBaseload(BaseloadLogObject logObj) {

        String sql = "REPLACE INTO %s (RunName," + "ID," + "ID_TEXT, " + "activePower,"
                + "reactivePower)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getActivePower() + ", " + logObj.getReactivePower()
                + ");";

        return new String[]{sql};
    }

    private static String[] logWaterTank(WaterTankLogObject logObj) {

        String tankIdentifier = getWaterTankIdentifier(logObj.getCommodity());

        String sql = "INSERT INTO %s_" + tankIdentifier
                + "(RunName,"
                + "ID,"
                + "ID_TEXT,"
                + "avgTemperature,"
                + "waterDemand,"
                + "waterSupply,"
                + "lastTemp)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getAverageTemperature()
                + ", " + logObj.getDemand()
                + ", " + logObj.getSupply()
                + ", " + logObj.getLastTemp()
                + ") ON DUPLICATE KEY UPDATE "
                + "avgTemperature=VALUES(avgTemperature)"
                + ", waterDemand=VALUES(waterDemand)"
                + ", waterSupply=VALUES(waterSupply)"
                + ", lastTemp=VALUES(lastTemp);";

        return new String[]{sql};
    }

    private static String[] logThermalSupply(ThermalSupplyLogObject logObj) {

        String tankIdentifier = getWaterTankIdentifier(logObj.getCommodity());


        String sql = "INSERT INTO %s_" + tankIdentifier
                + "(RunName,"
                + "ID,"
                + "ID_TEXT,"
                + "sourceSupply,"
                + "sourceStarts)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getSupply()
                + ", " + logObj.getStarts()
                + ") ON DUPLICATE KEY UPDATE "
                + "sourceSupply=VALUES(sourceSupply)"
                + ", sourceStarts=VALUES(sourceStarts);";

        return new String[]{sql};
    }

   private static String[] logEA(EALogObject logObj) {

        if (Double.isNaN(logObj.getAverageGenerationsUsed())) {
            return new String[0];
        }

        String sql = "REPLACE INTO %s ("
                + "RunName,"
                + "ID,"
                + "ID_TEXT,"
                + "avgGenerationsUsed, "
                + "avgFitnessChange, "
                + "avgFitnessSpread, "
                + "avgHomogeneity, "
                + "noOfOptimizations,"
                + "cervisiaCHP, "
                + "cervisiaHWT, "
                + "cervisiaDW, "
                + "cervisiaTD, "
                + "cervisiaWM)"

                + " VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getAverageGenerationsUsed()
                + ", '" + StringConversions.from2DimDoubleArrayToString(logObj.getAverageFitnessChange())
                + "', '" + StringConversions.from2DimDoubleArrayToString(logObj.getAverageFitnessSpread())
                + "', '" + Arrays.toString(logObj.getAverageHomogeneity())
                + "', " + logObj.getNoOfOptimizations()
                + ", " + logObj.getCervisia()[0]
                + ", " + logObj.getCervisia()[1]
                + ", " + logObj.getCervisia()[2]
                + ", " + logObj.getCervisia()[3]
                + ", " + logObj.getCervisia()[4] + ");";

        return new String[]{sql};
    }

    private static String[] logSmartHeater(SmartHeaterLogObject logObj) {
        String sql = "REPLACE INTO %s ("
                + "RunName,"
                + "ID,"
                + "ID_TEXT,"
                + "switchOns0, "
                + "switchOns1, "
                + "switchOns2, "
                + "runTimes0, "
                + "runTimes1, "
                + "runTimes2, "
                + "powerTierRunTimes0, "
                + "powerTierRunTimes1, "
                + "powerTierRunTimes2, "
                + "powerTierRunTimes3, "
                + "powerTierRunTimes4, "
                + "powerTierRunTimes5, "
                + "powerTierRunTimes6)"

                + "VALUES ('" + runName + "'"
                + ", UNHEX(REPLACE('" + logObj.getSender().toString() + "','-',''))"
                + ", '" + logObj.getSender().toString() + "'"
                + ", " + logObj.getSwitchOns()[0]
                + ", " + logObj.getSwitchOns()[1]
                + ", " + logObj.getSwitchOns()[2]
                + ", " + logObj.getRunTimes()[0]
                + ", " + logObj.getRunTimes()[1]
                + ", " + logObj.getRunTimes()[2]
                + ", " + logObj.getPowerTierRunTimes()[0]
                + ", " + logObj.getPowerTierRunTimes()[1]
                + ", " + logObj.getPowerTierRunTimes()[2]
                + ", " + logObj.getPowerTierRunTimes()[3]
                + ", " + logObj.getPowerTierRunTimes()[4]
                + ", " + logObj.getPowerTierRunTimes()[5]
                + ", " + logObj.getPowerTierRunTimes()[6] + ");";

        return new String[]{sql};
    }

    private static <L extends LoggingObjectStateExchange> void genericLogMethod(L work, DatabaseTableDefinitions.LogTableType type,
                                                                                Function<L, String[]> logMethod, String identifier) {
        for (int i = 0; i < preferredConnection.length; i++) {
            try {
                if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
                    trySetupConnection(i);
            } catch (SQLException e1) {
                e1.printStackTrace();
                return;
            }
            if (conn == null)
                return;
            int tries = 0;
            while (tries < 5) {
                System.out.println("Tried " + type + ": " + tries);
                if (conn != null) {
                    try {
                        Statement stmt = conn[i].createStatement();
                        if (!conn[i].getMetaData().getTables(null, null,
                                DatabaseTableDefinitions.tableTypeToNameMap.get(type), null).next()) {
                            DatabaseTableDefinitions.tableTypeToSetupMap.get(type).accept(conn, i, identifier);
                        }

                        String[] sql = logMethod.apply(work);

                        for (String singleSql : sql) {
                            stmt.executeUpdate(String.format(singleSql, DatabaseTableDefinitions.tableTypeToNameMap.get(type)));
                        }

                        stmt.close();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        trySetupConnection(i);
                        tries++;
                    }
                }
            }
        }
    }

    private static <L extends LoggingObjectStateExchange> void genericBlobLogMethod(L work, DatabaseTableDefinitions.LogTableType type,
                                                                       TriFunction<String, L, Connection, PreparedStatement> logMethod,
                                                                       String identifier) {
        for (int i = 0; i < preferredConnection.length; i++) {
            try {
                if (conn[i] == null || conn[i].isClosed() || !conn[i].isValid(1000))
                    trySetupConnection(i);
            } catch (SQLException e1) {
                e1.printStackTrace();
                return;
            }
            if (conn == null)
                return;
            int tries = 0;
            while (tries < 5) {
                System.out.println("Tried " + type + ": " + tries);
                if (conn != null) {
                    try {
                        PreparedStatement stmt = logMethod.apply(DatabaseTableDefinitions.tableTypeToNameMap.get(type), work, conn[i]);
                        if (!conn[i].getMetaData().getTables(null, null,
                                DatabaseTableDefinitions.tableTypeToNameMap.get(type), null).next()) {
                            DatabaseTableDefinitions.tableTypeToSetupMap.get(type).accept(conn, i, identifier);
                        }
                        stmt.executeUpdate();
                        stmt.close();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                        tries++;
                    }
                }
            }
        }
    }

    /**
     * Persists the given log-object to the database
     *
     * @param work the log object to persists
     * @param <L> the type of the log object
     */
    public static <L extends LoggingObjectStateExchange> void logObject(L work) {
        if (work instanceof SimulationResultsLogObject) {
            genericLogMethod((SimulationResultsLogObject) work, BASE,
                    DatabaseLogMethods::logSimulationResults, "");
        } else if (work instanceof PriceSignalLogObject) {
            genericLogMethod((PriceSignalLogObject) work, EPS,
                    DatabaseLogMethods::logEps, "");
        } else if (work instanceof PowerLimitSignalLogObject) {
            genericLogMethod((PowerLimitSignalLogObject) work, PLS,
                    DatabaseLogMethods::logPls, "");
        } else if (work instanceof H0LogObject) {
            genericLogMethod((H0LogObject) work, H0,
                    DatabaseLogMethods::logH0, "");
        } else if (work instanceof DevicesLogObject) {
            genericLogMethod((DevicesLogObject) work, DEVICES,
                    DatabaseLogMethods::logDevices, "");
        } else if (work instanceof BaseloadLogObject) {
            genericLogMethod((BaseloadLogObject) work, BASELOAD,
                    DatabaseLogMethods::logBaseload, "");
        } else if (work instanceof ThermalLoggingObject) {
            genericLogMethod((ThermalLoggingObject) work, THERMAL,
                    DatabaseLogMethods::logThermal,
                    getWaterTankIdentifier(((ThermalLoggingObject) work).getCommodity()));
        } else if (work instanceof DetailedPowerLogObject) {
            genericLogMethod((DetailedPowerLogObject) work, DETAILED_POWER,
                    DatabaseLogMethods::logDetailedPower, "");
        } else if (work instanceof WaterTankLogObject) {
            genericLogMethod((WaterTankLogObject) work, WATER_TANK,
                    DatabaseLogMethods::logWaterTank,
                    getWaterTankIdentifier(((WaterTankLogObject) work).getCommodity()));
        } else if (work instanceof ThermalSupplyLogObject) {
            genericLogMethod((ThermalSupplyLogObject) work, WATER_TANK,
                    DatabaseLogMethods::logThermalSupply,
                    getWaterTankIdentifier(((ThermalSupplyLogObject) work).getCommodity()));
        } else if (work instanceof EALogObject) {
            genericLogMethod((EALogObject) work, EA,
                    DatabaseLogMethods::logEA, "");
        } else if (work instanceof SmartHeaterLogObject) {
            genericLogMethod((SmartHeaterLogObject) work, SMART_HEATER,
                    DatabaseLogMethods::logSmartHeater, "");
        }  else {
            throw new RuntimeException("non-defined log object");
        }
    }

    private static String getWaterTankIdentifier(Commodity commodity) {
        switch (commodity) {
            case DOMESTICHOTWATERPOWER:
                return "DOM";
            case HEATINGHOTWATERPOWER:
                return "SPACE";
            default:
                return "";
        }
    }
}


