package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;

import java.util.UUID;


/**
 * Stop device now
 * 
 * @author Kaibin Bao
 *
 */
public class StopDeviceRequest extends CommandExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -465373952181618356L;

	public StopDeviceRequest(UUID sender, UUID receiver, long timestamp) {
		super(sender, receiver, timestamp);
	}

}
