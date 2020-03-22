package osh.datatypes.power;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.utils.dataStructures.Long2DoubleErsatzNavigableMap;

import java.util.Arrays;
import java.util.EnumMap;

/**
 * Implements are bare-bones and stripped down version of {@link AncillaryCommodityLoadProfile} for use only inside
 * the optimization loop.
 *
 * @author Sebastian Kramer
 */
public class ErsatzACLoadProfile {

    private final EnumMap<AncillaryCommodity, Long2DoubleErsatzNavigableMap> commodities = new EnumMap<>(AncillaryCommodity
            .class);
    private long endingTimeOfProfile;

    private final double[][] valuesSequential;
    private final long[][] keysSequential;
    private final int[] previousValues = new int[ErsatzACLoadProfile.acCount];
    private final int[] indexes = new int[ErsatzACLoadProfile.acCount];

    private static final AncillaryCommodity[] acValues = AncillaryCommodity.values();
    private static final int acCount = acValues.length;

    /**
     * Constructs this load profile with the given amount of maximum capacity for the data arrays.
     *
     * @param maxLength amount of maximum capacity for the data arrays
     */
    public ErsatzACLoadProfile(int maxLength) {
        this.valuesSequential = new double[acCount][maxLength];
        this.keysSequential = new long[acCount][maxLength];
        Arrays.fill(this.previousValues, Integer.MAX_VALUE);
    }

    /**
     * Constructs this load profile with the same amount of maximum capacity for the data arrays as the given other
     * load profile.
     *
     * @param other the other load profile to base this profile on
     */
    public ErsatzACLoadProfile(ErsatzACLoadProfile other) {
        this(other.keysSequential[0].length);
    }

    /**
     * Prepares this load profile for another round of sequential puts.
     */
    public void resetSequential() {
        Arrays.fill(this.previousValues, Integer.MAX_VALUE);
        Arrays.fill(this.indexes, 0);
    }

    /**
     * Performs a sequential put into this load profile of the values contained in the given
     * {@link AncillaryMeterState}.
     *
     * <p>
     * Values will only be inserted if they differ from their previous state, to save computing time in the
     * cost-function.
     *
     * @param state the state of the ancillary meter
     * @param t the current (simulated) time
     */
    public void setLoadSequential(AncillaryMeterState state, long t) {
        double[] powers = state.getAllPowerStates();
        for (int j = 0; j < powers.length; j++) {
            if ((int) powers[j] != this.previousValues[j]) {
                this.keysSequential[j][this.indexes[j]] = t;
                this.valuesSequential[j][this.indexes[j]] = (int) powers[j];
                this.previousValues[j] = (int) powers[j];
                this.indexes[j]++;
            }
        }
    }

    /**
     * Commits the previously temporarily held sequential puts into the respective
     * {@link Long2DoubleErsatzNavigableMap}.
     *
     * @param endingTimeOfProfile the end point of the load profile
     */
    public void endSequential(long endingTimeOfProfile) {
        for (AncillaryCommodity ac : acValues) {
            this.commodities.put(ac, new Long2DoubleErsatzNavigableMap(this.keysSequential[ac.ordinal()], this.valuesSequential[ac.ordinal()],
                    this.indexes[ac.ordinal()]));
        }

        this.endingTimeOfProfile = endingTimeOfProfile;
    }

    /**
     * Returns the key array for the given {@link AncillaryCommodity}.
     *
     * @param ac the ancillary commodity
     *
     * @return the key array for the given {@link AncillaryCommodity}
     */
    public long[] getKeyFor(AncillaryCommodity ac) {
        return this.commodities.get(ac).getKey();
    }

    /**
     * Returns the value array for the given {@link AncillaryCommodity}.
     *
     * @param ac the ancillary commodity
     *
     * @return the value array for the given {@link AncillaryCommodity}
     */
    public double[] getValueFor(AncillaryCommodity ac) {
        return this.commodities.get(ac).getValue();
    }

    /**
     * Returns the ending point of this load profile.
     *
     * @return the ending point of this load profile
     */
    public long getEndingTimeOfProfile() {
        return this.endingTimeOfProfile;
    }
}
