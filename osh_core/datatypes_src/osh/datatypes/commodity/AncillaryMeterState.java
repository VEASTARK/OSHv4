package osh.datatypes.commodity;

import osh.esc.ArrayUtils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wrapper class for the state of the ancillary meter
 *
 * @author Sebastian Kramer
 */
public class AncillaryMeterState implements Serializable {

    private static final long serialVersionUID = -1849030207239247471L;

    private static final int enumValCount = AncillaryCommodity.values().length;
    private double[] powerStates = new double[enumValCount];

    public AncillaryMeterState() {}

    public AncillaryMeterState(AncillaryMeterState other) {
        this.powerStates = Arrays.copyOf(other.powerStates, enumValCount);
    }

    public double getPower(AncillaryCommodity ancillaryCommodity) {
        return this.powerStates[ancillaryCommodity.ordinal()];
    }

    public void setPower(AncillaryCommodity ancillaryCommodity, double power) {
        this.powerStates[ancillaryCommodity.ordinal()] = power;
    }

    public void clear() {
        ArrayUtils.fillArrayDouble(this.powerStates, 0.0);
    }

    public double[] getAllPowerStates() {
        return this.powerStates;
    }

    public AncillaryMeterState clone() {
        return new AncillaryMeterState(this);
    }

    public static class ImmutableAncillaryMeterState extends AncillaryMeterState {

        private static final long serialVersionUID = -8438380085453609722L;

        public ImmutableAncillaryMeterState(AncillaryMeterState other) {
            super(other);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setPower(AncillaryCommodity ancillaryCommodity, double power) {
            throw new UnsupportedOperationException();
        }
    }
}
