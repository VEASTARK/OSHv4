package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ColdWaterTankObserverExchange
        extends HALDeviceObserverExchange {

    private final double topTemperature;
    private final double tankCapacity;

    private double coldWaterDemand;
    private double coldWaterSupply;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public ColdWaterTankObserverExchange(
            UUID deviceID,
            Long timestamp,
            double topTemperature,
            double tankCapacity,
            double hotWaterDemand,
            double hotWaterSupply) {
        super(deviceID, timestamp);

        this.topTemperature = topTemperature;
        this.tankCapacity = tankCapacity;
    }

    public double getTopTemperature() {
        return this.topTemperature;
    }

    public double getTankCapacity() {
        return this.tankCapacity;
    }

    public double getColdWaterDemand() {
        return this.coldWaterDemand;
    }

    public double getColdWaterSupply() {
        return this.coldWaterSupply;
    }

}
