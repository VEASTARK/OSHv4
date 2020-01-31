package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;
import osh.hal.interfaces.chp.IHALChpDetails;
import osh.hal.interfaces.chp.IHALExtendedChpDetails;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ChpObserverExchange
        extends HALDeviceObserverExchange
        implements IHALChpDetails,
        IHALExtendedChpDetails,
        IHALElectricalPowerDetails,
        IHALHotWaterPowerDetails,
        IHALGasPowerDetails {

    // ### IHALChpDetails ###
    private boolean running;
    private boolean heatingRequest;
    private boolean electricityRequest;
    private Duration minRuntimeRemaining;
    private int minRuntime;

    // ### IHALExtendedChpDetails ###
    private double temperatureIn;
    private double temperatureOut;

    // ### IHALElectricPowerDetails ###
    private int activePower;
    private int reactivePower;

    // ### IHALThermalPowerDetails ###
    private int thermalPower;

    // ### IHALGasPowerDetails ###
    private int gasPower;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public ChpObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    @Override
    public boolean isHeatingRequest() {
        return this.heatingRequest;
    }

    public void setHeatingRequest(boolean heatingRequest) {
        this.heatingRequest = heatingRequest;
    }

    @Override
    public boolean isElectricityRequest() {
        return this.electricityRequest;
    }

    public void setElectricityRequest(boolean electricityRequest) {
        this.electricityRequest = electricityRequest;
    }

    @Override
    public Duration getMinRuntimeRemaining() {
        return this.minRuntimeRemaining;
    }

    public void setMinRuntimeRemaining(Duration minRuntimeRemaining) {
        this.minRuntimeRemaining = minRuntimeRemaining;
    }

    public int getMinRuntime() {
        return this.minRuntime;
    }

    public void setMinRuntime(int minRuntime) {
        this.minRuntime = minRuntime;
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

    @Override
    public int getHotWaterPower() {
        return this.thermalPower;
    }

    public void setThermalPower(int thermalPower) {
        this.thermalPower = thermalPower;
    }

    @Override
    public double getTemperatureIn() {
        return this.temperatureIn;
    }

    public void setTemperatureIn(double temperatureIn) {
        this.temperatureIn = temperatureIn;
    }

    @Override
    public double getTemperatureOut() {
        return this.temperatureOut;
    }

    public void setTemperatureOut(double temperatureOut) {
        this.temperatureOut = temperatureOut;
    }

    public int getGasPower() {
        return this.gasPower;
    }

    public void setGasPower(int gasPower) {
        this.gasPower = gasPower;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public double getHotWaterTemperature() {
        return this.temperatureOut;
    }
}
