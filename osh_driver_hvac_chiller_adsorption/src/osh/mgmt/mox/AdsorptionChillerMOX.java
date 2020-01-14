package osh.mgmt.mox;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;

import java.util.Map;

/**
 * @author Ingo Mauser
 */
public class AdsorptionChillerMOX implements IModelOfObservationExchange {

    private final double coldWaterTemperature;
    private final double hotWaterTemperature;
    private final boolean running;
    private final int remainingRunningTime;

    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;

    private final Map<Long, Double> temperatureMap;

    /**
     * CONSTRUCTOR
     */
    public AdsorptionChillerMOX(
            double coldWaterTemperature,
            double hotWaterTemperature,
            boolean running,
            int remainingRunningTime,
            Map<Long, Double> temperatureMap,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super();

        this.coldWaterTemperature = coldWaterTemperature;
        this.hotWaterTemperature = hotWaterTemperature;
        this.running = running;
        this.remainingRunningTime = remainingRunningTime;

        this.compressionType = compressionType;
        this.compressionValue = compressionValue;

        this.temperatureMap = temperatureMap;
    }


    public double getColdWaterTemperature() {
        return this.coldWaterTemperature;
    }

    public double getHotWaterTemperature() {
        return this.hotWaterTemperature;
    }

    public boolean isRunning() {
        return this.running;
    }

    public int getRemainingRunningTime() {
        return this.remainingRunningTime;
    }

    public Map<Long, Double> getTemperatureMap() {
        return this.temperatureMap;
    }


    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }


    public int getCompressionValue() {
        return this.compressionValue;
    }
}