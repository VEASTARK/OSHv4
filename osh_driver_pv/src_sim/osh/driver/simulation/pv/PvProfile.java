package osh.driver.simulation.pv;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;


/**
 * Resolution = 15 minutes (intervals)
 *
 * @author Ingo Mauser
 */
public class PvProfile {

    /**
     * [Month][Day(Mo,Tu/We/Th,Fr,Sa,Su)][Hour]
     */
    private int[][][] pvProfileArray;

    private final int nominalPower;


    /**
     * @param pvProfileFilename
     * @param nominalPower      in W
     */
    public PvProfile(String pvProfileFilename, int nominalPower) {
        this.nominalPower = nominalPower;
        this.init(pvProfileFilename, nominalPower);
    }

    private void init(String pvProfileFilename, int nominalPower) {
        double[][] pvProfileFile = CSVImporter.readDouble2DimArrayFromFile(pvProfileFilename, ";");
        this.pvProfileArray = new int[12][1][96];
        for (int j = 0; j < 96; j++) {
            for (int k = 0; k < 12; k++) {
                this.pvProfileArray[k][0][j] = (int) Math.round((pvProfileFile[j][k] * nominalPower));
            }
        }
    }

    /**
     * IMPORTANT: Value <= 0 (Generating Power!)
     *
     * @param time
     * @return Value <= 0 in W
     */
    public int getPowerAt(ZonedDateTime time) {
        int month = TimeConversion.getCorrectedMonth(time);
        long daySeconds = TimeConversion.getSecondsSinceDayStart(time);

        // Do NOT use Math.round()!!!
        int interval = (int) ((double) daySeconds / (60 * 15));

        int power = this.pvProfileArray[month][0][interval];

//		// randomize
//		int day = (int) (timeStamp / 86400);
//		day = day % randomDay.length;
//		power = (int) (2 * power * randomDay[day]);

        // to be safe...
        if (power > 0) {
            power = (-1) * power;
        }

        return power;
    }

    public SparseLoadProfile getProfileForDayOfYear(ZonedDateTime date) {
        int month = date.getMonthValue() - 1;
        int startInterval = (date.getDayOfMonth() - 1) * 96;

        SparseLoadProfile slp = new SparseLoadProfile();

        for (int i = 0; i < 96; i++) {

            int power = this.pvProfileArray[month][0][startInterval + i];

            // to be safe...
            if (power > 0) {
                power = (-1) * power;
            }

            slp.setLoad(Commodity.ACTIVEPOWER, i * 15, power);
        }

        slp.setEndingTimeOfProfile(86400);
        return slp;
    }

    public int getNominalPower() {
        return this.nominalPower;
    }

    public int[][][] getPvProfileArray() {
        return this.pvProfileArray;
    }

}
