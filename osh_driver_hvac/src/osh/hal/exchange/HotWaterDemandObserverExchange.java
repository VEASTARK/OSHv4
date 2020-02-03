package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class HotWaterDemandObserverExchange extends HALDeviceObserverExchange {

    private final int power;


    public HotWaterDemandObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            int power) {
        super(deviceID, timestamp);

        this.power = power;
    }

    public int getHotWaterPower() {
        return this.power;
    }
}
