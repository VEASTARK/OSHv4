package osh.hal.exchange;

import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALObserverExchange;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class BaseloadPredictionExchange
        extends HALObserverExchange {

    final List<SparseLoadProfile> powerPredictions;
    private final float weightForOtherWeekday;
    private final float weightForSameWeekday;
    private final int usedDaysForPrediction;


    /**
     * CONSTRUCTOR 1
     *
     * @param deviceID
     * @param timestamp
     */
    public BaseloadPredictionExchange(UUID deviceID, ZonedDateTime timestamp, List<SparseLoadProfile> powerPredictions,
                                      int usedDaysForPrediction, float weightForOtherWeekday, float weightForSameWeekday) {
        super(deviceID, timestamp);
        this.powerPredictions = powerPredictions;
        this.usedDaysForPrediction = usedDaysForPrediction;
        this.weightForOtherWeekday = weightForOtherWeekday;
        this.weightForSameWeekday = weightForSameWeekday;
    }

    public List<SparseLoadProfile> getPredictions() {
        return this.powerPredictions;
    }

    public float getWeightForOtherWeekday() {
        return this.weightForOtherWeekday;
    }

    public float getWeightForSameWeekday() {
        return this.weightForSameWeekday;
    }

    public int getUsedDaysForPrediction() {
        return this.usedDaysForPrediction;
    }

}
