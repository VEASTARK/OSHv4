package osh.core.com;

import osh.cal.ICALExchange;
import osh.core.interfaces.IOSHOC;

import java.util.UUID;

/**
 * 
 * @author Ingo Mauser
 *
 */
public class DummyComManager extends ComManager {

	public DummyComManager(IOSHOC controllerbox, UUID uuid) {
		super(controllerbox, uuid);
	}

	@Override
	public void onDriverUpdate(ICALExchange exchangeObject) {
		//NOTHING
	}

}