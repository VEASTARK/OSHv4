package osh.datatypes.power;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class PowerInterval {

    private static final double UNKNOWN_UPPER_LIMIT = 43000;
    private static final double UNKNOWN_LOWER_LIMIT = -43000;
    private final double lowerLimit;
    private final double upperLimit;


    /**
     * CONSTRUCTOR
     */
    public PowerInterval() {
        this.upperLimit = PowerInterval.UNKNOWN_UPPER_LIMIT;
        this.lowerLimit = PowerInterval.UNKNOWN_LOWER_LIMIT;
    }

    /**
     * CONSTRUCTOR
     *
     * @param powerUpperLimit
     */
    public PowerInterval(double powerUpperLimit) {
        this.upperLimit = powerUpperLimit;
        this.lowerLimit = PowerInterval.UNKNOWN_LOWER_LIMIT;
    }

    /**
     * CONSTRUCTOR
     *
     * @param powerUpperLimit
     * @param powerLowerLimit
     */
    public PowerInterval(
            double powerUpperLimit,
            double powerLowerLimit) {
        this.upperLimit = powerUpperLimit;
        this.lowerLimit = powerLowerLimit;
    }


    public double[] getPowerLimits() {
        double[] activeLimits = new double[2];
        activeLimits[0] = this.upperLimit;
        activeLimits[1] = this.lowerLimit;
        return activeLimits;
    }

    public double getPowerUpperLimit() {
        return this.upperLimit;
    }

    public double getPowerLowerLimit() {
        return this.lowerLimit;
    }


    public boolean equals(PowerInterval other) {
        if (other == null) {
            return false;
        }
        return this.lowerLimit == other.lowerLimit && this.upperLimit == other.upperLimit;
    }

    @Override
    public PowerInterval clone() {
        return new PowerInterval(this.upperLimit, this.lowerLimit);
    }

    @Override
    public String toString() {
        return "uL=" + this.upperLimit + " lL=" + this.lowerLimit;
    }

}
