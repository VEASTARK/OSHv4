package osh.hal.exchange;

import osh.cal.CALComExchange;

import java.time.ZonedDateTime;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class GUIHotWaterPredictionComExchange extends CALComExchange {

    private final TreeMap<Long, Double> predictedTankTemp = new TreeMap<>();
    private final TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
    private final TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();

    public GUIHotWaterPredictionComExchange(UUID sender,
                                            ZonedDateTime timestamp,
                                            TreeMap<Long, Double> predictedTankTemp,
                                            TreeMap<Long, Double> predictedHotWaterDemand,
                                            TreeMap<Long, Double> predictedHotWaterSupply) {
        super(sender, timestamp);
        this.predictedTankTemp.putAll(predictedTankTemp);
        this.predictedHotWaterDemand.putAll(predictedHotWaterDemand);
        this.predictedHotWaterSupply.putAll(predictedHotWaterSupply);
    }

    public TreeMap<Long, Double> getPredictedTankTemp() {
        return this.predictedTankTemp;
    }

    public TreeMap<Long, Double> getPredictedHotWaterDemand() {
        return this.predictedHotWaterDemand;
    }

    public TreeMap<Long, Double> getPredictedHotWaterSupply() {
        return this.predictedHotWaterSupply;
    }

}
