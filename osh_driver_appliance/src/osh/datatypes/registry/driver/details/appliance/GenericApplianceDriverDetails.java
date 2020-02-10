package osh.datatypes.registry.driver.details.appliance;

import osh.datatypes.registry.StateExchange;
import osh.en50523.EN50523DeviceState;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Generic Appliance driver details
 * (communication of device state)
 *
 * @author Kaibin Bao, Ingo Mauser
 */

public class GenericApplianceDriverDetails extends StateExchange {

    private EN50523DeviceState state;
    private String stateTextDE;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public GenericApplianceDriverDetails(
            UUID sender,
            ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    // ### GETTERS and SETTERS ###

    public EN50523DeviceState getState() {
        return this.state;
    }

    public void setState(EN50523DeviceState state) {
        this.state = state;
    }

    public String getStateTextDE() {
        return this.stateTextDE;
    }

    public void setStateTextDE(String stateTextDE) {
        this.stateTextDE = stateTextDE;
    }

    // ### OTHER STUFF ###

    @Override
    public GenericApplianceDriverDetails clone() {
        GenericApplianceDriverDetails _new = new GenericApplianceDriverDetails(this.getSender(), this.getTimestamp());
        _new.state = this.state;
        _new.stateTextDE = this.stateTextDE + "";
        return _new;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GenericApplianceDriverDetails)) {
            return false;
        }

        GenericApplianceDriverDetails other = (GenericApplianceDriverDetails) obj;

        if (this.state == null) {
            return other.state == null;
        } else return this.state == other.state;

        //TODO details are currently not relevant
        /*
        if(this.programExtras == null) {
            if(other.programExtras != null)
                return false;
            else
                return true;
        }

        return (this.programExtras.equals(other.programExtras)); */
    }

    @Override
    public String toString() {
        return "ApplianceState: " + ((this.state == null) ? "null" : this.state.name());
    }
}
