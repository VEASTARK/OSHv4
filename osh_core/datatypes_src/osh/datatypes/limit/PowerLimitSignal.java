package osh.datatypes.limit;

import osh.datatypes.power.PowerInterval;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Representation of the Complex Power Limitation Signal for Shot Term Optimization
 *
 * @author Ingo Mauser
 */
public class PowerLimitSignal {

    // means the limit of a 64A three phase supply in complex power with cosPhi = 0.95 (inductive)
    //private final ComplexPower UNKNOWN_LIMIT = new ComplexPower(430000, 2, 0.95, true);
    private final PowerInterval UNKNOWN_LIMIT = new PowerInterval();
    private boolean isCompressed = true;
    private long limitUnknownBefore;
    private long limitUnknownAtAndAfter;
    //private TreeMap<Long, ComplexPower> powerLimits;
    private TreeMap<Long, PowerInterval> powerLimits;
    private final TreeMap<Long, Double> remsPowerLimits;


    /**
     * CONSTRUCTOR
     */
    public PowerLimitSignal() {
        this.powerLimits = new TreeMap<>();
        this.remsPowerLimits = new TreeMap<>();
    }

    public long getLimitUnknownAtAndAfter() {
        return this.limitUnknownAtAndAfter;
    }

    public void setLimitUnknownAtAndAfter(long limitUnknownAtAndAfter) {
        this.limitUnknownAtAndAfter = limitUnknownAtAndAfter;
    }

    public TreeMap<Long, PowerInterval> getPowerLimits() {
        return this.powerLimits;
    }

    public void setPowerLimits(TreeMap<Long, PowerInterval> powerLimits) {
        this.powerLimits = powerLimits;
    }

    /**
     * Sets the interval during which the power limit is known
     */
    public void setKnownPowerLimitInterval(long start, long end) {
        this.limitUnknownBefore = start;
        this.limitUnknownAtAndAfter = end;
    }

    /**
     * Removes redundant entries
     */
    public void compress() {
        if (this.isCompressed)
            return;

        Iterator<Entry<Long, PowerInterval>> i = this.powerLimits.entrySet().iterator();
        PowerInterval last = null;

        while (i.hasNext()) {
            Entry<Long, PowerInterval> e = i.next();
            if (e.getValue().equals(last)) {
                i.remove();
            } else {
                last = e.getValue();
            }
        }

        this.isCompressed = true;
    }

//	public boolean getIsCompressed() {
//		return isCompressed;
//	}


    public void setPowerLimit(long time, PowerInterval limit) {
        if (limit == null) throw new NullPointerException("limit is null");
        this.powerLimits.put(time, limit);
        this.isCompressed = false;
    }

    public void setPowerLimit(
            long time,
            double activePowerUpperLimit,
            double activePowerLowerLimit) {
        PowerInterval limit = new PowerInterval(
                activePowerUpperLimit,
                activePowerLowerLimit);
        this.setPowerLimit(time, limit);
    }

    public void setPowerLimit(long time, double powerUpperLimit) {
        PowerInterval limit = new PowerInterval(powerUpperLimit);
        this.setPowerLimit(time, limit);
    }


    public PowerInterval getPowerLimitClone(long time) {

        Entry<Long, PowerInterval> entry = this.powerLimits.floorEntry(time);

        if (entry != null) {
            return entry.getValue().clone();
        } else {
            return this.UNKNOWN_LIMIT.clone();
        }
    }

    public PowerInterval getPowerLimitInterval(Long time) {
        return this.powerLimits.floorEntry(time).getValue();
    }

    public double getPowerUpperLimit(long time) {

        Entry<Long, PowerInterval> entry = this.powerLimits.floorEntry(time);

        if (entry != null) {
            return entry.getValue().getPowerUpperLimit();
        } else {
            return this.UNKNOWN_LIMIT.getPowerUpperLimit();
        }
    }

    public double getPowerLowerLimit(long time) {

        Entry<Long, PowerInterval> entry = this.powerLimits.floorEntry(time);

        if (entry != null) {
            return entry.getValue().getPowerLowerLimit();
        } else {
            return this.UNKNOWN_LIMIT.getPowerLowerLimit();
        }
    }


    /**
     * @param base       offset time for new power limit signal
     * @param resolution New resolution in seconds
     * @return
     */
    public PowerLimitSignal scalePowerLimitSignal(long base, long resolution) {
        PowerLimitSignal newPS = new PowerLimitSignal();

        for (Entry<Long, PowerInterval> e : this.powerLimits.entrySet()) {
            newPS.setPowerLimit((e.getKey() - base) / resolution, e.getValue());
        }

        newPS.setKnownPowerLimitInterval(
                (this.limitUnknownBefore - base) / resolution,
                (this.limitUnknownAtAndAfter - base) / resolution);

        return newPS;
    }

    /**
     * Returns the time the power limit changes after t
     *
     * @param t time after power limit will change
     * @return null if there is no next power limit change
     */
    public Long getNextPowerLimitChange(long t) {
        if (t >= this.limitUnknownAtAndAfter) {
            return null;
        }

        this.compress();

        Long key = this.powerLimits.higherKey(t);

        /* && t < priceUnknownAfter */
        return Objects.requireNonNullElseGet(key, () -> this.limitUnknownAtAndAfter);
    }

    public Iterator<Entry<Long, PowerInterval>> getIteratorForSubMap(long from, long to) {
        return this.powerLimits.subMap(from, false, to, false).entrySet().iterator();
    }

    public Entry<Long, PowerInterval> getFloorEntry(long t) {
        return this.powerLimits.floorEntry(t);
    }

    @Deprecated
    public Long getNextActivePowerLimitChange(long t) {
        if (t >= this.limitUnknownAtAndAfter) {
            return null;
        }

        this.compress();

        Long nextChange = this.powerLimits.higherKey(t);
        Long prevChange = this.powerLimits.higherKey(t);

        boolean flag;
        long returnValue;

        do {
            /* && t < priceUnknownAfter */
            returnValue = Objects.requireNonNullElseGet(nextChange, () -> this.limitUnknownAtAndAfter);

            if (nextChange == null || prevChange == null) {
                flag = false;
            } else
                flag = this.powerLimits.get(prevChange).getPowerUpperLimit() != this.powerLimits.get(nextChange).getPowerUpperLimit();

        } while (flag);

        return returnValue;
    }

    @Override
    public PowerLimitSignal clone() {
        PowerLimitSignal clone = new PowerLimitSignal();

        clone.isCompressed = this.isCompressed;
        clone.limitUnknownBefore = this.limitUnknownBefore;
        clone.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;

        //deep clone tree map
        for (Entry<Long, PowerInterval> e : this.powerLimits.entrySet()) {
            clone.powerLimits.put(e.getKey(), e.getValue().clone());
        }

        return clone;
    }

    /**
     * returned value is the first time tick which has no limit.
     */
    public long getPowerLimitUnknownAtAndAfter() {
        return this.limitUnknownAtAndAfter;
    }

    /**
     * returned value is the first time tick which has a limit.
     */
    public long getPowerLimitUnknownBefore() {
        return this.limitUnknownBefore;
    }

    public TreeMap<Long, PowerInterval> getLimits() {
        return this.powerLimits;
    }

    private <T> Entry<Long, T> getNext(
            Iterator<Entry<Long, T>> it,
            long duration) {
        if (it.hasNext()) {
            Entry<Long, T> e = it.next();
            if (e.getKey() < duration)
                return e;
            else
                return null;
        } else
            return null;
    }

    public void extendAndOverride(PowerLimitSignal toExtend) {

        Iterator<Entry<Long, PowerInterval>> iSet1 = this.powerLimits.entrySet()
                .iterator();
        Iterator<Entry<Long, PowerInterval>> iSet2 = toExtend.powerLimits.entrySet()
                .iterator();

        Entry<Long, PowerInterval> entry1;
        Entry<Long, PowerInterval> entry2;
        TreeMap<Long, PowerInterval> newLimits = new TreeMap<>();
        long oldUnknownAfter = this.limitUnknownAtAndAfter;
        long oldUnknownBefore = this.limitUnknownBefore;

        this.limitUnknownBefore = Math.min(this.limitUnknownBefore, toExtend.limitUnknownBefore);
        this.limitUnknownAtAndAfter = Math.max(this.limitUnknownAtAndAfter, toExtend.limitUnknownAtAndAfter);
        this.isCompressed = false;

        entry1 = this.getNext(iSet1, oldUnknownAfter);
        entry2 = this.getNext(iSet2, toExtend.limitUnknownAtAndAfter);

        while (entry1 != null && entry2 != null) {

            if (entry1.getKey() < toExtend.limitUnknownBefore) {
                newLimits.put(entry1.getKey(), entry1.getValue());
                entry1 = this.getNext(iSet1, oldUnknownAfter);
            } else {
                newLimits.put(entry2.getKey(), entry2.getValue());
                entry2 = this.getNext(iSet2, toExtend.limitUnknownAtAndAfter);
            }
        }

        while (entry1 != null) { // 1st profile still has data points
            if (entry1.getKey() > toExtend.limitUnknownAtAndAfter) {
                newLimits.put(entry1.getKey(), entry1.getValue());
            }
            entry1 = this.getNext(iSet1, oldUnknownAfter);
        }

        while (entry2 != null) { // 2nd profile still has data points
            if (entry2.getKey() > toExtend.limitUnknownAtAndAfter) {
                newLimits.put(entry2.getKey(), entry2.getValue());
            }
            entry2 = this.getNext(iSet2, toExtend.limitUnknownAtAndAfter);
        }

        //price signals dont overlap (|----2----|     |----1----|), so we have an uncertain period
        if (toExtend.limitUnknownAtAndAfter < oldUnknownBefore) {
            newLimits.put(toExtend.limitUnknownAtAndAfter, this.UNKNOWN_LIMIT);
        }

        //price signals dont overlap (|----1----|     |----2----|), so we have an uncertain period
        if (oldUnknownAfter < toExtend.limitUnknownBefore) {
            newLimits.put(oldUnknownAfter, this.UNKNOWN_LIMIT);
        }

        this.powerLimits = newLimits;

        this.compress();
    }

    /**
     * clones this power limit signal after the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this power limit signal after the timestamp
     */
    public PowerLimitSignal cloneAfter(long timestamp) {

        PowerLimitSignal newLimitSignal = new PowerLimitSignal();

        PowerInterval startCorrection = this.getPowerLimitInterval(timestamp);

        if (startCorrection != null)
            newLimitSignal.powerLimits.put(timestamp, this.getPowerLimitInterval(timestamp).clone());

        for (Entry<Long, PowerInterval> en : this.powerLimits.tailMap(timestamp).entrySet()) {
            newLimitSignal.powerLimits.put(en.getKey(), en.getValue().clone());
        }
        newLimitSignal.limitUnknownAtAndAfter = this.limitUnknownAtAndAfter;
        newLimitSignal.limitUnknownBefore = timestamp;

        return newLimitSignal;
    }

    /**
     * clones this power limit signal before the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this power limit signal before the timestamp
     */
    public PowerLimitSignal cloneBefore(long timestamp) {

        PowerLimitSignal newLimitSignal = new PowerLimitSignal();

        for (Entry<Long, PowerInterval> en : this.powerLimits.headMap(timestamp).entrySet()) {
            newLimitSignal.powerLimits.put(en.getKey(), en.getValue().clone());
        }

        newLimitSignal.limitUnknownAtAndAfter = Math.min(this.limitUnknownAtAndAfter, timestamp);
        newLimitSignal.limitUnknownBefore = this.limitUnknownBefore;

        return newLimitSignal;
    }
}
