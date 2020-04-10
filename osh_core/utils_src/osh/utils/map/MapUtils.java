package osh.utils.map;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.dataStructures.fastutil.Long2DoubleTreeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Provides
 *
 */
public class MapUtils {

    public static <N extends Number> double getAverageFromMap(NavigableMap<Long, N> map, Long key) {
        Map.Entry<Long, N> floorEntry = map.floorEntry(key);
        N floorValue = floorEntry.getValue();
        Long floorKey = floorEntry.getKey();

        Map.Entry<Long, N> ceilingEntry = map.ceilingEntry(key);

        if (ceilingEntry == null) {
            return floorEntry.getValue().doubleValue();
        }
        N ceilingValue = ceilingEntry.getValue();
        Long ceilingKey = ceilingEntry.getKey();

        if (key.equals(floorKey)) {
            return floorValue.doubleValue();
        } else if (key.equals(ceilingKey)) {
            return ceilingValue.doubleValue();
        } else if (ceilingKey.equals(floorKey)) {
            return floorValue.doubleValue();
        } else {
            double a = (key.doubleValue() - floorKey.doubleValue()) / (ceilingKey.doubleValue() - floorKey.doubleValue());
            return a * ceilingValue.doubleValue() + (1.0 - a) * floorValue.doubleValue();
        }
    }

    public static double getAverageFromMap(Long2DoubleTreeMap map, long key) {
        Long2DoubleMap.Entry floorEntry = map.floorEntry(key);
        if (floorEntry == null) {
            return Double.NaN;
        }

        double floorValue = floorEntry.getDoubleValue();
        long floorKey = floorEntry.getLongKey();

        Long2DoubleMap.Entry ceilingEntry = map.ceilingEntry(key);

        if (ceilingEntry == null) {
            return floorEntry.getDoubleValue();
        }
        double ceilingValue = ceilingEntry.getDoubleValue();
        long ceilingKey = ceilingEntry.getLongKey();

        if (key == floorKey) {
            return floorValue;
        } else if (key == ceilingKey) {
            return ceilingValue;
        } else if (ceilingKey == floorKey) {
            return floorValue;
        } else {
            double a =
                    ((double) (key - floorKey)) / (ceilingKey - floorKey);
            return a * ceilingValue + (1.0 - a) * floorValue;
        }
    }

    public static <N extends Number> TreeMap<Long, N> getTreeMapFromCSV(String csvPath, String delimiter) {
        return internalGetTreeMapFromCSV(csvPath, delimiter, 1);
    }

    public static <N extends Number> TreeMap<Long, N> getTreeMapFromCSV(String csvPath, String delimiter, int index) {
        return internalGetTreeMapFromCSV(csvPath, delimiter, index);
    }

    @SuppressWarnings("unchecked")
    private static <N extends Number> TreeMap<Long, N> internalGetTreeMapFromCSV(String csvPath, String delimiter, int index) {
        TreeMap<Long, N> dataMap = new TreeMap<>();

        try {
            BufferedReader csvReader = new BufferedReader(new FileReader(new File(csvPath)));
            String line;

            while ((line = csvReader.readLine()) != null) {
                String[] splitLine = line.split(delimiter);
                dataMap.put(Long.valueOf(splitLine[0]), (N) Double.valueOf(splitLine[index]));
            }

            csvReader.close();

        } catch (Exception ex) {
            System.out.println("Error reading csv-file: " + ex.getMessage());
        }

        return dataMap;
    }

    public static Map<String, ?> mapFromConfigurationParameterCollection(Collection<ConfigurationParameter> parameters) {
        return parameters.stream().collect(Collectors.toMap(ConfigurationParameter::getParameterName, c -> c));
    }

    public static Map<String, Object> mapFromCPCollectionUnpacked(Collection<ConfigurationParameter> parameters) {
        return parameters.stream().collect(Collectors.toMap(ConfigurationParameter::getParameterName,
                MapUtils::objectFromConfigurationParameter));
    }

    private static Object objectFromConfigurationParameter(ConfigurationParameter cp) {
        try {
            Class<?> cl = Class.forName(cp.getParameterType());
            return cl.getConstructor(String.class).newInstance(cp.getParameterValue());
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException
                | ClassNotFoundException e) {
            throw new IllegalArgumentException();
        }
    }
}
