package osh.datatypes.power;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.utils.dataStructures.fastutil.Long2IntTreeMap;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class AncillaryCommodityLoadProfile extends LoadProfile<AncillaryCommodity> {

    public AncillaryCommodityLoadProfile() {
        super(AncillaryCommodity.class);
    }

    public LongSortedSet getAllLoadChangesFor(AncillaryCommodity ac, long from, long to) {
        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        return this.commodities.get(ac).subMap(from + 1, to).keySet();
    }

    public static AncillaryCommodityLoadProfile convertFromErsatzProfile(ErsatzACLoadProfile ersatzProfile) {
        AncillaryCommodityLoadProfile result = new AncillaryCommodityLoadProfile();

        for (AncillaryCommodity ac : AncillaryCommodity.values()) {
            long[] keys = ersatzProfile.getKeyFor(ac);
            int[] values = Arrays.stream(ersatzProfile.getValueFor(ac)).mapToInt(d -> (int) d).toArray();

            result.commodities.put(ac, new Long2IntTreeMap(keys, values));
        }

        result.setEndingTimeOfProfile(ersatzProfile.getEndingTimeOfProfile());

        return result;
    }

    @Override
    public AncillaryCommodityLoadProfile merge(
            LoadProfile<AncillaryCommodity> other,
            long offset) {
        AncillaryCommodityLoadProfile merged = new AncillaryCommodityLoadProfile();
        this.merge(other, offset, merged);
        return merged;
    }

    @Override
    public AncillaryCommodityLoadProfile getProfileWithoutDuplicateValues() {
        AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
        this.getProfileWithoutDuplicateValues(compressed);
        return compressed;
    }

    @Override
    public AncillaryCommodityLoadProfile getCompressedProfile(LoadProfileCompressionTypes ct, int powerEps, int time) {
        AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
        this.getCompressedProfile(ct, powerEps, time, compressed);
        return compressed;
    }

    @Override
    public AncillaryCommodityLoadProfile getCompressedProfileByDiscontinuities(double powerEps) {
        AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
        this.getCompressedProfileByDiscontinuities(powerEps, compressed);
        return compressed;
    }

    @Override
    public AncillaryCommodityLoadProfile getCompressedProfileByTimeSlot(final int time) {
        AncillaryCommodityLoadProfile compressed = new AncillaryCommodityLoadProfile();
        this.getCompressedProfileByTimeSlot(time, compressed);
        return compressed;
    }

    @Override
    public AncillaryCommodityLoadProfile clone() {
        AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
        this.clone(clone);
        return clone;
    }

    @Override
    public AncillaryCommodityLoadProfile cloneAfter(long timestamp) {
        AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
        this.cloneAfter(timestamp, clone);
        return clone;
    }

    @Override
    public AncillaryCommodityLoadProfile cloneBefore(long timestamp) {
        AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
        this.cloneBefore(timestamp, clone);
        return clone;
    }

    @Override
    public AncillaryCommodityLoadProfile cloneWithOffset(long offset) {
        AncillaryCommodityLoadProfile clone = new AncillaryCommodityLoadProfile();
        this.cloneWithOffset(offset, clone);
        return clone;
    }

    @Override
    public EnumMap<AncillaryCommodity, Map<Long, Integer>> convertToSimpleMap() {
        EnumMap<AncillaryCommodity, Map<Long, Integer>> map = new EnumMap<>(AncillaryCommodity.class);
        this.convertToSimpleMap(map, this.getEndingTimeOfProfile());
        return map;
    }

    @Override
    public EnumMap<AncillaryCommodity, Map<Long, Integer>> convertToSimpleMap(long maxTime) {
        EnumMap<AncillaryCommodity, Map<Long, Integer>> map = new EnumMap<>(AncillaryCommodity.class);
        this.convertToSimpleMap(map, maxTime);
        return map;
    }
}
