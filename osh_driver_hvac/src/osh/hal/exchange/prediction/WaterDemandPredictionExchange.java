package osh.hal.exchange.prediction;

import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALObserverExchange;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class WaterDemandPredictionExchange
        extends HALObserverExchange {

    final List<SparseLoadProfile> powerPredictions;
    private final int pastDaysPrediction;
    private final float weightForOtherWeekday;
    private final float weightForSameWeekday;


    /**
     * CONSTRUCTOR 1
     *
     * @param deviceID
     * @param timestamp
     */
    public WaterDemandPredictionExchange(UUID deviceID, ZonedDateTime timestamp,
                                         List<SparseLoadProfile> powerPredictions,
                                         int pastDaysPrediction,
                                         float weightForOtherWeekday,
                                         float weightForSameWeekday) {
        super(deviceID, timestamp);
        this.powerPredictions = powerPredictions;
        this.pastDaysPrediction = pastDaysPrediction;
        this.weightForOtherWeekday = weightForOtherWeekday;
        this.weightForSameWeekday = weightForSameWeekday;
    }

    public List<SparseLoadProfile> getPredictions() {
        return this.powerPredictions;
    }

    public int getPastDaysPrediction() {
        return this.pastDaysPrediction;
    }

    public float getWeightForOtherWeekday() {
        return this.weightForOtherWeekday;
    }

    public float getWeightForSameWeekday() {
        return this.weightForSameWeekday;
    }

}
