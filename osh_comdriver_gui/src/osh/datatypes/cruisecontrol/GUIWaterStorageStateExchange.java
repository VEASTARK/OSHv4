package osh.datatypes.cruisecontrol;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser
 */
public class GUIWaterStorageStateExchange
        extends WaterStorageOCSX
        implements EqualData<WaterStorageOCSX> {


    public GUIWaterStorageStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            double currentTemp,
            double minTemp,
            double maxTemp,
            double demand,
            double supply,
            UUID tankId) {
        super(sender, timestamp, currentTemp, minTemp, maxTemp, demand, supply, tankId);
    }


    public boolean equalData(GUIWaterStorageStateExchange o) {
        return super.equalData(o);
    }
}
