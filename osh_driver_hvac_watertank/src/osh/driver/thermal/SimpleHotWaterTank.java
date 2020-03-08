package osh.driver.thermal;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class SimpleHotWaterTank extends SimpleWaterTank {

    private static final long serialVersionUID = 4457037148593308740L;


    /**
     * CONSTRUCTOR
     */
    public SimpleHotWaterTank(
            double tankCapacity,
            Double tankDiameter,
            Double startTemperature,
            Double ambientTemperature) {
        super(
                // FZI HoLL (2 tanks), empirical
//				2 * -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)), 
                // KIT ESHL
                1 * -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)),
                tankCapacity,
                tankDiameter,
                startTemperature,
                ambientTemperature);
    }

    public SimpleHotWaterTank(SimpleHotWaterTank other) {
        super(other);
    }

}