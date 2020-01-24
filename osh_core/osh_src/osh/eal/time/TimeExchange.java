package osh.eal.time;

import osh.registry.interfaces.IPromiseToBeImmutable;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Exchange object representing a singular moment in time.
 *
 * @author Sebastian Kramer
 */
public class TimeExchange implements Serializable, IPromiseToBeImmutable {

    private static final long serialVersionUID = 5882601912869083826L;

    /**
     * The time events as described by {@link TimeSubscribeEnum} represented by this moment in time.
     */
    private final EnumSet<TimeSubscribeEnum> timeEvents;

    /**
     * The seconds that have passed since epoch.
     */
    private final long secondsSinceEpoch;

    /**
     * This moment in time described by {@link ZonedDateTime}.
     */
    private final ZonedDateTime time;

    /**
     * Constructs this moment in time, with the given time and the given set of events represented by this.
     *
     * @param timeEvents the set of events occuring in this moment in time
     * @param time the time of this moment in time
     */
    public TimeExchange(EnumSet<TimeSubscribeEnum> timeEvents, ZonedDateTime time) {
        Objects.requireNonNull(timeEvents);
        Objects.requireNonNull(time);

        this.timeEvents = timeEvents;
        this.secondsSinceEpoch = time.toEpochSecond();
        this.time = time;
    }

    /**
     * Returns the time events occuring during this moment in time.
     * @return the time events occuring during this moment in time
     */
    public EnumSet<TimeSubscribeEnum> getTimeEvents() {
        return this.timeEvents;
    }

    /**
     * Returns the seconds that have passed since epoch in this moment in time.
     * @return the seconds since epoch
     */
    public long getEpochSecond() {
        return this.secondsSinceEpoch;
    }

    /**
     * Returns the exact time of this moment in time
     * @return the exact time
     */
    public ZonedDateTime getTime() {
        return this.time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        TimeExchange that = (TimeExchange) o;

        if (this.secondsSinceEpoch != that.secondsSinceEpoch) return false;
        if (!this.timeEvents.equals(that.timeEvents)) return false;
        return this.time.equals(that.time);
    }

    @Override
    public int hashCode() {
        int result = this.timeEvents.hashCode();
        result = 31 * result + (int) (this.secondsSinceEpoch ^ (this.secondsSinceEpoch >>> 32));
        result = 31 * result + this.time.hashCode();
        return result;
    }
}
