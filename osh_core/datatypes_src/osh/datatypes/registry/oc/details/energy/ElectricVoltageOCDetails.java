package osh.datatypes.registry.oc.details.energy;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ElectricVoltageOCDetails extends StateExchange {

    protected double voltage;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public ElectricVoltageOCDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    public double getVoltage() {
        return this.voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

}
