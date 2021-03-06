package osh.driver.simulation.thermal;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;

/**
 * @author Florian Allerding, Sebastian Kramer, Ingo Mauser, Till Schuberth
 */
public class ThermalDemandData {

    private final double[] sumDemand;
    private final Commodity hotWaterType;


    /**
     * CONSTRUCTOR
     */
    public ThermalDemandData(
            String inputFile,
            Commodity hotWaterType) {

        // red input file
        this.sumDemand = CSVImporter.readDouble1DimArrayFromFile(inputFile);
        this.hotWaterType = hotWaterType;
    }


    /**
     * @param time
     * @param randomDev    e {0, 1, 2} -> +-1h
     * @param randomDevMax % 2 == 0
     * @param randomDevMax % 2 == 0
     * @return
     */
    public double getTotalThermalDemand(ZonedDateTime time, int randomDev, int randomDevMax) {
        int day = TimeConversion.getCorrectedDayOfYear(time);
        int hour = (int) (TimeConversion.getSecondsSinceDayStart(time) / 3600);

        hour = hour + randomDev - (randomDevMax / 2);
        if (hour < 0) {
            hour += 24;
            day -= 1;
        }
        if (hour > 23) {
            hour -= 24;
            day += 1;
        }

        if (day < 0 || day >= 365) {
            day = Math.floorMod(day, 365);
        }
        return this.sumDemand[day * 24 + hour];
    }

    public SparseLoadProfile getProfileForDayOfYear(int day) {
        int dayOfYear = day;
        if (dayOfYear < 0 || dayOfYear >= 365) {
            dayOfYear = Math.floorMod(dayOfYear, 365);
        }

        SparseLoadProfile slp = new SparseLoadProfile();

        for (int hour = 0; hour < 24; hour++) {
            slp.setLoad(this.hotWaterType, hour * 3600, (int) this.sumDemand[dayOfYear * 24 + hour]);
        }

        slp.setEndingTimeOfProfile(86400);

        return slp;
    }

}
