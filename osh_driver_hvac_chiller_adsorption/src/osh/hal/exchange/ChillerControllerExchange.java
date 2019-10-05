package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class ChillerControllerExchange 
				extends HALControllerExchange {
	
	private boolean stopGenerationFlag;
	
	private boolean coolingRequest;
	
	private int scheduledRuntime;

	/**
	 * 
	 * @param deviceID
	 * @param timestamp
	 */
	public ChillerControllerExchange(
			UUID deviceID, 
			Long timestamp, 
			boolean stopGenerationFlag, 
			boolean coolingRequest,
			int scheduledRuntime) {
		super(deviceID, timestamp);
		
		this.stopGenerationFlag = stopGenerationFlag;
		this.coolingRequest = coolingRequest;
		this.scheduledRuntime = scheduledRuntime;
	}

	
	public boolean isStopGenerationFlag() {
		return stopGenerationFlag;
	}

	public boolean isCoolingRequest() {
		return coolingRequest;
	}

	public int getScheduledRuntime() {
		return scheduledRuntime;
	}

	public void setCoolingRequest(boolean heatingRequest) {
		this.coolingRequest = heatingRequest;
	}
	
	public void setStopGenerationFlag(boolean stopGenerationFlag) {
		this.stopGenerationFlag = stopGenerationFlag;
	}	
}