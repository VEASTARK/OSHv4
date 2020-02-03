package osh.hal.exchange;

import osh.cal.CALComExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Jan Mueller
 */
public class GUIBatteryStorageComExchange extends CALComExchange {

    private final double currentStateOfCharge;
    private final double minStateOfCharge;
    private final double maxStateOfCharge;

    private final UUID batteryId;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     * @param currentStateOfCharge
     * @param minStateOfCharge
     * @param maxStateOfCharge
     */
    public GUIBatteryStorageComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            double currentStateOfCharge,
            double minStateOfCharge,
            double maxStateOfCharge,
            UUID batteryId) {
        super(deviceID, timestamp);

        this.currentStateOfCharge = currentStateOfCharge;
        this.minStateOfCharge = minStateOfCharge;
        this.maxStateOfCharge = maxStateOfCharge;
        this.batteryId = batteryId;
    }

    public double getCurrentStateOfCharge() {
        return this.currentStateOfCharge;
    }

    public double getMinStateOfCharge() {
        return this.minStateOfCharge;
    }

    public double getMaxStateOfCharge() {
        return this.maxStateOfCharge;
    }

    public UUID getBatteryId() {
        return this.batteryId;
    }


}
