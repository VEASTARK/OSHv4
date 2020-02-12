package osh.datatypes.power;

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class AncillaryCommodityLoadProfile extends LoadProfile<AncillaryCommodity> {

    private static final AncillaryCommodity[] ancillaryCommodityValues = AncillaryCommodity.values();
    private final int[] sequentialFloorValues =
            new int[AncillaryCommodity.values().length];


    public AncillaryCommodityLoadProfile() {
        super(AncillaryCommodity.class);
    }

    public void initSequential() {
        Arrays.fill(this.sequentialFloorValues, Integer.MAX_VALUE);
    }

    public void endSequential() {
    }

    /**
     * WARNING: Do NOT use if you want to either set Loads non-sequential (now or in the future) or if you want to compress/merge/clone ...
     * <p>
     * <p>
     * Sets the load of the provided ancillaryCommodity, but will ignore same power level inputs
     *
     * @param state the ancillary meter states
     * @param t     the time to put the ancillary meter values
     */
    public void setLoadSequential(AncillaryMeterState state, long t) {

        double[] allPowers = state.getAllPowerStates();

        for (int i = 0; i < allPowers.length; i++) {
            int power = (int) allPowers[i];
            int oldPower = this.sequentialFloorValues[i];

            if (oldPower != power) {
                this.commodities.get(ancillaryCommodityValues[i]).put(t, power);
                this.sequentialFloorValues[i] = power;
            }
        }
    }

    public LongSortedSet getAllLoadChangesFor(AncillaryCommodity ac, long from, long to) {
        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        return this.commodities.get(ac).subMap(from + 1, to).keySet();
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
