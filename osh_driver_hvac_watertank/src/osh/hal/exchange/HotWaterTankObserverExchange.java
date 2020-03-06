package osh.hal.exchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an information exchange about the state and handling of a hot-water tank.
 *
 * @author Ingo Mauser
 * @author Sebastian Kramer
 */
public class HotWaterTankObserverExchange extends WaterTankObserverExchange {

    private final double hotWaterDemand;
    private final double hotWaterSupply;

    /**
     * Creates this information exchange with the given values about the state and handling of the hot-water tank.
     *
     * @param deviceID the uniquie id of the device sending this information
     * @param timestamp the timestamp when this information is being sent
     * @param tankCapacity the capacity of the water tank
     * @param tankCapacity the capacity of the water tank
     * @param tankDiameter the diameter of the water tank
     * @param ambientTemperature the ambient temperature surrounding the water tank
     * @param standingHeatLossFactor the heat-loss factor as a multiple of the standard assumed heat-loss of the
     *                               simple water tank
     * @param rescheduleIfViolatedTemperature the temeprature border for prediction violation
     * @param rescheduleIfViolatedDuration the clock duration for prediction violation
     * @param hotWaterDemand the current hot-water demand
     * @param hotWaterSupply the current hot-water supply
     */
    public HotWaterTankObserverExchange(UUID deviceID, ZonedDateTime timestamp, double topTemperature, double tankCapacity,
                                        double tankDiameter, double ambientTemperature, double standingHeatLossFactor,
                                        double rescheduleIfViolatedTemperature, Duration rescheduleIfViolatedDuration,
                                        double hotWaterDemand, double hotWaterSupply) {

        super(deviceID, timestamp, topTemperature, tankCapacity, tankDiameter, ambientTemperature, standingHeatLossFactor,
                rescheduleIfViolatedTemperature, rescheduleIfViolatedDuration);
        this.hotWaterDemand = hotWaterDemand;
        this.hotWaterSupply = hotWaterSupply;
    }

    /**
     * Returns the current hot-water demand.
     *
     * @return the current hot-water demand
     */
    public double getHotWaterDemand() {
        return this.hotWaterDemand;
    }

    /**
     * Returns the current hot-water supply.
     *
     * @return the current hot-water supply
     */
    public double getHotWaterSupply() {
        return this.hotWaterSupply;
    }
}
