package osh.datatypes.registry.driver.details.chp;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class ChpDriverDetails extends StateExchange {

    // Heating request or power request? Or both?
    protected boolean powerGenerationRequest;
    protected boolean heatingRequest;

    // current power
    protected double currentElectricalPower;
    protected double currentThermalPower;

    // total energy
    protected double generatedElectricalWork;
    protected double generatedThermalWork;

    // priorities
    protected boolean electricalPowerPrioritizedControl;
    protected boolean thermalPowerPrioritizedControl;

    //
    protected int temperatureIn;
    protected int temperatureOut;


    /**
     * CONSTRUCTOR
     */
    public ChpDriverDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    public double getCurrentElectricalPower() {
        return this.currentElectricalPower;
    }

    public void setCurrentElectricalPower(double currentElectricalPower) {
        this.currentElectricalPower = currentElectricalPower;
    }

    public double getCurrentThermalPower() {
        return this.currentThermalPower;
    }

    public void setCurrentThermalPower(double currentThermalPower) {
        this.currentThermalPower = currentThermalPower;
    }

    public double getGeneratedElectricalWork() {
        return this.generatedElectricalWork;
    }

    public void setGeneratedElectricalWork(double generatedElectricalWork) {
        this.generatedElectricalWork = generatedElectricalWork;
    }

    public double getGeneratedThermalWork() {
        return this.generatedThermalWork;
    }

    public void setGeneratedThermalWork(double generatedThermalWork) {
        this.generatedThermalWork = generatedThermalWork;
    }

    public boolean getElectricalPowerPrioritizedControl() {
        return this.electricalPowerPrioritizedControl;
    }

    public void setElectricalPowerPrioritizedControl(boolean electricalPowerPrioritizedControl) {
        this.electricalPowerPrioritizedControl = electricalPowerPrioritizedControl;
    }

    public boolean getThermalPowerPrioritizedControl() {
        return this.thermalPowerPrioritizedControl;
    }

    public void setThermalPowerPrioritizedControl(boolean thermalPowerPrioritizedControl) {
        this.thermalPowerPrioritizedControl = thermalPowerPrioritizedControl;
    }

    public boolean isHeatingRequest() {
        return this.heatingRequest;
    }

    public void setHeatingRequest(boolean heatingRequest) {
        this.heatingRequest = heatingRequest;
    }

    public boolean isPowerGenerationRequest() {
        return this.powerGenerationRequest;
    }

    public void setPowerGenerationRequest(boolean powerGenerationRequest) {
        this.powerGenerationRequest = powerGenerationRequest;
    }

    public int getTemperatureIn() {
        return this.temperatureIn;
    }

    public void setTemperatureIn(int temperatureIn) {
        this.temperatureIn = temperatureIn;
    }

    public int getTemperatureOut() {
        return this.temperatureOut;
    }

    public void setTemperatureOut(int temperatureOut) {
        this.temperatureOut = temperatureOut;
    }

    @Override
    public String toString() {
        return "[currentElectricalPower=" + this.currentElectricalPower + "," +
                "currentThermalPower=" + this.currentThermalPower + "," +
                "generatedElectricalWork=" + this.generatedElectricalWork + "," +
                "generatedThermalWork=" + this.generatedThermalWork + "," +
                "electricalPowerPrioritizedControl=" + this.electricalPowerPrioritizedControl + "," +
                "thermalPowerPrioritizedControl=" + this.thermalPowerPrioritizedControl + "]";
    }

}
