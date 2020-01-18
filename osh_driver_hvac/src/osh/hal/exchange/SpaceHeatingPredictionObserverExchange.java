package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.util.Map;
import java.util.UUID;

/**
 * @author Jan Mueller
 */
public class SpaceHeatingPredictionObserverExchange extends HALDeviceObserverExchange {

    private final Map<Long, Double> predictedHeatConsumptionMap;

    public SpaceHeatingPredictionObserverExchange(UUID deviceID, Long timestamp,
                                                  Map<Long, Double> predictedHeatConsumptionMap) {
        super(deviceID, timestamp);
        this.predictedHeatConsumptionMap = predictedHeatConsumptionMap;
    }

    public Map<Long, Double> getPredictedHeatConsumptionMap() {
        return this.predictedHeatConsumptionMap;
    }

}
