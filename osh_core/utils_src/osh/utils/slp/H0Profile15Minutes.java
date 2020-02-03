package osh.utils.slp;

import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class H0Profile15Minutes implements IH0Profile {

    private final String h0ProfileFileName;

    private final int year;
    private final double yearlyKWh; // in kWh/a

    private final int numberOfDaysInThisYear;
    private final int[] daysPerMonth;


    private final double[] seasonCorrectionFactor = {0.955824945, 1.039432691, 1.003277321};
    private final double[][] seasonWeekdayCorrectionFactor = {
            {0.974925926, 0.974925926, 0.974925926, 0.974925926, 0.974925926, 1.101019664, 1.024350705},
            {0.987019511, 0.987019511, 0.987019511, 0.987019511, 0.987019511, 1.063843913, 1.001058532},
            {0.979653683, 0.979653683, 0.979653683, 0.979653683, 0.979653683, 1.095177467, 1.006554118}};

    // Season 1

    /**
     * [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W
     */
    private final double[][][] h0ProfileArray = new double[3][7][96]; //W

    /**
     * [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W, scaled to yearly kWh
     */
    private final double[][][] h0ProfileArrayScaled = new double[3][7][96]; //W

    /**
     * [Season][Day(Mo,Tu,We,Th,Fr,Sa,Su)][QuarterHour] in W, scaled to yearly kWh, reduced by minimum value
     */
    private final double[][][] h0ProfileArrayWithoutMin = new double[3][7][96]; //W

    private final double[][] seasonWeekdayMinValue = new double[3][7];
    private final double[][] seasonWeekdayMaxValue = new double[3][7];
    private final double[][] seasonWeekdayAvgValue = new double[3][7];

    private double[] correctionFactorForDay;


    /**
     * CONSTRUCTOR
     *
     * @param year
     * @param h0ProfileFileName
     * @param yearlyKWh
     */
    public H0Profile15Minutes(
            int year,
            String h0ProfileFileName,
            double yearlyKWh) {

        this.h0ProfileFileName = h0ProfileFileName;
        this.year = year;
        this.yearlyKWh = yearlyKWh;

        // calculate number of days
        if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
            // leap year
            this.numberOfDaysInThisYear = 366;
            this.daysPerMonth = new int[]{31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        } else {
            // normal year
            this.numberOfDaysInThisYear = 365;
            this.daysPerMonth = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        }

        this.correctionFactorForDay = new double[this.numberOfDaysInThisYear];

        this.calculateCorrectionFactorDay();

        double[][] h0ProfileFile = CSVImporter.readDouble2DimArrayFromFile(h0ProfileFileName, ";");

        // d0 = season, d1 = weekday, d2 = quarter hour
        for (int d0 = 0; d0 < this.h0ProfileArray.length; d0++) {
            for (int d1 = 0; d1 < this.h0ProfileArray[d0].length; d1++) {
                for (int d2 = 0; d2 < this.h0ProfileArray[d0][d1].length; d2++) {
                    this.h0ProfileArray[d0][d1][d2] = h0ProfileFile[d2][d0 * 7 + d1];

                    //scaling, original profile is for 1000 kWh/a
                    this.h0ProfileArrayScaled[d0][d1][d2] = this.h0ProfileArray[d0][d1][d2] * (yearlyKWh / 1000.0);
                }
            }
        }

        // calculate seasonal weekday min values and array of reduced h0Profile
        for (int d0 = 0; d0 < this.h0ProfileArray.length; d0++) {
            for (int d1 = 0; d1 < this.h0ProfileArray[d0].length; d1++) {
                double tempMin = Double.MAX_VALUE;
                double tempMax = Double.MIN_VALUE;
                double tempSum = 0;
                for (int d2 = 0; d2 < this.h0ProfileArray[d0][d1].length; d2++) {
                    tempMin = Math.min(tempMin, this.h0ProfileArray[d0][d1][d2]);
                    tempMax = Math.max(tempMax, this.h0ProfileArray[d0][d1][d2]);
                    tempSum += this.h0ProfileArray[d0][d1][d2];
                }
                this.seasonWeekdayMinValue[d0][d1] = tempMin;
                this.seasonWeekdayMaxValue[d0][d1] = tempMax;
                this.seasonWeekdayAvgValue[d0][d1] = tempSum / this.h0ProfileArray[d0][d1].length;
                for (int d2 = 0; d2 < this.h0ProfileArray[d0][d1].length; d2++) {
                    this.h0ProfileArrayWithoutMin[d0][d1][d2] = this.h0ProfileArray[d0][d1][d2] - tempMin;
                }
            }
        }
        // calculate
    }

    private void calculateCorrectionFactorDay() {

        ZonedDateTime time = ZonedDateTime.of(this.year, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
        double aggregate = 0;

        for (int d0 = 0; d0 < this.numberOfDaysInThisYear; d0++) {
            time = time.plusDays(d0);
            int dayOfWeek = TimeConversion.getCorrectedDayOfWeek(time);
            int month = TimeConversion.getCorrectedMonth(time);
            int dayOfMonth = TimeConversion.getCorrectedDayOfMonth(time);
            int season = this.getSeasonIndexFromDayMonth(dayOfMonth, month);

            this.correctionFactorForDay[d0] = IH0Profile.getBdewDynamizationValue(d0) * this.seasonCorrectionFactor[season] * this.seasonWeekdayCorrectionFactor[season][dayOfWeek];

            aggregate += this.correctionFactorForDay[d0];
        }

        aggregate /= this.numberOfDaysInThisYear;

        if (Math.abs(aggregate - 1.0) > 0.00000001) {
            final double multiplier = aggregate;
            this.correctionFactorForDay = Arrays.stream(this.correctionFactorForDay).map(d -> d / multiplier).toArray();
        }
    }

    public double getCorrectionFactorForTimestamp(ZonedDateTime time) {
        int month = TimeConversion.getCorrectedMonth(time);
        int dayOfWeek = TimeConversion.getCorrectedDayOfWeek(time);
        int dayOfMonth = TimeConversion.getCorrectedDayOfMonth(time);
        int dayOfYear = TimeConversion.getCorrectedDayOfYear(time);

        int season = this.getSeasonIndexFromDayMonth(dayOfMonth, month);


        return IH0Profile.getBdewDynamizationValue(dayOfYear) * this.seasonCorrectionFactor[season] * this.seasonWeekdayCorrectionFactor[season][dayOfWeek];
    }

    /**
     * gets the season index from dayOfMonth and month
     *
     * @param day   1. of month = 1
     * @param month jan = 1, ...
     * @return the season index
     */
    private int getSeasonIndexFromDayMonth(int day, int month) {

        /* boundarys:
         *
         * Winter: 01.11. - 20.03.
         * Summer: 15.05. - 14.09.
         * Intermediate: 21.03. - 14.05. && 15.09. - 31.10
         */

        //def. Winter
        if (month < 3 || month > 10)
            return 0;

        //def. summer
        if (month > 5 && month < 9)
            return 1;

        //def. intermediate
        if (month == 4 || month == 10)
            return 2;

        //winter or intermediate
        if (month == 3) {
            if (day < 21)
                return 0;
            else
                return 2;
        }

        //intermediate or summer
        if (month == 5) {
            if (day < 15)
                return 2;
            else
                return 1;
        }

        //summer or intermediate
        if (month == 9) {
            if (day < 15)
                return 1;
            else
                return 2;
        }

        //Illegal month/day
        return -1;
    }


    public double[][][] getH0ProfileArray() {
        return this.h0ProfileArray;
    }

    public double[][][] getH0ProfileArrayScaled() {
        return this.h0ProfileArrayScaled;
    }


    public double getAvgPercentOfDailyMaxWithoutDailyMin(ZonedDateTime time) {
        int month = TimeConversion.getCorrectedMonth(time);
        int weekday = TimeConversion.getCorrectedDayOfWeek(time);
        int dayOfMonth = TimeConversion.getCorrectedDayOfMonth(time);
        int season = this.getSeasonIndexFromDayMonth(dayOfMonth, month);

        double max = 0;
        for (int i = 0; i < 96; i++) {
            max = Math.max(max, this.h0ProfileArrayWithoutMin[season][weekday][i]);
        }

        double avgPercent = 0;
        for (int i = 0; i < 96; i++) {
            avgPercent += this.h0ProfileArrayWithoutMin[season][weekday][i] / max / 96;
        }

        return avgPercent;
    }

    public double getPercentOfDailyMaxWithoutDailyMin(ZonedDateTime time) {
        int month = TimeConversion.getCorrectedMonth(time);
        int dayOfWeek = TimeConversion.getCorrectedDayOfWeek(time);
        int dayOfMonth = TimeConversion.getCorrectedDayOfMonth(time);
        int dayOfYear = TimeConversion.getCorrectedDayOfYear(time);
        int season = this.getSeasonIndexFromDayMonth(dayOfMonth, month);
        long daySecond = TimeConversion.getSecondsSinceDayStart(time);
        int quarterHour = (int) ((daySecond / (15 * 60)) % 96);

        double max = 0;
        for (int i = 0; i < 96; i++) {
            max = Math.max(max, this.h0ProfileArrayWithoutMin[season][dayOfWeek][i]);
        }
        return this.h0ProfileArrayWithoutMin[season][dayOfWeek][quarterHour] / max;
    }


    public int getActivePowerAt(ZonedDateTime time) {
        int month = TimeConversion.getCorrectedMonth(time);
        int dayOfWeek = TimeConversion.getCorrectedDayOfWeek(time);
        int dayOfMonth = TimeConversion.getCorrectedDayOfMonth(time);
        int dayOfYear = TimeConversion.getCorrectedDayOfYear(time);
        int season = this.getSeasonIndexFromDayMonth(dayOfMonth, month);
        long daySecond = TimeConversion.getSecondsSinceDayStart(time);
        int quarterHour = (int) ((daySecond / (15 * 60)) % 96);

//		double factor1 = IH0Profile.getCorrectedBdewDynamizationValue(dayOfYear);
        double factor3 = IH0Profile.getBdewDynamizationValue(dayOfYear);
        double factor2 = 1.002891587; //H0-Profile and dynamization from csv does not exactly add up to 1000 kWh/a

        return (int) Math.round((this.h0ProfileArrayScaled[season][dayOfWeek][quarterHour] * factor2 * factor3));
    }


    @Override
    public double[] getCorrectionFactorForDay() {
        return this.correctionFactorForDay;
    }
}
