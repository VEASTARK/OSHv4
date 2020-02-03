package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.UUID;


/**
 * @author ???
 */
public class ScheduledApplianceUIExchange extends CALComExchange {

    private ArrayList<ExpectedStartTimeExchange> currentApplianceSchedules;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public ScheduledApplianceUIExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    /**
     * @return the currentApplianceSchedules
     */
    public ArrayList<ExpectedStartTimeExchange> getCurrentApplianceSchedules() {
        return this.currentApplianceSchedules;
    }

    /**
     * @param currentApplianceSchedules the currentApplianceSchedules to set
     */
    public void setCurrentApplianceSchedules(
            ArrayList<ExpectedStartTimeExchange> currentApplianceSchedules) {
        this.currentApplianceSchedules = currentApplianceSchedules;
    }
}
