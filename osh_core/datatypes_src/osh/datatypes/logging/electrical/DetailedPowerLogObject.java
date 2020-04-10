package osh.datatypes.logging.electrical;

import osh.datatypes.logging.LoggingObjectStateExchange;
import osh.datatypes.power.AncillaryCommodityLoadProfile;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents specific logging information about the power output of a device.
 *
 * @author Sebastian Kramer
 */
public class DetailedPowerLogObject extends LoggingObjectStateExchange {

    private final AncillaryCommodityLoadProfile loadProfile;

    /**
     * Constructs this log exchange with the given sender, timestamp and the load profile of the device.
     *
     * @param sender the sender of this exchange
     * @param timestamp the timestamp of this exchange
     * @param loadProfile the load profile of the device
     */
    public DetailedPowerLogObject(UUID sender, ZonedDateTime timestamp, AncillaryCommodityLoadProfile loadProfile) {
        super(sender, timestamp);
        this.loadProfile = loadProfile;
    }

    public AncillaryCommodityLoadProfile getLoadProfile() {
        return this.loadProfile;
    }

    @Override
    public DetailedPowerLogObject clone() {
        return new DetailedPowerLogObject(this.getSender(), this.getTimestamp(), this.loadProfile.clone());
    }
}
