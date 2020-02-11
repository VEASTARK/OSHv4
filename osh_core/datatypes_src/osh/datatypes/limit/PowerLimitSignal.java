package osh.datatypes.limit;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import osh.datatypes.power.PowerInterval;
import osh.utils.dataStructures.fastutil.Long2ObjectTreeMap;

import java.util.Objects;

/**
 * Represents a signal limiting the power draw, mapping a known time-interval to upper- and lower limits for power.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class PowerLimitSignal {

    /**
     * default limit when none is known
     */
    private final PowerInterval UNKNOWN_LIMIT = new PowerInterval();
    private Long2ObjectTreeMap<PowerInterval> powerLimits;
    /**
     * Flag whether the redundant entries have been removed
     */
    private boolean isCompressed = true;

    private long limitUnknownBefore;
    private long limitUnknownAtAndAfter;

    /**
     * Constructs an empty power-limit signal.
     */
    public PowerLimitSignal() {
        this.powerLimits = new Long2ObjectTreeMap<>();
    }

    /**
     * Constructs a power-limit signal based on the given mappings.
     *
     * @param limits the mapping
     */
    public PowerLimitSignal(Long2ObjectSortedMap<PowerInterval> limits) {
        this.powerLimits = new Long2ObjectTreeMap<>(limits);
        this.limitUnknownBefore = limits.firstLongKey();
        this.limitUnknownAtAndAfter = limits.lastLongKey();
        this.isCompressed = false;
    }

    /**
     * Constructs a power-limit signal based on the given mappings and applies the given offset to the time interval.
     *
     * @param limits the mapping
     * @param offset the offset
     */
    public PowerLimitSignal(Long2ObjectSortedMap<PowerInterval> limits, long offset) {
        this.powerLimits = new Long2ObjectTreeMap<>();
        Long2ObjectMaps.fastForEach(limits, e -> this.powerLimits.put(e.getLongKey() + offset, e.getValue().clone()));
        this.limitUnknownBefore = limits.firstLongKey() + offset;
        this.limitUnknownAtAndAfter = limits.lastLongKey() + offset;
        this.isCompressed = false;
    }

    /**
     * Sets the known power-limit to the given value at the given time
     *
     * @param time the time for the new mapping
     * @param limit the limit for the new mapping
     */
    public void setPowerLimit(long time, PowerInterval limit) {
        Objects.requireNonNull(limit);
        this.powerLimits.put(time, limit);
        this.isCompressed = false;
    }

    /**
     * Sets the known power-limit to the given upper and lower limit at the given time
     *
     * @param time the time for the new mapping
     * @param upperLimit the upper limit for the new mapping
     * @param lowerLimit the lower limit for the new mapping
     */
    public void setPowerLimit(
            long time,
            double upperLimit,
            double lowerLimit) {
        this.setPowerLimit(time, new PowerInterval(upperLimit, lowerLimit));
    }

    /**
     * Sets the known power-limit to the given upper limit at the given time
     *
     * @param time the time for the new mapping
     * @param upperLimit the upper limit for the new mapping
     */
    public void setPowerLimit(long time, double upperLimit) {
        this.setPowerLimit(time, new PowerInterval(upperLimit));
    }

    /**
     * Sets the time interval during which the power-limits are known.
     *
     * @param start the (inclusive) start of the interval
     * @param end the (inclusive) end of the interval
     */
    public void setKnownPowerLimitInterval(long start, long end) {
        this.limitUnknownBefore = start;
        this.limitUnknownAtAndAfter = end;
    }

    /**
     * Removes redundant entries.
     */
    public void compress() {
        if (this.isCompressed) {
            return;
        }

        ObjectIterator<Long2ObjectMap.Entry<PowerInterval>> i = Long2ObjectMaps.fastIterator(this.powerLimits);
        PowerInterval last = null;

        while (i.hasNext()) {
            Long2ObjectMap.Entry<PowerInterval> e = i.next();
            if (e.getValue().equals(last)) {
                i.remove();
            } else {
                last = e.getValue();
            }
        }

        this.isCompressed = true;
    }

    /**
     * Returns the current limit at the given time t.<br>
     * If there is no limit available: return UNKNOWN_LIMIT (+/- 43kW)
     *
     * @param t the time
     *
     * @return the power-limit at time t
     */
    public PowerInterval getPowerLimitInterval(long t) {
        return this.powerLimits.floorEntry(t).getValue();
    }

    /**
     * Returns the current upper limit at the given time t.<br>
     * If there is no limit available: return UNKNOWN_LIMIT (+ 43kW)
     *
     * @param t the time
     *
     * @return the upper power-limit at time t
     */
    public double getPowerUpperLimit(long t) {

        Long2ObjectMap.Entry<PowerInterval> entry = this.powerLimits.floorEntry(t);

        if (entry != null) {
            return entry.getValue().getPowerUpperLimit();
        } else {
            return this.UNKNOWN_LIMIT.getPowerUpperLimit();
        }
    }

    /**
     * Returns the current lower limit at the given time t.<br>
     * If there is no limit available: return UNKNOWN_LIMIT (- 43kW)
     *
     * @param t the time
     *
     * @return the upper power-limit at time t
     */
    public double getPowerLowerLimit(long t) {

        Long2ObjectMap.Entry<PowerInterval> entry = this.powerLimits.floorEntry(t);

        if (entry != null) {
            return entry.getValue().getPowerLowerLimit();
        } else {
            return this.UNKNOWN_LIMIT.getPowerLowerLimit();
        }
    }

    /**
     * Returns the next time the power-limit changes after the given time t.
     *
     * @param t the time
     *
     * @return the next time the power-limit changes after t or null if there is no next power-limit change
     */
    public Long getNextPowerLimitChange(long t) {
        if (t >= this.limitUnknownAtAndAfter) {
            return null;
        }

        this.compress();

        long key = this.powerLimits.higherKey(t);

        return key == Long2ObjectTreeMap.INVALID_KEY ? this.limitUnknownAtAndAfter : key;
    }

    /**
     * Returns an iterator for the power-limit mappings in the given time interval.
     *
     * @param from the (exclusive) start of the interval
     * @param to the (inclusive) end of the interval
     *
     * @return an iterator for the power-limit mappings in the time interval
     */
    public ObjectIterator<Long2ObjectMap.Entry<PowerInterval>> getIteratorForSubMap(long from, long to) {
        return Long2ObjectMaps.fastIterator(this.powerLimits.subMap(from + 1, to));
    }

    /**
     * Returns the mapping associated with the greatest time less than or equal to the given time, or null if there
     * is no such mapping.
     *
     * @param t the time
     *
     * @return the mapping associated with the greatest time less than or equal to the given time, or null if there is
     * no such mapping
     */
    public Long2ObjectMap.Entry<PowerInterval> getFloorEntry(long t) {
        return this.powerLimits.floorEntry(t);
    }

    /**
     * Returns the time-point after which no power-limit is known.
     *
     * @return the point in time after which no power-limit is known
     */
    public long getPowerLimitUnknownAtAndAfter() {
        return this.limitUnknownAtAndAfter;
    }

    /**
     * Returns the time-point before which no power-limit is known.
     *
     * @return the point in time before which no power-limit is known
     */
    public long getPowerLimitUnknownBefore() {
        return this.limitUnknownBefore;
    }

    /**
     * Returns all mappings of time to power-limits.
     *
     * @return all mappings of time to power-limits
     */
    public Long2ObjectTreeMap<PowerInterval> getLimits() {
        return this.powerLimits;
    }

    @Override
    public PowerLimitSignal clone() {
        PowerLimitSignal clone = new PowerLimitSignal();

        clone.isCompressed = this.isCompressed;
        clone.limitUnknownBefore = this.limitUnknownBefore;
        clone.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;

        Long2ObjectMaps.fastForEach(this.powerLimits, e -> clone.powerLimits.put(e.getLongKey(), e.getValue().clone()));

        return clone;
    }

    private Long2ObjectMap.Entry<PowerInterval> getNext(
            ObjectIterator<Long2ObjectMap.Entry<PowerInterval>> it,
            long duration) {
        if (it.hasNext()) {
            Long2ObjectMap.Entry<PowerInterval> e = it.next();
            if (e.getLongKey() < duration)
                return e;
            else
                return null;
        } else
            return null;
    }

    private void extendAndOverride(ObjectIterator<Long2ObjectMap.Entry<PowerInterval>> iSet2, long toExtendUnknownBefore,
                                   long toExtendUnknownAtAndAfter) {
        ObjectIterator<Long2ObjectMap.Entry<PowerInterval>> iSet1 = Long2ObjectMaps.fastIterator(this.powerLimits);

        Long2ObjectMap.Entry<PowerInterval> entry1 = this.getNext(iSet1, this.limitUnknownAtAndAfter);
        Long2ObjectMap.Entry<PowerInterval> entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
        Long2ObjectTreeMap<PowerInterval> newLimits = new Long2ObjectTreeMap<>();
        long oldUnknownAfter = this.limitUnknownAtAndAfter;
        long oldUnknownBefore = this.limitUnknownBefore;

        this.limitUnknownBefore = Math.min(this.limitUnknownBefore, toExtendUnknownBefore);
        this.limitUnknownAtAndAfter = Math.max(this.limitUnknownAtAndAfter, toExtendUnknownAtAndAfter);
        this.isCompressed = false;

        while (entry1 != null && entry2 != null) {
            if (entry1.getLongKey() < toExtendUnknownBefore) {
                newLimits.put(entry1.getLongKey(), entry1.getValue());
                entry1 = this.getNext(iSet1, oldUnknownAfter);
            } else {
                newLimits.put(entry2.getLongKey(), entry2.getValue());
                entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
            }
        }

        while (entry1 != null) { // 1st profile still has data points
            if (entry1.getLongKey() > toExtendUnknownAtAndAfter) {
                newLimits.put(entry1.getLongKey(), entry1.getValue());
            }
            entry1 = this.getNext(iSet1, oldUnknownAfter);
        }

        while (entry2 != null) { // 2nd profile still has data points
            if (entry2.getLongKey() > toExtendUnknownAtAndAfter) {
                newLimits.put(entry2.getLongKey(), entry2.getValue());
            }
            entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
        }

        //power-limit signals dont overlap (|----2----|     |----1----|), so we have an uncertain period
        if (toExtendUnknownAtAndAfter < oldUnknownBefore) {
            newLimits.put(toExtendUnknownAtAndAfter, this.UNKNOWN_LIMIT);
        }

        //power-limit signals dont overlap (|----1----|     |----2----|), so we have an uncertain period
        if (oldUnknownAfter < toExtendUnknownBefore) {
            newLimits.put(oldUnknownAfter, this.UNKNOWN_LIMIT);
        }

        this.powerLimits = newLimits;

        this.compress();
    }

    /**
     * Overrides (and extends) the mappings of this power-limit signal with all the mappings of the given power-limit signal.
     *
     * @param toExtend the new and extended power-limits
     */
    public void extendAndOverride(PowerLimitSignal toExtend) {
        this.extendAndOverride(Long2ObjectMaps.fastIterator(toExtend.powerLimits),
                toExtend.limitUnknownBefore, toExtend.limitUnknownAtAndAfter);
    }



    /**
     * Clones this power-limit signal after the given time and returns the result.
     *
     * @param timestamp the time
     *
     * @return a clone of this power-limit signal after the timestamp
     */
    public PowerLimitSignal cloneAfter(long timestamp) {

        PowerLimitSignal newLimitSignal = new PowerLimitSignal();

        PowerInterval startCorrection = this.getPowerLimitInterval(timestamp);

        if (startCorrection != null)
            newLimitSignal.powerLimits.put(timestamp, startCorrection.clone());

        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        newLimitSignal.powerLimits.putAll(this.powerLimits.tailMap(timestamp + 1));
        newLimitSignal.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;
        newLimitSignal.limitUnknownBefore = timestamp;

        return newLimitSignal;
    }

    /**
     * Clones this power-limit signal before the given time and returns the result.
     *
     * @param timestamp the time
     *
     * @return a clone of this power-limit signal before the timestamp
     */
    public PowerLimitSignal cloneBefore(long timestamp) {

        PowerLimitSignal newLimitSignal = new PowerLimitSignal();

        newLimitSignal.powerLimits.putAll(this.powerLimits.headMap(timestamp));

        newLimitSignal.limitUnknownAtAndAfter = Math.min(this.limitUnknownAtAndAfter, timestamp);
        newLimitSignal.limitUnknownBefore = this.limitUnknownBefore;

        return newLimitSignal;
    }

    @Override
    public String toString() {
        return this.powerLimits.toString();
    }
}
