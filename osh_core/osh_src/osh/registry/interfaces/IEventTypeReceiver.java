package osh.registry.interfaces;

import osh.core.interfaces.IPromiseToEnsureSynchronization;
import osh.core.interfaces.IQueueEventTypeSubscriber;

import java.util.UUID;


public interface IEventTypeReceiver extends IQueueEventTypeSubscriber, IPromiseToEnsureSynchronization {
	
	/** return null if the element has no UUID, but it won't be able to receive commands */
	public UUID getUUID();
	
}
