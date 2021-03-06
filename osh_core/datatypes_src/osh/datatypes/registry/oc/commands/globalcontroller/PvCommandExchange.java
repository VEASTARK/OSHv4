package osh.datatypes.registry.oc.commands.globalcontroller;

import osh.datatypes.registry.CommandExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


public class PvCommandExchange extends CommandExchange {

    private Boolean newPvSwitchedOn;
    private Double reactivePowerTargetValue;

    public PvCommandExchange(UUID sender, UUID receiver, ZonedDateTime time, Boolean newPvSwitchedOn) {
        this(sender, receiver, time, newPvSwitchedOn, null);
    }

    public PvCommandExchange(UUID sender, UUID receiver, ZonedDateTime time, Double reactivePowerTargetValue) {
        this(sender, receiver, time, null, reactivePowerTargetValue);
    }

    public PvCommandExchange(UUID sender, UUID receiver, ZonedDateTime timestamp, Boolean newPvSwitchedOn,
                             Double reactivePowerTargetValue) {
        super(sender, receiver, timestamp);
        this.newPvSwitchedOn = newPvSwitchedOn;
        this.reactivePowerTargetValue = reactivePowerTargetValue;
    }

    public Boolean getNewPvSwitchedOn() {
        return this.newPvSwitchedOn;
    }

    public void setNewPvSwitchedOn(Boolean newPvSwitchedOn) {
        this.newPvSwitchedOn = newPvSwitchedOn;
    }

    public Double getReactivePowerTargetValue() {
        return this.reactivePowerTargetValue;
    }

    public void setReactivePowerTargetValue(Double reactivePowerTargetValue) {
        this.reactivePowerTargetValue = reactivePowerTargetValue;
    }

}