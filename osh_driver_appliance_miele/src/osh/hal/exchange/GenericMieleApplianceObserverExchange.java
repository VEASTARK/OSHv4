package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.en50523.EN50523DeviceState;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALGenericApplianceIsCurrentlyControllable;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class GenericMieleApplianceObserverExchange
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails,
        IHALGasPowerDetails,
        IHALGenericApplianceDetails,
        IHALGenericApplianceIsCurrentlyControllable {

    // ### IHALElectricPowerDetails ###
    private int activePower;
    private int reactivePower;

    // ### IHALGasPowerDetails ###
    private int gasPower;

    // ### IHALGenericApplianceDetails ###
    private EN50523DeviceState en50523DeviceState;

    // ### IHALGenericApplianceIsCurrentlyControllable ###
    private boolean currentlyControllable;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GenericMieleApplianceObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp) {
        super(deviceID, timestamp);
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

    public int getGasPower() {
        return this.gasPower;
    }

    public void setGasPower(int gasPower) {
        this.gasPower = gasPower;
    }

    @Override
    public EN50523DeviceState getEN50523DeviceState() {
        return this.en50523DeviceState;
    }

    public void setEn50523DeviceState(EN50523DeviceState en50523DeviceState) {
        this.en50523DeviceState = en50523DeviceState;
    }

    @Override
    public boolean isCurrentlyControllable() {
        return this.currentlyControllable;
    }

    public void setCurrentlyControllable(boolean currentlyControllable) {
        this.currentlyControllable = currentlyControllable;
    }

}
