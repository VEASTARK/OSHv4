package osh.datatypes.power;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import osh.datatypes.commodity.Commodity;
import osh.utils.dataStructures.fastutil.Long2IntTreeMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SparseLoadProfile extends LoadProfile<Commodity> {

    public SparseLoadProfile() {
        super(Commodity.class);
    }

    public SequentialSparseLoadProfileIterator initSequentialAverageLoad(long from, long till) {

        EnumMap<Commodity, Long2IntMap.Entry> currentEntry = new EnumMap<>(this.enumType);
        EnumMap<Commodity, Long2IntMap.Entry> nextEntry = new EnumMap<>(this.enumType);
        EnumMap<Commodity, ObjectIterator<Long2IntMap.Entry>> iterators = new EnumMap<>(this.enumType);

        for (Commodity c : this.getEnumValues()) {

            Long2IntTreeMap loadProfile = this.getLoadProfile(c);

            if (from < this.endingTimeOfProfile) {
                currentEntry.put(c, loadProfile.floorEntry(from));

                ObjectIterator<Long2IntMap.Entry> it = Long2IntMaps.fastIterator(loadProfile.subMap(from, till));
                iterators.put(c, it);

                nextEntry.put(c, (it.hasNext() ? it.next() : null));
            } else {
                currentEntry.put(c, null);
                iterators.put(c, null);
                nextEntry.put(c, null);
            }
        }

        return new SequentialSparseLoadProfileIterator(currentEntry, nextEntry, iterators,
                this.getEndingTimeOfProfile());
    }

    public SequentialSparseLoadProfileIterator initSequentialAverageLoad(long from) {

        return this.initSequentialAverageLoad(from, Long.MAX_VALUE);
    }

    /************************
    /************************
     * static methods
     ************************/

    public static SparseLoadProfile[][] getCompressedLoadProfilesByTimeSlot(SparseLoadProfile[][] original, final int time) {
        SparseLoadProfile[][] compressed = null;
        if (original != null) {
            compressed = new SparseLoadProfile[original.length][];
            for (int i = 0; i < original.length; i++) {
                compressed[i] = SparseLoadProfile.getCompressedLoadProfilesByTimeSlot(original[i], time);
            }
        }
        return compressed;
    }

    public static SparseLoadProfile[] getCompressedLoadProfilesByTimeSlot(SparseLoadProfile[] original, final int time) {
        SparseLoadProfile[] compressed = null;
        if (original != null) {
            compressed = new SparseLoadProfile[original.length];
            for (int i = 0; i < original.length; i++) {
                compressed[i] = original[i].getCompressedProfileByTimeSlot(time);
            }
        }
        return compressed;
    }

    public static SparseLoadProfile[][] getCompressedLoadProfilesByDiscontinuities(SparseLoadProfile[][] original, final int powerEps) {
        SparseLoadProfile[][] compressed = null;
        if (original != null) {
            compressed = new SparseLoadProfile[original.length][];
            for (int i = 0; i < original.length; i++) {
                compressed[i] = SparseLoadProfile.getCompressedLoadProfilesByDiscontinuities(original[i], powerEps);
            }
        }
        return compressed;
    }

    public static SparseLoadProfile[] getCompressedLoadProfilesByDiscontinuities(SparseLoadProfile[] original, final int powerEps) {
        SparseLoadProfile[] compressed = null;
        if (original != null) {
            compressed = new SparseLoadProfile[original.length];
            for (int i = 0; i < original.length; i++) {
                compressed[i] = original[i].getCompressedProfileByDiscontinuities(powerEps);
            }
        }
        return compressed;
    }

    public static SparseLoadProfile[][] getCompressedProfile(
            LoadProfileCompressionTypes ct,
            SparseLoadProfile[][] original,
            final int powerEps,
            final int time) {
        if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
            return getCompressedLoadProfilesByDiscontinuities(original, powerEps);
        } else if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
            return getCompressedLoadProfilesByTimeSlot(original, time);
        } else {
            return null;
        }
    }

    /**
     * Convert and do NOT compress
     *
     * @param powerProfile
     * @return
     */
    public static SparseLoadProfile convertToSparseProfile(
            EnumMap<Commodity, ArrayList<PowerProfileTick>> powerProfile,
            LoadProfileCompressionTypes ct,
            final int timeSlotDuration) {
        // conversion without compression
        return convertToSparseProfile(
                powerProfile,
                ct,
                -1,
                timeSlotDuration);
    }

    public static SparseLoadProfile[] getCompressedProfile(
            LoadProfileCompressionTypes ct,
            SparseLoadProfile[] original, final int powerEps, final int time) {
        if (ct == LoadProfileCompressionTypes.DISCONTINUITIES) {
            return getCompressedLoadProfilesByDiscontinuities(original, powerEps);
        }
        if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
            return getCompressedLoadProfilesByTimeSlot(original, time);
        } else {
            return null;
        }
    }

    /**
     * Convert and compress
     *
     * @param powerProfiles
     * @param powerEps      max delta value for new point
     * @return
     */
    public static SparseLoadProfile convertToSparseProfile(
            EnumMap<Commodity, ArrayList<PowerProfileTick>> powerProfiles,
            LoadProfileCompressionTypes ct,
            int powerEps,
            final int timeSlotDuration) {
        SparseLoadProfile profile = new SparseLoadProfile();
        return convertToSparseProfile(powerProfiles, ct, powerEps, timeSlotDuration, profile);
    }

    protected static SparseLoadProfile convertToSparseProfile(
            EnumMap<Commodity, ArrayList<PowerProfileTick>> powerProfiles,
            LoadProfileCompressionTypes ct,
            int powerEps,
            final int timeSlotDuration,
            SparseLoadProfile profile) {

        SparseLoadProfile compressedProfile = profile;
        int eps = powerEps;
        if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
            eps = -1;
        }

        for (Entry<Commodity, ArrayList<PowerProfileTick>> e : powerProfiles.entrySet()) {

            int powerLastAvg = 0;
            long powerAvgSum = 0;
            long powerAvgCnt = 0;

            long i = 0;
            long prevI = 0;

            List<PowerProfileTick> powerProfile = e.getValue();
            Commodity currentCommodity = e.getKey();

            for (PowerProfileTick p : powerProfile) {
                if (i == 0) {
                    powerAvgSum += p.load;
                    powerAvgCnt++;
                    powerLastAvg = Math.round(powerAvgSum / powerAvgCnt);

                } else if (i == (powerProfile.size() - 1)) {
                    compressedProfile.setLoad(currentCommodity, prevI, powerLastAvg);

                    compressedProfile.setLoad(currentCommodity, i, p.load);
                } else {
                    if (Math.abs(powerLastAvg - p.load) > eps) {

                        compressedProfile.setLoad(currentCommodity, prevI, powerLastAvg);
                        powerLastAvg = p.load;
                        powerAvgSum = p.load;
                        powerAvgCnt = 1;

                        prevI = i;
                    } else {
                        powerAvgSum += p.load;
                        powerAvgCnt++;
                        powerLastAvg = Math.round(powerAvgSum / powerAvgCnt);
                    }
                }
                i++;
            }

        }

        if (ct == LoadProfileCompressionTypes.TIMESLOTS) {
            compressedProfile = compressedProfile.getCompressedProfile(ct, eps, timeSlotDuration);
        }

        return compressedProfile;
    }

    @Override
    public SparseLoadProfile merge(
            LoadProfile<Commodity> other,
            long offset) {
        SparseLoadProfile merged = new SparseLoadProfile();
        this.merge(other, offset, merged);

        return merged;
    }

    @Override
    public SparseLoadProfile getProfileWithoutDuplicateValues() {
        SparseLoadProfile compressed = new SparseLoadProfile();
        this.getProfileWithoutDuplicateValues(compressed);
        return compressed;
    }

    @Override
    public SparseLoadProfile getCompressedProfile(LoadProfileCompressionTypes ct, int powerEps, int time) {
        SparseLoadProfile compressed = new SparseLoadProfile();
        this.getCompressedProfile(ct, powerEps, time, compressed);
        return compressed;
    }

    @Override
    public SparseLoadProfile getCompressedProfileByDiscontinuities(double powerEps) {
        SparseLoadProfile compressed = new SparseLoadProfile();
        this.getCompressedProfileByDiscontinuities(powerEps, compressed);
        return compressed;
    }

    @Override
    public SparseLoadProfile getCompressedProfileByTimeSlot(final int time) {
        SparseLoadProfile compressed = new SparseLoadProfile();
        this.getCompressedProfileByTimeSlot(time, compressed);
        return compressed;
    }

    @Override
    public SparseLoadProfile clone() {
        SparseLoadProfile clone = new SparseLoadProfile();
        this.clone(clone);
        return clone;
    }

    @Override
    public SparseLoadProfile cloneAfter(long timestamp) {
        SparseLoadProfile clone = new SparseLoadProfile();
        this.cloneAfter(timestamp, clone);
        return clone;
    }

    @Override
    public SparseLoadProfile cloneBefore(long timestamp) {
        SparseLoadProfile clone = new SparseLoadProfile();
        this.cloneBefore(timestamp, clone);
        return clone;
    }

    @Override
    public SparseLoadProfile cloneWithOffset(long offset) {
        SparseLoadProfile clone = new SparseLoadProfile();
        this.cloneWithOffset(offset, clone);
        return clone;
    }

    @Override
    public EnumMap<Commodity, Map<Long, Integer>> convertToSimpleMap() {
        EnumMap<Commodity, Map<Long, Integer>> map = new EnumMap<>(Commodity.class);
        this.convertToSimpleMap(map, this.getEndingTimeOfProfile());
        return map;
    }

    @Override
    public EnumMap<Commodity, Map<Long, Integer>> convertToSimpleMap(long maxTime) {
        EnumMap<Commodity, Map<Long, Integer>> map = new EnumMap<>(Commodity.class);
        this.convertToSimpleMap(map, maxTime);
        return map;
    }
}
