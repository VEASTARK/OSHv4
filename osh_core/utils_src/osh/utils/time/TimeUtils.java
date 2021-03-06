package osh.utils.time;

import java.time.ZonedDateTime;

/**
 * Provides simple utility function for handling {@link ZonedDateTime} objects.
 *
 * @author Sebastian Kramer
 */
public class TimeUtils {

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
