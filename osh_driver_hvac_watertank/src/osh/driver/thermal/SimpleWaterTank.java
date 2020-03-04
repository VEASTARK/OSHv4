package osh.driver.thermal;

/**
 * @author Ingo Mauser
 */
public class SimpleWaterTank extends WaterTank {

    private static final long serialVersionUID = -754345613886808973L;

    /**
     * specific heat capacity [J / (kg * K)]
     */
    private static final double avgThermalCapacityOfWater = 4190.0;

    /**
     * tank capacity [liters]
     */
    private final double tankCapacity;

    /**
     * average temperature [�C]
     */
    private double waterTemperature;

    /**
     * ambient air temperature [�C]
     */
    private final double ambientTemperature;

    /**
     * correction factor with <br>
     * dT_reference = waterTemperature - ambientTemperature = 40K<br>
     * loss = -1.0 * (12 + 5.93 * Math.pow(tankCapacity, 0.4)) [W]
     */
    private final double normalizedEnergyLoss;

    private final double thermalCapacityOfTank;

    // currently unused
    private final double tankHeight;
    private final double tankDiameter;
    @SuppressWarnings("unused")
    private final double tankSurface;


    /**
     * CONSTRCUTOR
     *
     * @param tankCapacity       [liters]
     * @param tankDiameter       [m] (default: 0.5)
     * @param startTemperature    [°C] (default: 50°C)
     * @param ambientTemperature [°C] (default: 20°C)
     */
    public SimpleWaterTank(
            double normalizedEnergyLoss,
            double tankCapacity,
            Double tankDiameter,
            Double startTemperature,
            Double ambientTemperature) {
        // currently relevant parameters

        this.tankDiameter = tankDiameter != null ? tankDiameter : 0.5;

        this.tankCapacity = tankCapacity;

        // variable energy loss (different insulations)
        this.normalizedEnergyLoss = normalizedEnergyLoss;

        this.waterTemperature = startTemperature != null ? startTemperature : 50.0;

        this.ambientTemperature = ambientTemperature != null ? ambientTemperature : 20.0;

        // Geometry: cylinder
        // (currently not relevant)
        this.tankHeight = tankCapacity / 1000.0 / (Math.PI * (this.tankDiameter / 2.0) * (this.tankDiameter / 2.0));
        this.tankSurface = this.tankDiameter * Math.PI * this.tankHeight + 2 * 0.5 * this.tankDiameter * Math.PI;

        this.thermalCapacityOfTank = avgThermalCapacityOfWater * this.tankCapacity;
    }

    public SimpleWaterTank(SimpleWaterTank other) {
        this.tankDiameter = other.tankDiameter;
        this.tankCapacity = other.tankCapacity;
        this.normalizedEnergyLoss = other.normalizedEnergyLoss;
        this.waterTemperature = other.waterTemperature;
        this.ambientTemperature = other.ambientTemperature;
        this.tankHeight = other.tankHeight;
        this.tankSurface = other.tankSurface;
        this.thermalCapacityOfTank = other.thermalCapacityOfTank;
    }

    public void reduceByStandingHeatLoss(long seconds) {
        this.addEnergy(this.calcStandingHeatLoss(seconds));
    }

    /**
     * standing loss (DE: Verlustleistung) respective standing gain (DE: Erw�rmung)
     *
     * @param seconds
     * @return [Ws]
     */
    private double calcStandingHeatLoss(long seconds) {
        double lossCorrectionFactor = (this.waterTemperature - this.ambientTemperature) / 40;
        return this.normalizedEnergyLoss * lossCorrectionFactor * seconds; //[Ws]
    }

    /**
     * positive value: add energy to water<br>
     * negative value: remove energy from water
     *
     * @param power   [W]
     * @param seconds [s]
     */
    public void addPowerOverTime(double power, long seconds, Double reflowTemperature, Double massFlow) {
        this.addEnergy(power * seconds);
    }

    /**
     * positive value: add energy to water<br>
     * negative value: remove energy from water
     *
     * @param energy [Ws] (positive value: add energy to water)
     */
    public void addEnergy(double energy) {
        this.waterTemperature += (energy / this.thermalCapacityOfTank);
    }

    /**
     * @param oldTemperature [�C]
     * @param newTemperature [�C]
     * @param timeDifference [s]
     * @return [W]
     */
    public double calculatePowerDrawOff(double oldTemperature, double newTemperature, long timeDifference) {
        double deltaTheta = newTemperature - oldTemperature;
        double energy = deltaTheta * avgThermalCapacityOfWater * this.tankCapacity;
        return energy / timeDifference;
    }

    /**
     * @param oldTemperature [�C]
     * @param newTemperature [�C]
     * @return [J]
     */
    public double calculateEnergyDrawOff(double oldTemperature, double newTemperature) {
        double deltaTheta = newTemperature - oldTemperature;
        return deltaTheta * avgThermalCapacityOfWater * this.tankCapacity;
    }

    public double setCurrentWaterTemperature(double temperature) {
        return this.waterTemperature = temperature;
    }

    public double getCurrentWaterTemperature() {
        return this.waterTemperature;
    }

    public double getTankCapacity() {
        return this.tankCapacity;
    }

    public double getAmbientTemperature() {
        return this.ambientTemperature;
    }

    public double getTankDiameter() {
        return this.tankDiameter;
    }

}
