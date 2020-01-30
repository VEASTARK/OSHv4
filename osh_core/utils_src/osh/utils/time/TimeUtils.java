package osh.utils.time;

import java.time.ZonedDateTime;

/**
 * Provides simple utility function for handling {@link ZonedDateTime} objects.
 *
 * @author Sebastian Kramer
 */
public class TimeUtils {

    /**
     * Returns if the first given time is equal or after the second given time.
     *
     * @param time the first time
     * @param other the second time
     *
     * @return true if the first time is equal or after the second time
     */
    public static boolean isAfterEquals(ZonedDateTime time, ZonedDateTime other) {
        return other.isBefore(time);
    }

    /**
     * Returns if the first given time is equal or before the second given time.
     *
     * @param time the first time
     * @param other the second time
     *
     * @return true if the first time is equal or before the second time
     */
    public static boolean isBeforeEquals(ZonedDateTime time, ZonedDateTime other) {
        return other.isAfter(time);
    }

    /**
     * Returns the earlier of the two given times
     *
     * @param time the first time
     * @param other the second time
     *
     * @return the earlier of the two times
     */
    public static ZonedDateTime getEarlierTime(ZonedDateTime time, ZonedDateTime other) {
        return time.compareTo(other) < 0 ? time : other;
    }
}
