package osh.hal.exchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an information exchange about the state and handling of a cold-water tank.
 *
 * @author Ingo Mauser
 * @author Sebastian Kramer
 */
public class ColdWaterTankObserverExchange
        extends WaterTankObserverExchange {

    private final double coldWaterDemand;
    private final double coldWaterSupply;

    /**
     * Creates this information exchange with the given values about the state and handling of the cold-water tank.
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
     * @param coldWaterDemand the current cold-water demand
     * @param coldWaterSupply the current cold-water supply
     */
    public ColdWaterTankObserverExchange(UUID deviceID, ZonedDateTime timestamp, double topTemperature, double tankCapacity,
                                        double tankDiameter, double ambientTemperature, double standingHeatLossFactor,
                                        double rescheduleIfViolatedTemperature, Duration rescheduleIfViolatedDuration,
                                        double coldWaterDemand, double coldWaterSupply) {

        super(deviceID, timestamp, topTemperature, tankCapacity, tankDiameter, ambientTemperature, standingHeatLossFactor,
                rescheduleIfViolatedTemperature, rescheduleIfViolatedDuration);
        this.coldWaterDemand = coldWaterDemand;
        this.coldWaterSupply = coldWaterSupply;
    }

    /**
     * Returns the current cold-water demand.
     *
     * @return the current cold-water demand
     */
    public double getColdWaterDemand() {
        return this.coldWaterDemand;
    }

    /**
     * Returns the current cold-water supply.
     *
     * @return the current cold-water supply
     */
    public double getColdWaterSupply() {
        return this.coldWaterSupply;
    }
}
