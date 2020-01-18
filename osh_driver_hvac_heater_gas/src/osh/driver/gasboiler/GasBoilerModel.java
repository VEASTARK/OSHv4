package osh.driver.gasboiler;

import java.io.Serializable;

/**
 * @author Ingo Mauser
 */
public class GasBoilerModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6693604390224543213L;
    private final int TYPICAL_ACTIVE_POWER_ON;
    private final int TYPICAL_ACTIVE_POWER_OFF;
    private final int TYPICAL_REACTIVE_POWER_ON;
    private final int TYPICAL_REACTIVE_POWER_OFF;
    private final int MAX_HOT_WATER_POWER;
    private final int MAX_GAS_POWER;

    private int currentHotWaterPower;
    private int currentGasPower;


    /**
     * CONSTRUCTOR
     */
    public GasBoilerModel(
            int MAX_HOT_WATER_POWER,
            int MAX_GAS_POWER,
            int activePowerOn,
            int activePowerOff,
            int reactivePowerOn,
            int reactivePowerOff,
            boolean currentState) {
        this.MAX_HOT_WATER_POWER = MAX_HOT_WATER_POWER;
        this.MAX_GAS_POWER = MAX_GAS_POWER;
        this.TYPICAL_ACTIVE_POWER_ON = activePowerOn;
        this.TYPICAL_ACTIVE_POWER_OFF = activePowerOff;
        this.TYPICAL_REACTIVE_POWER_ON = reactivePowerOn;
        this.TYPICAL_REACTIVE_POWER_OFF = reactivePowerOff;

        if (currentState) {
            this.switchOn();
        }
    }

    protected GasBoilerModel() {
        this.MAX_HOT_WATER_POWER = 0;
        this.MAX_GAS_POWER = 0;
        this.TYPICAL_ACTIVE_POWER_ON = 0;
        this.TYPICAL_ACTIVE_POWER_OFF = 0;
        this.TYPICAL_REACTIVE_POWER_ON = 0;
        this.TYPICAL_REACTIVE_POWER_OFF = 0;
    }


    public int getHotWaterPower() {
        return this.currentHotWaterPower;
    }

    public int getGasPower() {
        return this.currentGasPower;
    }

    public void switchOn() {
        this.currentHotWaterPower = this.MAX_HOT_WATER_POWER;
        this.currentGasPower = this.MAX_GAS_POWER;
    }

    public void switchOff() {
        this.currentHotWaterPower = 0;
        this.currentGasPower = 0;
    }

    public boolean isOn() {
        return (this.currentHotWaterPower > 0);
    }

    public int getActivePower() {
        return (this.isOn() ? this.TYPICAL_ACTIVE_POWER_ON : this.TYPICAL_ACTIVE_POWER_OFF);
    }

    public int getReactivePower() {
        return (this.isOn() ? this.TYPICAL_REACTIVE_POWER_ON : this.TYPICAL_REACTIVE_POWER_OFF);
    }
}
