package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.common.IHALSwitchDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;

import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class SmartPlugObserverExchange
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails,
        IHALSwitchDetails {

    //
    private boolean incompleteData;

    // ### IHALElectricPowerDetails ###
    private int activePower;
    private int reactivePower;

    // ### IHALSwitchDetails ###
    private boolean on;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public SmartPlugObserverExchange(UUID deviceID, Long timestamp) {
        super(deviceID, timestamp);
    }

    public boolean isIncompleteData() {
        return this.incompleteData;
    }

    public void setIncompleteData(boolean incompleteData) {
        this.incompleteData = incompleteData;
    }

    public boolean isOn() {
        return this.on;
    }


    public void setOn(boolean on) {
        this.on = on;
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
