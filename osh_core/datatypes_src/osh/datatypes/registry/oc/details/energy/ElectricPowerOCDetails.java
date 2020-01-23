package osh.datatypes.registry.oc.details.energy;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ElectricPowerOCDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 827260439992008634L;
    protected int activePower;
    protected int reactivePower;
    public ElectricPowerOCDetails(UUID sender, ZonedDateTime timestamp, int activePower,
                                  int reactivePower) {
        super(sender, timestamp);
        this.activePower = activePower;
        this.reactivePower = reactivePower;
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public ElectricPowerOCDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    public int getActivePower() {
        return this.activePower;
    }

    public void setActivePower(int activePower) {
        this.activePower = activePower;
    }


    public int getReactivePower() {
        return this.reactivePower;
    }

    public void setReactivePower(int reactivePower) {
        this.reactivePower = reactivePower;
    }

}
