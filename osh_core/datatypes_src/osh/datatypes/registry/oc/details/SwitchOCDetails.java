package osh.datatypes.registry.oc.details;

import osh.datatypes.registry.StateExchange;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class SwitchOCDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -9070474430421146195L;
    private boolean on;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public SwitchOCDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }


    public boolean isOn() {
        return this.on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public String toString() {
        return "Switch: " + (this.on ? "ON" : "OFF");
    }
}
