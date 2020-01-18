package osh.driver.thermal;

/**
 * @author Ingo Mauser
 */
public class ESHLHotWaterTank extends SimpleHotWaterTank {

    /**
     *
     */
    private static final long serialVersionUID = 3449104859618324997L;

    public ESHLHotWaterTank(double tankCapacity, Double tankDiameter, Double startTemperature,
                            Double ambientTemperature) {
        super(tankCapacity, tankDiameter, startTemperature, ambientTemperature);

    }

}
