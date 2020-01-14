package osh.driver.simulation.spacecooling;

import osh.core.logging.IGlobalLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class OutdoorTemperatures {

    private final double temperatureCorrection = 2.0;
    private final Map<Long, Double> valuesFromFile;
    private final IGlobalLogger logger;


    /**
     * CONSTRUCTOR
     */
    public OutdoorTemperatures(IGlobalLogger logger, String fileAndPath) {
        this.logger = logger;
        this.valuesFromFile = new HashMap<>();

        BufferedReader file = null;

        try {
            file = new BufferedReader(new FileReader(fileAndPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            logger.logDebug("Error CSV file");
        }

        String line;

        try {
            while ((line = file.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line, ",");
                @SuppressWarnings("unused")
                String id = st.nextToken();
                int day = Integer.parseInt(st.nextToken());
                int month = Integer.parseInt(st.nextToken());
                int minute = Integer.parseInt(st.nextToken());
                int hour = Integer.parseInt(st.nextToken());
                double temperature = Double.parseDouble(st.nextToken());

                ZonedDateTime cal = ZonedDateTime.of(1970, month, day, hour, minute, 0, 0, ZoneId.of("UTC"));
                long timeInSec = cal.toEpochSecond();
                this.valuesFromFile.put(timeInSec, temperature + this.temperatureCorrection);
//			    logger.logDebug(month +" "+  day+" " +  hour+" " +  minute + " = " + timeInSec);
            }
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
            logger.logDebug("Error parsing file", e);
        }
    }


    public double getTemperature(long timestamp) {
        try {
            return this.valuesFromFile.get((timestamp / 300) * 300);
        } catch (Exception e) {
            e.printStackTrace();
            this.logger.logDebug(timestamp);
            this.logger.logDebug((timestamp / 300) * 300);
            System.exit(0);
            return 22.0;
        }
    }

    /**
     * Handle with care. Do NOT alter/modify/whatever to map!
     */
    public Map<Long, Double> getMap() {
        return this.valuesFromFile;
    }

}
