package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;


/**
 * 
 * @author Sebastian Kramer
 *
 */
public class GenericApplianceStarttimesControllerExchange extends HALControllerExchange {

	private long startTime;
	
	
	/**
	 * CONSTRUCTOR
	 * @param deviceID
	 * @param timestamp
	 */
	public GenericApplianceStarttimesControllerExchange(
			UUID deviceID, 
			Long timestamp,
			long startTime) {
		super(deviceID, timestamp);
		
		this.startTime = startTime;
	}


	public long getStartTime() {
		return startTime;
	}


	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	
}
