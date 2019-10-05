package osh.datatypes.cruisecontrol;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;

import java.util.UUID;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
public class GUIWaterStorageStateExchange 
				extends WaterStorageOCSX 
				implements EqualData<WaterStorageOCSX> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7038499626567724690L;


	public GUIWaterStorageStateExchange(
			UUID sender, 
			long timestamp,
			double currenttemp, 
			double mintemp, 
			double maxtemp,
			double demand,
			double supply,
			UUID tankId) {
		super(sender, timestamp, currenttemp, mintemp, maxtemp, demand, supply, tankId);
	}

	
	public boolean equalData(GUIWaterStorageStateExchange o) {
		return super.equalData(o);
	}
}
