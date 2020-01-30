package osh.hal.exchange;

import osh.cal.CALComExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class GUIWaterStorageComExchange extends CALComExchange {

    private final double currentTemp;
    private final double minTemp;
    private final double maxTemp;
    private final double demand;
    private final double supply;
    private final UUID tankId;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     * @param currentTemp
     * @param minTemp
     * @param maxTemp
     */
    public GUIWaterStorageComExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            double currentTemp,
            double minTemp,
            double maxTemp,
            double demand,
            double supply,
            UUID tankId) {
        super(deviceID, timestamp);

        this.currentTemp = currentTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.demand = demand;
        this.supply = supply;
        this.tankId = tankId;
    }


    public double getCurrentTemp() {
        return this.currentTemp;
    }


    public double getMinTemp() {
        return this.minTemp;
    }


    public double getMaxTemp() {
        return this.maxTemp;
    }

    public double getDemand() {
        return this.demand;
    }


    public double getSupply() {
        return this.supply;
    }

    public UUID getTankId() {
        return this.tankId;
    }

}
