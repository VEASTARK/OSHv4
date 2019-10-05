package osh.esc.grid;

import osh.datatypes.commodity.Commodity;

import java.io.Serializable;

public class InitializedEnergyRelationTarget implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1754351498889622361L;
	
	private int targetID;
	private Commodity commodity;
	
	public InitializedEnergyRelationTarget(int targetID, Commodity commodity) {
		this.targetID = targetID;
		this.commodity = commodity;
	}
	
	/** do not use - for serialisation only */
	@Deprecated
	protected InitializedEnergyRelationTarget() {
		
	}
	
	public int getTargetID() {
		return targetID;
	}
	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}
	public Commodity getCommodity() {
		return commodity;
	}
	public void setCommodity(Commodity commodity) {
		this.commodity = commodity;
	}

}
