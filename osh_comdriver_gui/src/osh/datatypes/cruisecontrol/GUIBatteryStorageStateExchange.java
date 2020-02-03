package osh.datatypes.cruisecontrol;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Jan Mueller
 */
public class GUIBatteryStorageStateExchange
        extends BatteryStorageOCSX
        implements EqualData<BatteryStorageOCSX> {

    private static final long serialVersionUID = 2308641394864672076L;


    public GUIBatteryStorageStateExchange(
            UUID sender,
            ZonedDateTime timestamp,
            double currentStateOfCharge,
            double minStateOfCharge,
            double maxStateOfCharge,
            UUID batteryId) {
        super(sender, timestamp, currentStateOfCharge, minStateOfCharge, maxStateOfCharge, batteryId);
    }


    public boolean equalData(GUIBatteryStorageStateExchange o) {
        return super.equalData(o);
    }
}
