package osh.utils.physics;

/**
 * Represents a collection of physicals constant for use in calclulations.
 *
 * @author Sebastian Kramer
 */
public class PhysicalConstants {

    //@25° C, J * kg^-1 * K^-1
    public static final double isobaricMassHeatCapacity_Water_25C = 4181.3;
    public static final double isobaricVolumeHeatCapacity_Water_25C = 4179.6;

    //TODO: This is slightly wrong, but we need to continue to use the value to keep backwards compatibility, change
    // to the actual values as soon as the next update hits that breaks backwards compatibility
    public static final double schoolBookIsobaricVolumeHeatCapacity_Water_20C = 4190.0;

    //@25° C, J * kg^-1 * K^-1
    public static final double isobaricMassHeatCapacity_Air_20C = 1012.0;
    public static final double isobaricVolumeHeatCapacity_Air_20C = 1.21;

    //@20° C, kg * m^-3
    public static final double density_Water_25C = 997.0479;
    public static final double density_Air_20C = 1.2041;

    public static final double factor_wsToKWh = 3600.0 * 1000.0;
}
