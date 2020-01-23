package osh.eal.time;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents different time periods components of this OSH can subscribe to.
 *
 * @author Sebastian Kramer
 */
public enum TimeSubscribeEnum {

    SECOND,
    MINUTE,
    HOUR,
    HALF_DAY,
    DAY,
    WEEK,
    MONTH,
    YEAR;

    /**
     * Calculates the next time the time event represented by the given {@link TimeSubscribeEnum} will occur based on
     * the given current time
     *
     * @param timeEvent the time event
     * @param time the current time
     * @return the next time this given time event will occur after the given current time
     */
    public static ZonedDateTime getNextMatchingTime(TimeSubscribeEnum timeEvent, ZonedDateTime time) {
        switch (timeEvent) {
            case SECOND:
                return time.plusSeconds(1);
            case MINUTE:
                return time.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
            case HOUR:
                return time.plusHours(1).truncatedTo(ChronoUnit.HOURS);
            case HALF_DAY:
                if (time.getHour() < 12) {
                    return time.withHour(12).truncatedTo(ChronoUnit.HOURS);
                } else {
                    return time.plusDays(1).truncatedTo(ChronoUnit.DAYS);
                }
            case DAY:
                return time.plusDays(1).truncatedTo(ChronoUnit.DAYS);
            case WEEK:
                return time.minusDays(time.getDayOfWeek().getValue() - 1).plusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            case MONTH:
                return time.plusMonths(1).truncatedTo(ChronoUnit.DAYS).withDayOfMonth(1);
            case YEAR:
                return time.plusYears(1).truncatedTo(ChronoUnit.DAYS).withDayOfYear(1);
            default:
                return null;
        }
    }
}
