package osh.hal.exchange.prediction;

import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALObserverExchange;

import java.util.List;
import java.util.UUID;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class WaterDemandPredictionExchange 
				extends HALObserverExchange {
	
	List<SparseLoadProfile> powerPredicitions;
	private int pastDaysPrediction;
	private float weightForOtherWeekday;
	private float weightForSameWeekday;
	
	
	/**
	 * CONSTRUCTOR 1
	 * @param deviceID
	 * @param timestamp
	 */
	public WaterDemandPredictionExchange(UUID deviceID, Long timestamp, List<SparseLoadProfile> powerPredicitions, 
			int pastDaysPrediction,
			float weightForOtherWeekday,
			float weightForSameWeekday) {
		super(deviceID, timestamp);
		this.powerPredicitions = powerPredicitions;
		this.pastDaysPrediction = pastDaysPrediction;
		this.weightForOtherWeekday = weightForOtherWeekday;
		this.weightForSameWeekday = weightForSameWeekday;
	}
	
	public List<SparseLoadProfile> getPredicitons() {
		return powerPredicitions;
	}
	
	public int getPastDaysPrediction() {
		return pastDaysPrediction;
	}

	public float getWeightForOtherWeekday() {
		return weightForOtherWeekday;
	}

	public float getWeightForSameWeekday() {
		return weightForSameWeekday;
	}

}
