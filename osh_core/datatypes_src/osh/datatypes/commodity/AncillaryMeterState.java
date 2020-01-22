package osh.datatypes.commodity;

import osh.esc.ArrayUtils;

import java.util.Arrays;

/**
 * Wrapper class for the state of the ancillary meter
 *
 * @author Sebastian Kramer
 */
public class AncillaryMeterState {

    private static final int enumValCount = AncillaryCommodity.values().length;
    private static final double[] EMPTY_POWER = new double[enumValCount];
    static {
        Arrays.fill(EMPTY_POWER, 0.0);
    }
    private double[] powerStates = new double[enumValCount];

    public AncillaryMeterState() {
//		Arrays.fill(powerStates, 0.0);
    }

    public AncillaryMeterState(AncillaryMeterState other) {
//		powerStates = new double[AncillaryCommodity.values().length];
        this.powerStates = Arrays.copyOf(this.powerStates, enumValCount);
    }

    public double getPower(AncillaryCommodity ancillaryCommodity) {
        return this.powerStates[ancillaryCommodity.ordinal()];
    }

    public void setPower(AncillaryCommodity ancillaryCommodity, double power) {
        this.powerStates[ancillaryCommodity.ordinal()] = power;
    }

    public void clear() {
//		Arrays.fill(powerStates, 0.0);
        ArrayUtils.fillArrayDouble(this.powerStates, 0.0);
    }

    public double[] getAllPowerStates() {
        return this.powerStates;
    }

    public AncillaryMeterState clone() {
        return new AncillaryMeterState(this);
    }
}
