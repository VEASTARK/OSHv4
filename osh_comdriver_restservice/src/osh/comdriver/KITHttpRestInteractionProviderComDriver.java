package osh.comdriver;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;

import java.util.UUID;


/**
 * 
 * @author Kaibin Bao
 *
 */
public class KITHttpRestInteractionProviderComDriver extends HttpRestInteractionProviderBusDriver {

	public KITHttpRestInteractionProviderComDriver(
			IOSH controllerbox,
			UUID deviceID, 
			OSHParameterCollection driverConfig) {
		super(controllerbox, deviceID, driverConfig);
	}

	@Override
	String getEnvironment() {
		return "kit";
	}

}
