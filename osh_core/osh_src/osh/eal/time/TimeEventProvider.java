package osh.eal.time;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Objects;

import static osh.eal.time.TimeSubscribeEnum.*;

/**
 * Provides utility methods to calculate the set of events represented by a moment in time.
 *
 * @author Sebastian Kramer
 */
public class TimeEventProvider {

    private static ZonedDateTime lastSentEventsTime = ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.of("UTC"));
    private static EnumSet<TimeSubscribeEnum> lastSentEvents = getTimeEvents(lastSentEventsTime);

    private static EnumSet<TimeSubscribeEnum> getTimeEventsFromTime(ZonedDateTime time) {
        EnumSet<TimeSubscribeEnum> timeEvents = EnumSet.noneOf(TimeSubscribeEnum.class);
        timeEvents.add(SECOND);

        if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(MINUTE,
                time.minusSeconds(1)), time)) {
            timeEvents.add(MINUTE);

            if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(HOUR,
                    time.minusSeconds(1)), time)) {
                timeEvents.add(HOUR);

                if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(HALF_DAY,
                        time.minusSeconds(1)), time)) {
                    timeEvents.add(HALF_DAY);

                    if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(DAY,
                            time.minusSeconds(1)), time)) {
                        timeEvents.add(DAY);

                        if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(WEEK,
                                time.minusSeconds(1)), time)) {
                            timeEvents.add(WEEK);
                        }

                        if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(MONTH,
                                time.minusSeconds(1)), time)) {
                            timeEvents.add(MONTH);

                            if (Objects.equals(TimeSubscribeEnum.getNextMatchingTime(YEAR,
                                    time.minusSeconds(1)), time)) {
                                timeEvents.add(YEAR);
                            }
                        }
                    }
                }
            }
        }
        return timeEvents;
    }

    /**
     * Return the set of events represented by the given time. Will resend the saved events if requested more than
     * once with the same time.
     *
     * @param time the given time
     * @return a set of time events represented by the given time
     */
    public static EnumSet<TimeSubscribeEnum> getTimeEvents(ZonedDateTime time) {
        if (!lastSentEventsTime.isEqual(time)) {
            lastSentEventsTime = time;
            lastSentEvents = getTimeEventsFromTime(time);
        }
        return lastSentEvents;
    }
}
