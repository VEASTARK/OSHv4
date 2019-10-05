package osh.core.oc;

import osh.core.interfaces.IOSHOC;
import osh.eal.hal.HALRealTimeDriver;

import java.util.UUID;

/**
 * abstract superclass for the O/C-Unit container
 * 
 * @author Florian Allerding
 */
public abstract class OCUnit {
	
	private IOSHOC controllerbox;
	protected final UUID unitID;
	
	
	public OCUnit(UUID unitID, IOSHOC controllerbox) {
		this.controllerbox = controllerbox;
		this.unitID = unitID;
	}
	
	
	protected HALRealTimeDriver getSystemTimer() {
		return controllerbox.getTimer();
	}
	
	public UUID getUnitID() {
		return unitID;
	}

}
