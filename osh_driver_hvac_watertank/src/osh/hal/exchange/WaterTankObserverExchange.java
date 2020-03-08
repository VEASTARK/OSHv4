package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents an abstract information exchange about the state and handling of a water tank.
 *
 * @author Sebastian Kramer
 */
public abstract class WaterTankObserverExchange extends HALDeviceObserverExchange {

    private final double topTemperature;

    private final double tankCapacity;
    private final double tankDiameter;
    private final double ambientTemperature;
    private final double standingHeatLossFactor;

    /**
     * The temperature border for deviation from the predicted temperature above which the violation clock for
     * reschedlung beings to tick.
     */
    private final double rescheduleIfViolatedTemperature;
    /**
     * Duration for how long the violation clock needs to tick before a rescheduling is triggered.
     */
    private final Duration rescheduleIfViolatedDuration;

    private boolean sendNewIpp;
    private boolean forceRescheduling;

    /**
     * Creates this information exchange with the given values about the state and handling of the water tank.
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
     */
    public WaterTankObserverExchange(UUID deviceID, ZonedDateTime timestamp, double topTemperature,
                                     double tankCapacity, double tankDiameter, double ambientTemperature,
                                     double standingHeatLossFactor, double rescheduleIfViolatedTemperature,
                                     Duration rescheduleIfViolatedDuration) {
        super(deviceID, timestamp);
        this.topTemperature = topTemperature;
        this.tankCapacity = tankCapacity;
        this.tankDiameter = tankDiameter;
        this.ambientTemperature = ambientTemperature;
        this.standingHeatLossFactor = standingHeatLossFactor;
        this.rescheduleIfViolatedTemperature = rescheduleIfViolatedTemperature;
        this.rescheduleIfViolatedDuration = rescheduleIfViolatedDuration;
    }

    /**
     * Returns the top-temperature of the tank.
     *
     * @return the top-temperature of the tank
     */
    public double getTopTemperature() {
        return this.topTemperature;
    }

    /**
     * Returns the capacity of the tank.
     *
     * @return the capacity of the tank
     */
    public double getTankCapacity() {
        return this.tankCapacity;
    }

    /**
     * Returns the diameter of the tank.
     *
     * @return the diameter of the tank
     */
    public double getTankDiameter() {
        return this.tankDiameter;
    }

    /**
     * Returns the ambient temperature surrounding the water tank.
     *
     * @return the ambient temperature surrounding the water tank
     */
    public double getAmbientTemperature() {
        return this.ambientTemperature;
    }

    /**
     * Returns the heat-loss factor as a multiple of the standard assumed heat-loss of the simple water tank.
     *
     * @return the heat-loss factor as a multiple of the standard assumed heat-loss of the simple water tank
     */
    public double getStandingHeatLossFactor() {
        return this.standingHeatLossFactor;
    }

    /**
     * Returns the temeprature border for prediction violation.
     *
     * @return the temeprature border for prediction violation
     */
    public double getRescheduleIfViolatedTemperature() {
        return this.rescheduleIfViolatedTemperature;
    }

    /**
     * Returns the clock duration for prediction violation.
     *
     * @return the clock duration for prediction violation
     */
    public Duration getRescheduleIfViolatedDuration() {
        return this.rescheduleIfViolatedDuration;
    }

    /**
     * Returns if a new ipp should be sent.
     *
     * @return true if a new ipp should be sent
     */
    public boolean isSendNewIpp() {
        return this.sendNewIpp;
    }

    /**
     * Sets the flag if a new ipp should be sent.
     *
     * @param sendNewIpp the new value for the flag
     */
    public void setSendNewIpp(boolean sendNewIpp) {
        this.sendNewIpp = sendNewIpp;
    }

    /**
     * Returns if a rescheduling should be forced.
     *
     * @return true if a rescheduling should be forced
     */
    public boolean isForceRescheduling() {
        return this.forceRescheduling;
    }

    /**
     * Sets the flag if a rescheduling should be forced.
     *
     * @param forceRescheduling the new value for the flag
     */
    public void setForceRescheduling(boolean forceRescheduling) {
        this.forceRescheduling = forceRescheduling;
    }
}
