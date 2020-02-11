package osh.datatypes.registry.oc.localobserver;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

//import osh.datatypes.energy.INeededEnergy;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class BatteryStorageOCSX extends StateExchange implements IPromiseToBeImmutable {

    private final UUID batteryId;
    private final double stateOfCharge;
    private final double minStateOfCharge;
    private final double maxStateOfCharge;


    /**
     * CONSTRUCTOR
     */
    public BatteryStorageOCSX(
            UUID sender,
            ZonedDateTime timestamp,
            double stateOfCharge,
            double minStateOfCharge,
            double maxStateOfCharge,
            UUID batteryId) {
        super(sender, timestamp);

        this.stateOfCharge = stateOfCharge;
        this.minStateOfCharge = minStateOfCharge;
        this.maxStateOfCharge = maxStateOfCharge;
        this.batteryId = batteryId;
    }


    public UUID getBatteryId() {
        return this.batteryId;
    }

    public double getStateOfCharge() {
        return this.stateOfCharge;
    }

    public double getMinStateOfCharge() {
        return this.minStateOfCharge;
    }

    public double getMaxStateOfCharge() {
        return this.maxStateOfCharge;
    }

    public boolean equalData(BatteryStorageOCSX o) {
        if (o != null) {
            //compare using an epsilon environment
            return Math.abs(this.stateOfCharge - o.stateOfCharge) < 0.001 &&
                    Math.abs(this.minStateOfCharge - o.minStateOfCharge) < 0.001 &&
                    Math.abs(this.maxStateOfCharge - o.maxStateOfCharge) < 0.001
                    && ((this.batteryId != null && this.batteryId.equals(o.batteryId)) || (this.batteryId == null && o.batteryId == null));
        }
        return false;
    }

}
