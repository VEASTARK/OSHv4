package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class SwitchDriverDetails extends StateExchange {

    protected boolean on;

    public SwitchDriverDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    public boolean isOn() {
        return this.on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof SwitchDriverDetails))
            return false;
        SwitchDriverDetails other = (SwitchDriverDetails) obj;

        return (this.on == other.on);
    }

    @Override
    public String toString() {
        return "Switch: " + (this.on ? "ON" : "OFF");
    }
}
