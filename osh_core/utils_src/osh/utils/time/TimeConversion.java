package osh.utils.time;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Contains utility-function for handling date-time objects.
 *
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 */
public class TimeConversion {

    //This should be adjusted for where the system is running
    private static ZoneId zone = ZoneId.of("UTC");

    /**
     * Sets the time-zone for all future calculation to the provided time-zone.
     *
     * @param zone the new time-zone
     */
    public static void setZone(ZoneId zone) {
        TimeConversion.zone = zone;
    }

    /**
     * Returns the month-value of the given date as a zero-based index.
     *
     * @param time the date
     * @return the month as a zero-based index (0 = january, ...)
     */
    public static int getCorrectedMonth(ZonedDateTime time) {
        return time.getMonthValue() - 1;
    }

    /**
     * Returns the day-of-year-value of the given date as a zero-based index.
     *
     * @param time the date
     * @return the day-of-year as a zero-based index (0 = 01.01., ...)
     */
    public static int getCorrectedDayOfYear(ZonedDateTime time) {
        return time.getDayOfYear() - 1;
    }

    /**
     * Returns the day-of-week-value of the given date as a zero-based index.
     *
     * @param time the date
     * @return the day-of-week as a zero-based index (0 = monday, ...)
     */
    public static int getCorrectedDayOfWeek(ZonedDateTime time) {
        return time.getDayOfWeek().ordinal();
    }

    /**
     * Returns the day-of-month-value of the given date as a zero-based index.
     *
     * @param time the date
     * @return the day-of-week as a zero-based index (0 = 1st, ...)
     */
    public static int getCorrectedDayOfMonth(ZonedDateTime time) {
        return time.getDayOfMonth() - 1;
    }

    /**
     * Returns the number of seconds passed since the start of the year at the given time.
     *
     * @param time the time
     * @return the number of seconds passed since the start of the year
     */
    public static long getSecondsSinceYearStart(ZonedDateTime time) {
        ZonedDateTime yearStart = getStartOfYear(time);
        return Duration.between(yearStart, time).toSeconds();
    }

    /**
     * Returns the number of seconds passed since the start of the day at the given time.
     *
     * @param time the time
     * @return the number of seconds passed since the start of the day
     */
    public static long getSecondsSinceDayStart(ZonedDateTime time) {
        return Duration.between(getStartOfDay(time), time).toSeconds();
    }

    /**
     * Returns the number of minutes passed since the start of the day at the given time.
     *
     * @param time the time
     * @return the number of minutes passed since the start of the day
     */
    public static int getMinutesSinceDayStart(ZonedDateTime time) {
        return (int) Duration.between(getStartOfDay(time), time).toMinutes();
    }

    /**
     * Returns the time at the start of the day of the given time.
     *
     * @param time the time
     * @return the time at the start of the day
     */
    public static ZonedDateTime getStartOfDay(ZonedDateTime time) {
        return time.truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Returns the time at the start of the week of the given time.
     *
     * @param time the time
     * @return the time at the start of the week
     */
    public static ZonedDateTime getStartOfWeek(ZonedDateTime time) {
        return time.truncatedTo(ChronoUnit.WEEKS);
    }

    /**
     * Returns the time at the start of the month of the given time.
     *
     * @param time the time
     * @return the time at the start of the month
     */
    public static ZonedDateTime getStartOfMonth(ZonedDateTime time) {
        return time.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Returns the time at the start of the year of the given time.
     *
     * @param time the time
     * @return the time at the start of the year
     */
    public static ZonedDateTime getStartOfYear(ZonedDateTime time) {
        return time.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
    }

    /**
     * Returns the day-of-week-value of the given unix-time as a zero-based index.
     *
     * @param unixTime the unix-time
     * @return the day-of-week as a zero-based index (0 = monday, ...)
     */
    public static int convertUnixTime2CorrectedDayOfWeek(long unixTime) {
        Instant time = Instant.ofEpochSecond(unixTime);
        ZonedDateTime zdt = time.atZone(zone);
        return zdt.getDayOfWeek().getValue() - 1; //DoW indexes from Mon=1 to Sun=7 so we need to adjust
    }

    /**
     * Returns the amount of seconds that have passed since the start-of-day of the given unix-time.
     *
     * @param unixTime the unix-time
     * @return the amount of seconds that have passed since the start-of-day
     */
    public static int convertUnixTime2SecondsSinceMidnight(long unixTime) {
        Instant time = Instant.ofEpochSecond(unixTime);
        ZonedDateTime zdt = time.atZone(zone);
        return (int) getSecondsSinceDayStart(zdt);
    }

    /**
     * Returns a {@link ZonedDateTime} object corresponding to the given unix-time.
     *
     * @param currentUnixTime the unix-time
     * @return {@link ZonedDateTime} object corresponding to the currentUnixTime
     */
    public static ZonedDateTime convertUnixTimeToZonedDateTime(long currentUnixTime) {
        return Instant.ofEpochSecond(currentUnixTime).atZone(zone);
    }

    /**
     * Returns the number of days in the year of the given time.
     *
     * @param time the time
     * @return the number of days in the year
     */
    public static int getNumberOfDaysInYearFromTime(ZonedDateTime time) {
        return (int) time.range(ChronoField.DAY_OF_YEAR).getMaximum();
    }
}
