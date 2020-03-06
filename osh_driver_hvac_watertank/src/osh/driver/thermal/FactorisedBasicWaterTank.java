package osh.driver.thermal;

/**
 * Represents a simple water tank with a specified heat-loss-factor.
 *
 * @author Sebastian Kramer
 */
public class FactorisedBasicWaterTank extends SimpleWaterTank {

    private static final long serialVersionUID = -5444959455010082834L;

    private final double standingHeatLossFactor;

    /**
     * Constructs this water tank with the basic parameters given.
     *
     * @param tankCapacity the capacity of the water tank
     * @param tankDiameter the diameter of the water tank
     * @param startTemperature the starting temperature of the water tank
     * @param ambientTemperature the ambient temperature surrounding the water tank
     * @param standingHeatLossFactor the heat-loss factor as a multiple of the standard assumed heat-loss of the
     *                               simple water tank
     */
    public FactorisedBasicWaterTank(
            double tankCapacity,
            Double tankDiameter,
            Double startTemperature,
            Double ambientTemperature,
            double standingHeatLossFactor) {
        super(
                standingHeatLossFactor * -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)),
                tankCapacity,
                tankDiameter,
                startTemperature,
                ambientTemperature);

        this.standingHeatLossFactor = standingHeatLossFactor;
    }

    /**
     * Constructs a copy of the other given water tank.
     *
     * @param other the water tank to copy
     */
    public FactorisedBasicWaterTank(FactorisedBasicWaterTank other) {
        super(other);
        this.standingHeatLossFactor = other.standingHeatLossFactor;
    }

    /**
     * Returns the heat-loss factor.
     *
     * @return the heat-loss factor
     */
    public double getStandingHeatLossFactor() {
        return this.standingHeatLossFactor;
    }
}
