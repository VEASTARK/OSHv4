package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class GasBoilerObserverExchange
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails, IHALHotWaterPowerDetails, IHALGasPowerDetails {

    private final double minTemperature;
    private final double maxTemperature;
    private final double hotWaterTemperature;

    private final boolean currentState;

    private final int hotWaterPower;
    private final int activePower;
    private int reactivePower;
    private final int gasPower;

    private final int maxHotWaterPower;
    private final int maxGasPower;

    private final int typicalActivePowerOn;
    private final int typicalActivePowerOff;
    private final int typicalReactivePowerOn;
    private final int typicalReactivePowerOff;

    private final Duration newIppAfter;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public GasBoilerObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp,

            double minTemperature,
            double maxTemperature,
            double waterTemperature,

            boolean currentState,

            int activePower,
            int reactivePower,
            int gasPower,
            int hotWaterPower,
            int maxHotWaterPower,
            int maxGasPower,
            int typicalActivePowerOn,
            int typicalActivePowerOff,
            int typicalReactivePowerOn,
            int typicalReactivePowerOff,
            Duration newIppAfter) {

        super(deviceID, timestamp);

        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.hotWaterTemperature = waterTemperature;

        this.currentState = currentState;

        this.activePower = activePower;
        this.hotWaterPower = hotWaterPower;
        this.gasPower = gasPower;

        this.maxHotWaterPower = maxHotWaterPower;
        this.maxGasPower = maxGasPower;

        this.newIppAfter = newIppAfter;
        this.typicalActivePowerOn = typicalActivePowerOn;
        this.typicalActivePowerOff = typicalActivePowerOff;
        this.typicalReactivePowerOn = typicalReactivePowerOn;
        this.typicalReactivePowerOff = typicalReactivePowerOff;
    }

    public double getMinTemperature() {
        return this.minTemperature;
    }

    public double getMaxTemperature() {
        return this.maxTemperature;
    }

    public boolean getCurrentState() {
        return this.currentState;
    }

    @Override
    public int getHotWaterPower() {
        return this.hotWaterPower;
    }

    @Override
    public int getActivePower() {
        return this.activePower;
    }

    @Override
    public int getReactivePower() {
        return this.reactivePower;
    }

    @Override
    public int getGasPower() {
        return this.gasPower;
    }

    public int getMaxHotWaterPower() {
        return this.maxHotWaterPower;
    }

    public int getMaxGasPower() {
        return this.maxGasPower;
    }

    public int getTypicalActivePowerOn() {
        return this.typicalActivePowerOn;
    }

    public int getTypicalActivePowerOff() {
        return this.typicalActivePowerOff;
    }

    public int getTypicalReactivePowerOn() {
        return this.typicalReactivePowerOn;
    }

    public int getTypicalReactivePowerOff() {
        return this.typicalReactivePowerOff;
    }

    public Duration getNewIppAfter() {
        return this.newIppAfter;
    }

    @Override
    public double getHotWaterTemperature() {
        return this.hotWaterTemperature;
    }

}
