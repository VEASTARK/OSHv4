package osh.mgmt.localobserver;

import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.registry.interfaces.IHasState;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser
 *
 */
public abstract class ThermalDemandLocalObserver 
				extends LocalObserver
				implements IHasState {
	
	
	/**
	 * CONSTRUCTOR 
	 */
	public ThermalDemandLocalObserver(IOSHOC osh) {
		super(osh);
	}
	
	
	@Override
	public IModelOfObservationExchange getObservedModelData(
			IModelOfObservationType type) {
		return null;
	}

	@Override
	public UUID getUUID() {
		return getDeviceID();
	}
	
}