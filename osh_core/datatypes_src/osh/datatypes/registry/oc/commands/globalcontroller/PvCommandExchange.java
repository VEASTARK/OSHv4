package osh.datatypes.registry.oc.commands.globalcontroller;

import osh.datatypes.registry.CommandExchange;

import java.util.UUID;


public class PvCommandExchange extends CommandExchange {

    /**
     *
     */
    private static final long serialVersionUID = -168998441819144791L;
    private Boolean newPvSwitchedOn;
    private Double reactivePowerTargetValue;

    public PvCommandExchange(UUID sender, UUID receiver, long time, Boolean newPvSwitchedOn) {
        this(sender, receiver, time, newPvSwitchedOn, null);
    }

    public PvCommandExchange(UUID sender, UUID receiver, long time, Double reactivePowerTargetValue) {
        this(sender, receiver, time, null, reactivePowerTargetValue);
    }

    public PvCommandExchange(UUID sender, UUID receiver, long timestamp, Boolean newPvSwitchedOn, Double reactivePowerTargetValue) {
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