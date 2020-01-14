package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.registry.StateExchange;

import java.util.TreeMap;
import java.util.UUID;

public class GUIHotWaterPredictionStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 5935603463467532593L;

    private final TreeMap<Long, Double> predictedTankTemp;
    private final TreeMap<Long, Double> predictedHotWaterDemand;
    private final TreeMap<Long, Double> predictedHotWaterSupply;

    public GUIHotWaterPredictionStateExchange(
            UUID sender,
            long timestamp,
            TreeMap<Long, Double> predictedTankTemp,
            TreeMap<Long, Double> predictedHotWaterDemand,
            TreeMap<Long, Double> predictedHotWaterSupply) {
        super(sender, timestamp);
        this.predictedTankTemp = predictedTankTemp;
        this.predictedHotWaterDemand = predictedHotWaterDemand;
        this.predictedHotWaterSupply = predictedHotWaterSupply;
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

    @Override
    public GUIHotWaterPredictionStateExchange clone() {
        //TODO: do proper cloning
        return new GUIHotWaterPredictionStateExchange(
                this.sender,
                this.getTimestamp(),
                new TreeMap<>(this.predictedTankTemp),
                new TreeMap<>(this.predictedHotWaterDemand),
                new TreeMap<>(this.predictedHotWaterSupply));
    }

}
