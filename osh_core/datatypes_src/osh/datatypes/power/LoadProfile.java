package osh.datatypes.power;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import osh.utils.dataStructures.fastutil.Long2IntTreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 */
@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class LoadProfile<C extends Enum<C>> implements ILoadProfile<C> {

    protected final Class<C> enumType;
    protected EnumMap<C, Long2IntTreeMap> commodities;
    protected long endingTimeOfProfile;

    public LoadProfile(Class<C> enumType) {
        this.commodities = new EnumMap<>(enumType);
        this.enumType = enumType;
        for (C commodity : this.getEnumValues()) {
            this.commodities.put(commodity, new Long2IntTreeMap());
        }

        this.endingTimeOfProfile = 0;
    }

    protected Long2IntTreeMap getLoadProfile(C c) {
        return this.commodities.computeIfAbsent(c, k -> new Long2IntTreeMap());
    }

    public void setLoad(C c, long t, int power) {
        Long2IntTreeMap loadProfile = this.getLoadProfile(c);
        loadProfile.put(t, power);
        if (this.endingTimeOfProfile < t + 1) {
            this.endingTimeOfProfile = t + 1;
        }
    }

    protected Long2IntMap.Entry getNext(
            ObjectIterator<Long2IntMap.Entry> it,
            long duration) {
        if (it.hasNext()) {
            Long2IntMap.Entry e = it.next();
            if (e.getLongKey() < duration)
                return e;
            else
                return null;
        } else
            return null;
    }

    public ObjectIterator<Long2IntMap.Entry> getIteratorForSubMap(C c, long from, long to) {
        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        return Long2IntMaps.fastIterator(this.commodities.get(c).subMap(from + 1, to));
    }

    public ObjectIterator<Long2IntMap.Entry> getIteratorForType(C c) {
        return Long2IntMaps.fastIterator(this.commodities.get(c));
    }

    public Long2IntMap.Entry getFloorEntry(C c, long t) {
        return this.commodities.get(c).floorEntry(t);
    }

    @Override
    public ILoadProfile<C> merge(ILoadProfile<C> other, long offset) {
        if (other instanceof LoadProfile) {
            ILoadProfile<C> merged = this.merge((LoadProfile<C>) other, offset);
            if (this.endingTimeOfProfile != 0 || other.getEndingTimeOfProfile() != 0) {
                if (merged.getEndingTimeOfProfile() == 0) {
                    // still possible because of offset, but not normal
                    System.out.println("ERROR: merged.getEndingTimeOfProfile() == 0, although one profile not 0");
                }
            }
            return merged;
        } else {
            if (other == null) {
                throw new NullPointerException("other is null");
            }
            throw new UnsupportedOperationException();
        }
    }

    public abstract LoadProfile<C> merge(
            LoadProfile<C> other,
            long offset);

    protected void merge(
            LoadProfile<C> other,
            long offset,
            LoadProfile<C> merged) {

        merged.endingTimeOfProfile = Math.max(this.endingTimeOfProfile, other.endingTimeOfProfile + offset);

        for (C commodity : this.getEnumValues()) {
            ObjectIterator<Long2IntMap.Entry> iSet1 = Long2IntMaps.fastIterator(this.getLoadProfile(commodity));
            ObjectIterator<Long2IntMap.Entry> iSet2 = Long2IntMaps.fastIterator(other.getLoadProfile(commodity));

            Long2IntMap.Entry entry1 = this.getNext(iSet1, this.endingTimeOfProfile);
            Long2IntMap.Entry entry2 = this.getNext(iSet2, other.endingTimeOfProfile);

            int activeValue1 = 0;
            int activeValue2 = 0;

            while (entry1 != null && entry2 != null) {

                if (entry1.getLongKey() < entry2.getLongKey() + offset) {
                    merged.setLoad(commodity, entry1.getLongKey(),
                            entry1.getIntValue() + activeValue2);

                    activeValue1 = entry1.getIntValue();

                    entry1 = this.getNext(iSet1, this.endingTimeOfProfile);
                } else if (entry1.getLongKey() > entry2.getLongKey() + offset) {
                    merged.setLoad(commodity, entry2.getLongKey() + offset,
                            activeValue1 + entry2.getIntValue());

                    activeValue2 = entry2.getIntValue();

                    entry2 = this.getNext(iSet2, other.endingTimeOfProfile);
                } else /* (entry1.getKey() == entry2.getKey() + offset) */ {
                    merged.setLoad(commodity, entry2.getLongKey() + offset,
                            entry1.getIntValue() + entry2.getIntValue());

                    activeValue1 = entry1.getIntValue();
                    activeValue2 = entry2.getIntValue();

                    entry1 = this.getNext(iSet1, this.endingTimeOfProfile);
                    entry2 = this.getNext(iSet2, other.endingTimeOfProfile);
                }
            }

            while (entry1 != null) { // 1st profile still has data points
                if (entry1.getLongKey() < other.endingTimeOfProfile + offset) {
                    merged.setLoad(commodity, entry1.getLongKey(),
                            entry1.getIntValue() + activeValue2);
                    activeValue1 = entry1.getIntValue();
                } else { // 2nd profile has ended
                    if (activeValue2 != 0) {
                        merged.setLoad(commodity, other.endingTimeOfProfile + offset,
                                activeValue1);
                        activeValue2 = 0;
                    }
                    merged.setLoad(commodity, entry1.getLongKey(),
                            entry1.getIntValue() + activeValue2);
                }

                entry1 = this.getNext(iSet1, this.endingTimeOfProfile);
            }
            while (entry2 != null) {
                if (entry2.getLongKey() + offset < this.endingTimeOfProfile) {
                    merged.setLoad(commodity, entry2.getLongKey() + offset,
                            entry2.getIntValue() + activeValue1);
                    activeValue2 = entry2.getIntValue();
                } else {
                    if (activeValue1 != 0) {
                        merged.setLoad(commodity, this.endingTimeOfProfile, activeValue2);
                        activeValue1 = 0;
                    }
                    merged.setLoad(commodity, entry2.getLongKey() + offset,
                            entry2.getIntValue() + activeValue1);
                }

                entry2 = this.getNext(iSet2, other.endingTimeOfProfile);
            }

            // handling the end of profiles
            if (activeValue1 != 0 && activeValue2 != 0) {
                if (this.endingTimeOfProfile > other.endingTimeOfProfile + offset) {
                    merged.setLoad(commodity, other.endingTimeOfProfile + offset,
                            activeValue1);
                } else if (this.endingTimeOfProfile < other.endingTimeOfProfile + offset) {
                    merged.setLoad(commodity, this.endingTimeOfProfile, activeValue2);
                } else { /* == */
                    assert (this.endingTimeOfProfile == merged.endingTimeOfProfile);
                }
            } else if (activeValue2 != 0) {
                merged.setLoad(commodity, other.endingTimeOfProfile + offset, activeValue1);
            } else if (activeValue1 != 0) {
                merged.setLoad(commodity, this.endingTimeOfProfile, activeValue2);
            }
        }
    }

    /**
     * EndingTimeOfProfile is defined as the point in time where
     * the profile stops having a value other than 0, NOT the length of the profile.
     * See {@link ILoadProfile#getEndingTimeOfProfile()}
     */
    @Override
    public long getEndingTimeOfProfile() {
        return this.endingTimeOfProfile;
    }

    /**
     * EndingTime is defined as the point in time (timestamp) where
     * the profile stops having a value other than 0, NOT the length of the profile.
     * See {@link ILoadProfile#getEndingTimeOfProfile()}
     */
    public void setEndingTimeOfProfile(long endingTimeOfProfile) {
        this.endingTimeOfProfile = endingTimeOfProfile;
    }

    @Override
    public int getLoadAt(C c, long t) {
        if (t >= this.endingTimeOfProfile) {
            return 0;
        }

        Long2IntMap.Entry entry = this.getLoadProfile(c).floorEntry(t);
        return (entry == null) ? 0 : entry.getIntValue();
    }

    public int getAverageLoadFromTill(C commodity, long start, long end) {
        if (start >= this.endingTimeOfProfile) {
            return 0;
        }

        Long2IntTreeMap loadProfile = this.getLoadProfile(commodity);

        double avg = 0.0;
        long currentTime = start;
        long maxTime = Math.min(end, this.endingTimeOfProfile);

        //checking if profile has values
        Long2IntMap.Entry currentEntry = loadProfile.floorEntry(start);
        if (currentEntry == null)
            return 0;

        long higherKey = loadProfile.higherKey(currentTime);

        //no other values for the requested time period
        if (higherKey == Long2IntTreeMap.INVALID_KEY || higherKey >= maxTime) {
            avg = currentEntry.getIntValue() * ((double) (maxTime - currentTime) / (end - start));
            return (int) Math.round(avg);
        }

        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        ObjectIterator<Long2IntMap.Entry> entryIterator =
                Long2IntMaps.fastIterator(loadProfile.subMap(start + 1, maxTime));
        Long2IntMap.Entry nextEntry = entryIterator.next();
        while (nextEntry.getLongKey() == currentEntry.getLongKey()) nextEntry = entryIterator.next();

        while (nextEntry != null) {
            long nextChange = nextEntry.getLongKey();

            avg += ((double) currentEntry.getIntValue()) * ((double) (nextChange - currentTime) / (double) (end - start));
            currentTime = nextChange;
            currentEntry = nextEntry;

            if (entryIterator.hasNext()) {
                nextEntry = entryIterator.next();
            } else {
                nextEntry = null;
            }
        }

        if (currentTime < maxTime) {
            avg += currentEntry.getIntValue() * ((double) (maxTime - currentTime) / (end - start));
        }

        return (int) Math.round(avg);
    }

    @Override
    public Long getNextLoadChange(C commodity, long t) {
        return this.getLoadProfile(commodity).higherKey(t);
    }

    /**
     * cuts off all ticks with a negative time and inserts a tick at time 0
     * if necessary. This function changes the current object.
     *
     * @author tisu
     */
    @Override
    public void cutOffNegativeTimeValues() {
        for (Long2IntTreeMap load : this.commodities.values()) {
            int lastVal = 0;
            boolean modified = false;
            while(load.firstLongKey() < 0) {
                lastVal = load.get(load.firstLongKey());
                load.remove(load.firstLongKey());
                modified = true;
            }
            if (modified) {
                load.put(0, lastVal);
            }
        }
    }

    public void multiplyLoadsWithFactor(double factor) {
        for (Long2IntTreeMap map : this.commodities.values()) {
            Long2IntMaps.fastForEach(map, e -> e.setValue((int) Math.round(e.getIntValue() * factor)));
        }
    }

    public void multiplyLoadsWithFactor(double factor, C c) {
        Long2IntMaps.fastForEach(this.getLoadProfile(c), e -> e.setValue((int) Math.round(e.getIntValue() * factor)));
    }

    public abstract LoadProfile<C> getProfileWithoutDuplicateValues();


    // ###########
    // COMPRESSION
    // ###########

    // general
    protected void getProfileWithoutDuplicateValues(LoadProfile<C> compress) {
        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            Long2IntTreeMap otherMap = compress.getLoadProfile(c);
            ObjectIterator<Long2IntMap.Entry> it = Long2IntMaps.fastIterator(map);

            if (!it.hasNext())
                continue;

            Long2IntMap.Entry lastValue = it.next();
            otherMap.put(lastValue.getLongKey(), lastValue.getIntValue());

            while (it.hasNext()) {
                Long2IntMap.Entry e = it.next();

                if (lastValue.getIntValue() != e.getIntValue()) {
                    lastValue = e;
                    otherMap.put(lastValue.getLongKey(), lastValue.getIntValue());
                }
            }
        }
        compress.endingTimeOfProfile = this.endingTimeOfProfile;
    }

    public abstract LoadProfile<C> getCompressedProfile(LoadProfileCompressionTypes ct, final int powerEps, final int time);

    // general
    protected void getCompressedProfile(
            LoadProfileCompressionTypes ct, final int powerEps, final int time, LoadProfile<C> compress) {
        if (ct == null || time <= 0 || powerEps <= 0) {
            compress = this.clone();
            System.out.println("[ERROR][AbstractLoadProfile]: compression-type or -value is invalid (null or <= 0) " +
                    "returning clone of original profile");
        } else if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
            this.getCompressedProfileByDiscontinuities(powerEps, compress);
        } else if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
            this.getCompressedProfileByTimeSlot(time, compress);
        }
    }

    public abstract LoadProfile<C> getCompressedProfileByDiscontinuities(final double powerEps);

    protected void getCompressedProfileByDiscontinuities(
            final double powerEps, LoadProfile<C> compressed) {

        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            Long2IntTreeMap compressedMap = compressed.getLoadProfile(c);

            double lastValueSaved;
            long lastValueSavedKey;

            double momentaryAvg;
            double momentaryAvgMax;
            double momentaryAvgMin;

            long lastLookedAtKey;
            int lastLookedAtValue;

            long counter = 0;

            //store first value
            if (!map.isEmpty()) {
                lastValueSavedKey = map.firstLongKey();
                lastLookedAtValue = map.get(lastValueSavedKey);

                lastValueSaved = lastLookedAtValue;
                lastLookedAtKey = lastValueSavedKey;

                momentaryAvg = lastValueSaved;
                momentaryAvgMax = lastValueSaved;
                momentaryAvgMin = lastValueSaved;

                compressedMap.put(lastValueSavedKey, (int) lastValueSaved);

                //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
                for (ObjectIterator<Long2IntMap.Entry> it =
                     Long2IntMaps.fastIterator(map.tailMap(map.firstLongKey() + 1)); it.hasNext(); ) {
                    Long2IntMap.Entry e = it.next();
                    // if last sample -> store sample
                    if (!it.hasNext()) {
                        compressed.setLoad(c, e.getLongKey(), e.getIntValue());

                        if (lastLookedAtKey != Long2IntTreeMap.INVALID_KEY) {
                            // write previous average value...
                            compressed.setLoad(c, lastValueSavedKey, (int) Math.round(momentaryAvg));
                        }
                    }
                    //if difference of avg to min/max/lastValue/nowValue > powerEps --> store new sample
                    else if (Math.abs(momentaryAvg - momentaryAvgMax) > powerEps
                            || Math.abs(momentaryAvg - momentaryAvgMin) > powerEps
                            || Math.abs(momentaryAvg - lastValueSaved) > powerEps
                            || Math.abs(momentaryAvg - e.getIntValue()) > powerEps) {

                        long diffToLastKey = e.getLongKey() - lastLookedAtKey;
                        momentaryAvg = (lastLookedAtValue * diffToLastKey + momentaryAvg * counter) / (diffToLastKey + counter);

                        compressed.setLoad(c, lastValueSavedKey, (int) Math.round(momentaryAvg));

                        lastValueSavedKey = e.getLongKey();
                        lastValueSaved = e.getIntValue();

                        lastLookedAtKey = e.getLongKey();
                        lastLookedAtValue = e.getIntValue();

                        momentaryAvg = lastValueSaved;
                        momentaryAvgMax = lastValueSaved;
                        momentaryAvgMin = lastValueSaved;

                        counter = 0;
                    }
                    // difference is to small, update avg/min/max etc.
                    else {
                        long diffToLastKey = e.getLongKey() - lastLookedAtKey;
                        momentaryAvg = (lastLookedAtValue * diffToLastKey + momentaryAvg * counter) / (diffToLastKey + counter);

                        lastLookedAtKey = e.getLongKey();
                        lastLookedAtValue = e.getIntValue();

                        if (e.getIntValue() > momentaryAvgMax) {
                            momentaryAvgMax = e.getIntValue();
                        } else if (e.getIntValue() < momentaryAvgMin) {
                            momentaryAvgMin = e.getIntValue();
                        }

                        counter += diffToLastKey;
                    }
                }
            }
        }
        compressed.endingTimeOfProfile = this.endingTimeOfProfile;
    }

    // ByDiscontinuities

    public abstract LoadProfile<C> getCompressedProfileByTimeSlot(final int time);

    // by TimeSlot
    protected void getCompressedProfileByTimeSlot(final int time, LoadProfile<C> compressed) {

        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            Long2IntTreeMap compressedMap = compressed.getLoadProfile(c);

            double lastAvg;
            long lastKey;
            int lastValue;

            //store first value
            if (!map.isEmpty()) {
                lastKey = map.firstLongKey();
                lastValue = map.get(lastKey);
                lastAvg = lastValue;

                compressedMap.put(lastKey, lastValue);

                //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
                for (ObjectIterator<Long2IntMap.Entry> it =
                     Long2IntMaps.fastIterator(map.tailMap(map.firstLongKey() + 1)); it.hasNext(); ) {
                    Long2IntMap.Entry e = it.next();
                    // if last -> set value
                    if (!it.hasNext()) {
                        compressed.setLoad(c, e.getLongKey(), e.getIntValue());

                        if (lastKey != Long2IntTreeMap.INVALID_KEY) {
                            // write last value...
                            compressed.setLoad(c, lastKey, (int) Math.round(lastAvg));
                        }
                    }
                    // difference to previous value is too small -> update avg
                    else if (Math.abs(e.getLongKey() - lastKey) < time) {
                        long diffToLastKey = e.getLongKey() - lastKey;
                        int currentValue = e.getIntValue();
                        lastAvg = (lastAvg * diffToLastKey + currentValue) / (diffToLastKey + 1);
                    }
                    // difference to previous is big enough -> set avg and add new datapoint
                    else {
                        compressed.setLoad(c, lastKey, (int) Math.round(lastAvg));

                        lastKey = e.getLongKey();
                        lastValue = e.getIntValue();
                        lastAvg = lastValue;
                    }
                }
            }
        }
        compressed.endingTimeOfProfile = this.endingTimeOfProfile;
        //if slot-value too low remove duplicate entries to speed up optimization
        if (time < 300) {
            compressed = compressed.getProfileWithoutDuplicateValues();
        }
    }

    /***********************************************************
     * utility functions (cloning, toString, ...)
     **********************************************************/

    public abstract LoadProfile<C> clone();

    protected void clone(LoadProfile<C> newLoadProfile) {
        for (C c : this.getEnumValues()) {
            newLoadProfile.commodities.put(c, new Long2IntTreeMap(this.getLoadProfile(c)));
        }

        newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile;
    }

    /**
     * clones this load profile after the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this profile after the timestamp
     */
    public abstract LoadProfile<C> cloneAfter(long timestamp);

    protected void cloneAfter(long timestamp, LoadProfile<C> newLoadProfile) {
        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            newLoadProfile.commodities.put(c, new Long2IntTreeMap(map.tailMap(timestamp)));
            if (!map.isEmpty() && !map.containsKey(timestamp)) {
                Long2IntMap.Entry lastEntry = map.floorEntry(timestamp);
                if (lastEntry != null)
                    newLoadProfile.setLoad(c, timestamp, lastEntry.getIntValue());
            }
        }

        newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile;
    }

    /**
     * clones this load profile before the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this profile before the timestamp
     */
    public abstract LoadProfile<C> cloneBefore(long timestamp);

    protected void cloneBefore(long timestamp, LoadProfile<C> newLoadProfile) {
        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            newLoadProfile.commodities.put(c, new Long2IntTreeMap(map.headMap(timestamp)));
            if (!map.isEmpty() && map.containsKey(timestamp)) {
                newLoadProfile.setLoad(c, timestamp, map.get(timestamp));
            }
        }

        newLoadProfile.endingTimeOfProfile = Math.min(timestamp, this.endingTimeOfProfile);
    }

    public abstract LoadProfile<C> cloneWithOffset(long offset);

    protected void cloneWithOffset(long offset, LoadProfile<C> newLoadProfile) {

        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            Long2IntTreeMap newMap = new Long2IntTreeMap();
            Long2IntMaps.fastForEach(map, e -> newMap.put(e.getLongKey() + offset, e.getIntValue()));
            newLoadProfile.commodities.put(c, newMap);
        }

        newLoadProfile.endingTimeOfProfile = this.endingTimeOfProfile + offset;
    }

    public abstract EnumMap<C, Map<Long, Integer>> convertToSimpleMap();

    public abstract EnumMap<C, Map<Long, Integer>> convertToSimpleMap(long maxTime);

    void convertToSimpleMap(EnumMap<C, Map<Long, Integer>> simpleMap, long maxTime) {
        long endingTime = Math.min(maxTime, this.endingTimeOfProfile);

        for (C c : this.getEnumValues()) {
            Long2IntTreeMap map = this.getLoadProfile(c);
            HashMap<Long, Integer> clone = new HashMap<>(map);
            if (!map.isEmpty()) {
                Long2IntMap.Entry en = map.floorEntry(endingTime);
                clone.put(endingTime, en != null ? en.getIntValue() : 0);
                //just to be sure
                clone.put(endingTime + 1, 0);
            }
            simpleMap.put(c, clone);
        }
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder();

        for (Entry<C, Long2IntTreeMap> es : this.commodities.entrySet()) {
            returnValue.append("Profile for ").append(this.enumType.getSimpleName()).append(" ").append(es.getKey()).append(": ").append(es.getValue().toString());
        }

        return returnValue.toString();
    }

    // toString

    @Override
    public String toStringShort() {
        StringBuilder returnValue = new StringBuilder("[ ");

        for (Entry<C, Long2IntTreeMap> es : this.commodities.entrySet()) {
            Long2IntTreeMap map = es.getValue();
            if (map != null && !map.isEmpty()) {
                returnValue.append(es.getKey()).append(", ");
            }
        }

        return returnValue + "]";
    }

    protected C[] getEnumValues() {
        return this.enumType.getEnumConstants();
    }
}
