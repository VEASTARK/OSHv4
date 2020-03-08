package osh.utils.slp;

import java.time.ZonedDateTime;

/**
 * interface for methods which a H0-Profile should provide
 *
 * @author Sebastian Kramer
 */
public interface IH0Profile {

    /**
     * EN: Dynamization function of German for H0, mean = 1.004464866
     *
     * @param dayOfYear 1. Jan = 0
     * @return
     */
    static double getBdewDynamizationValue(int dayOfYear) {
        int correctedDayOfYear = dayOfYear + 1;
        return -0.000000000392 * Math.pow(correctedDayOfYear, 4)
                + 0.00000032 * Math.pow(correctedDayOfYear, 3)
                + -0.0000702 * Math.pow(correctedDayOfYear, 2)
                + 0.0021 * Math.pow(correctedDayOfYear, 1) + 1.24;
    }

    /**
     * EN: Corrected ynamization function of German for H0, mean = 1.0
     *
     * @param dayOfYear 1. Jan = 0
     * @return
     */
    static double getCorrectedBdewDynamizationValue(int dayOfYear) {
        return getBdewDynamizationValue(dayOfYear) / 1.004464866; // correction because integral of dynamization function is not 1
    }

    /**
     * gets the baseload power for the given time
     *
     * @param time time
     * @return baseload power for the given time
     */
    int getActivePowerAt(ZonedDateTime time);

    /**
     * gets the percentage the power of the given time is between the daily maximum and minimum
     *
     * @param time time
     * @return the percentage the power of the given time is between the daily maximum and minimum
     */
    double getPercentOfDailyMaxWithoutDailyMin(ZonedDateTime time);

    /**
     * gets the average percentage the power during the whole day of the given time is between the daily maximum and minimum
     *
     * @param time time
     * @return the average percentage the power during the whole day of the given time is between the daily maximum and minimum
     */
    double getAvgPercentOfDailyMaxWithoutDailyMin(ZonedDateTime time);

    /**
     * gets the probability correction factor to which one has to correct a distribution to follow the H0-Profile
     *
     * @param time time
     * @return the probability correction factor to which one has to correct a distribution to follow the H0-Profile
     */
    double getCorrectionFactorForTimestamp(ZonedDateTime time);

    /**
     * gets the correction factors as an array for every day in the year
     * <p>
     * February will always be given assumed as having 29 days, in non-leap years the 29th value can just be ignored
     *
     * @return the correction factors as an array for every day in the year
     */
    double[] getCorrectionFactorForDay();
}
