package osh.hal.exchange;

import osh.driver.simulation.spacecooling.OutdoorTemperatures;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.hvac.IHALAdsorptionChillerDetails;
import osh.eal.hal.interfaces.thermal.IHALColdWaterPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ChillerObserverExchange
        extends HALDeviceObserverExchange
        implements IHALAdsorptionChillerDetails,
        IHALElectricalPowerDetails,
        IHALColdWaterPowerDetails,
        IHALHotWaterPowerDetails {

    // ### IHALAdsortionChillerDetails ###
    private final boolean running;
    private int minRuntimeRemaining;

    // ### IHALElectricPowerDetails ###
    private int activePower;
    private int reactivePower;

    // ### IHALColdWaterPowerDetails ###
    private int coldWaterPower;

    // ### IHALHotWaterPowerDetails ###
    private int hotWaterPower;

    //DIRTY HACK!
    private final OutdoorTemperatures outdoorTemperature;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public ChillerObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            boolean running,
            OutdoorTemperatures outdoorTemperature) {
        super(deviceID, timestamp);

        this.running = running;
        this.outdoorTemperature = outdoorTemperature;
    }

    @Override
    public int getMinRuntimeRemaining() {
        return this.minRuntimeRemaining;
    }

    public void setMinRuntimeRemaining(int minRuntimeRemaining) {
        this.minRuntimeRemaining = minRuntimeRemaining;
    }

    @Override
    public int getActivePower() {
        return this.activePower;
    }

    public void setActivePower(int activePower) {
        this.activePower = activePower;
    }

    @Override
    public int getReactivePower() {
        return this.reactivePower;
    }

    public void setReactivePower(int reactivePower) {
        this.reactivePower = reactivePower;
    }


    public int getColdWaterPower() {
        return this.coldWaterPower;
    }

    public void setColdWaterPower(int coldWaterPower) {
        this.coldWaterPower = coldWaterPower;
    }

    public int getHotWaterPower() {
        return this.hotWaterPower;
    }

    public void setHotWaterPower(int hotWaterPower) {
        this.hotWaterPower = hotWaterPower;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    public OutdoorTemperatures getOutdoorTemperature() {
        return this.outdoorTemperature;
    }

    @Override
    public double getHotWaterTemperature() {
        return Double.NaN;
    }

    @Override
    public double getColdWaterTemperature() {
        return Double.NaN;
    }
}
