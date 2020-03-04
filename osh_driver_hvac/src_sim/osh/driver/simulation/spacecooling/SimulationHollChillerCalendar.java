package osh.driver.simulation.spacecooling;

import osh.core.OSHRandom;
import osh.driver.datatypes.cooling.ChillerCalendarDate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class SimulationHollChillerCalendar
        extends ChillerCalendarSimulation {

    //SETTINGS
    static final int minDatesPerDay = 1;
    static final int maxDatesPerDay = 2;
    static final int minPersonsPerDate = 2;
    static final int maxPersonsPerDate = 5;
    static final int minLengthOfDate = 2;
    static final int maxLengthOfDate = 4;
    static final int minPause = 2;
    static final int maxPause = 3;
    private final OSHRandom random;
    private long minTimeToAddNextDate;

    //HELPER VARIABLES
    private final ArrayList<ChillerCalendarDate> dates = new ArrayList<>();


    /**
     * CONSTRUCTOR
     */
    public SimulationHollChillerCalendar(OSHRandom random) {
        super();
        this.random = random;
    }


    public ArrayList<ChillerCalendarDate> getDate(long timestamp) {

        //TERMINE FÜR DEN TAG GENERIEREN
        OSHRandom newRandomGen = new OSHRandom(this.random.getNextLong());

        int datesPerDay = newRandomGen.getNextInt(maxDatesPerDay - minDatesPerDay + 1) + minDatesPerDay;

        System.out.println("Termine für diesen Tag: " + datesPerDay);

        this.minTimeToAddNextDate = (timestamp + 3600 * 8) + (3600 * (newRandomGen.getNextInt(maxPause + 1)));

        for (int i = 0; i < datesPerDay; i++) {

            long length = 3600 * (newRandomGen.getNextInt(maxLengthOfDate - minLengthOfDate + 1) + minLengthOfDate);
            int personsPerDate = newRandomGen.getNextInt(maxPersonsPerDate - minPersonsPerDate + 1) + minPersonsPerDate;

            ZonedDateTime time = ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.minTimeToAddNextDate), ZoneId.of(
                    "UTC"));

            int hour = time.getHour();

            System.out.println("Termin um: " + hour + " Uhr | " + length / 3600 + " Stunden");

            ChillerCalendarDate date = new ChillerCalendarDate(this.minTimeToAddNextDate, length, personsPerDate, 22.0, Integer.MAX_VALUE);

            int pause = 3600 * (newRandomGen.getNextInt(maxPause - minPause + 1) + minPause);

            this.minTimeToAddNextDate += length + pause;

            this.dates.add(date);
        }

        return this.dates;
    }

}
