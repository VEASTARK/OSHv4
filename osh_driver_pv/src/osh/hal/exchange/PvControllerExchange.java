package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class PvControllerExchange extends HALControllerExchange {

    private Boolean newPvSwitchedOn;
    private Integer reactivePowerTargetValue;


    /**
     * CONSTRUCTOR
     */
    public PvControllerExchange(
            UUID deviceID,
            long timestamp,
            Boolean newPvSwitchedOn,
            Integer reactivePowerTargetValue) {
        super(deviceID, timestamp);

        this.newPvSwitchedOn = newPvSwitchedOn;
        this.reactivePowerTargetValue = reactivePowerTargetValue;
    }


    public Boolean getNewPvSwitchedOn() {
        return this.newPvSwitchedOn;
    }

    public void setNewPvSwitchedOn(Boolean newPvSwitchedOn) {
        this.newPvSwitchedOn = newPvSwitchedOn;
    }

    public Integer getNewReactivePower() {
        return this.reactivePowerTargetValue;
    }

    public void setNewReactivePower(Integer newReactivePower) {
        this.reactivePowerTargetValue = newReactivePower;
    }

}
