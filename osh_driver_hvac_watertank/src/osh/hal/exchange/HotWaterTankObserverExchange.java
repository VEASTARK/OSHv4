package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class HotWaterTankObserverExchange extends HALDeviceObserverExchange {

    private final double topTemperature;

    private final double tankCapacity;
    private final double tankDiameter;
    private final double ambientTemperature;

    private final double hotWaterDemand;
    private final double hotWaterSupply;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public HotWaterTankObserverExchange(
            UUID deviceID,
            Long timestamp,
            double topTemperature,
            double tankCapacity,
            double tankDiameter,
            double ambientTemperature,
            double hotWaterDemand,
            double hotWaterSupply) {
        super(deviceID, timestamp);

        this.topTemperature = topTemperature;
        this.tankCapacity = tankCapacity;
        this.tankDiameter = tankDiameter;
        this.ambientTemperature = ambientTemperature;
        this.hotWaterDemand = hotWaterDemand;
        this.hotWaterSupply = hotWaterSupply;
    }

    public double getTopTemperature() {
        return this.topTemperature;
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

    public double getHotWaterDemand() {
        return this.hotWaterDemand;
    }

    public double getHotWaterSupply() {
        return this.hotWaterSupply;
    }
}
