package osh.datatypes.registry.oc.localobserver;

import osh.datatypes.registry.StateExchange;

import java.util.UUID;

//import osh.datatypes.energy.INeededEnergy;


/**
 * 
 * @author Jan Mueller, Sebastian Kochanneck
 *
 */
public class PvIntelligentOCSX extends StateExchange {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -889391896803816383L;
	private UUID pvIntelligentId;
	private double pMaxLim, qMaxLim;
	
	
	/**
	 * CONSTRUCTOR
	 * @param sender
	 * @param timestamp
	 * @param stateOfCharge
	 * @param minStateOfCharge
	 * @param maxStateOfCharge
	 */
	public PvIntelligentOCSX(
			UUID sender, 
			long timestamp, 
			double maxActivePower, 
			double maxReactivePower, 
			double currentActivePower,
			double currentReactivePower,
			UUID pvIntelligentId
			) {
		super(sender, timestamp);
		
		this.pMaxLim = maxActivePower;
		this.qMaxLim = maxReactivePower;
		this.pvIntelligentId = pvIntelligentId;
	}

	public UUID getPvIntelligentId() {
		return pvIntelligentId;
	}


	public double getMaxActivePower() {
		return pMaxLim;
	}

	public double getMaxReactivePower() {
		return qMaxLim;
	}

	public boolean equalData(PvIntelligentOCSX o) {
		if (o instanceof PvIntelligentOCSX) {
			PvIntelligentOCSX oex = o;
			
			//compare using an epsilon environment
            return Math.abs(pMaxLim - oex.pMaxLim) < 0.001 &&
                    Math.abs(pMaxLim - oex.pMaxLim) < 0.001 &&
                    Math.abs(pMaxLim - oex.pMaxLim) < 0.001
                    && ((pvIntelligentId != null && pvIntelligentId.equals(oex.pvIntelligentId)) || (pvIntelligentId == null && oex.pvIntelligentId == null));
		}
		
		return false;
	}
}
