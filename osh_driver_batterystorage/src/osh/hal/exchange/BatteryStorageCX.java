package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.List;
import java.util.UUID;

/**
 * @author Jan Mueller
 */
public class BatteryStorageCX extends HALControllerExchange {

    private final List<Integer> controlList;
    private final Long referenceTime;


    /**
     * CONSTRUCTOR
     */
    public BatteryStorageCX(
            UUID deviceID,
            Long timestamp,
            List<Integer> list,
            Long referenceTime
    ) {
        super(deviceID, timestamp);
        this.controlList = list;
        this.referenceTime = referenceTime;
    }


    public long getReferenceTime() {
        return this.referenceTime;
    }

    public List<Integer> getControlList() {
        return this.controlList;
    }

}
