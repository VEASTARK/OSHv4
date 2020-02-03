package osh.hal.exchange;

import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALObserverExchange;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class PvPredictionExchange
        extends HALObserverExchange {

    private final List<SparseLoadProfile> powerPredictions;
    private final int pastDaysPrediction;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public PvPredictionExchange(UUID deviceID, ZonedDateTime timestamp, List<SparseLoadProfile> powerPredictions,
                                int pastDaysPrediction) {
        super(deviceID, timestamp);
        this.powerPredictions = powerPredictions;
        this.pastDaysPrediction = pastDaysPrediction;
    }

    public List<SparseLoadProfile> getPredictions() {
        return this.powerPredictions;
    }

    public int getPastDaysPrediction() {
        return this.pastDaysPrediction;
    }

}
