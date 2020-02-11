package osh.datatypes.registry.oc.details.energy;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ElectricCurrentOCDetails extends StateExchange {

    private double current;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public ElectricCurrentOCDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    public double getCurrent() {
        return this.current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

}
