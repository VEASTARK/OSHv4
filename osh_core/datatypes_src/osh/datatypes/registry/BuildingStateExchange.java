package osh.datatypes.registry;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Simone Droll
 */

public class BuildingStateExchange extends StateExchange {

    private ZonedDateTime currentTime;
    private double currentActivePower;
    private double currentActivePowerConsumption;
    private double currentActivePowerChp;
    private double currentActivePowerChpFeedIn;
    private double currentActivePowerChpAutoConsumption;
    private double currentActivePowerPv;
    private double currentActivePowerPvFeedIn;
    private double currentActivePowerPvAutoConsumption;
    private double currentActivePowerBatteryCharging;
    private double currentActivePowerBatteryDischarging;
    private double currentActivePowerBatteryAutoConsumption;
    private double currentActivePowerBatteryFeedIn;
    private double currentActivePowerExternal;
    private double currentReactivePowerExternal;
    private double currentGasPowerExternal;

    public BuildingStateExchange(BuildingStateExchange other) {
        super(other.getSender(), other.getTimestamp());


        this.currentTime = other.currentTime;
        this.currentActivePower = other.currentActivePower;
        this.currentActivePowerConsumption = other.currentActivePowerConsumption;
        this.currentActivePowerChp = other.currentActivePowerChp;
        this.currentActivePowerChpFeedIn = other.currentActivePowerChpFeedIn;
        this.currentActivePowerChpAutoConsumption = other.currentActivePowerChpAutoConsumption;
        this.currentActivePowerPv = other.currentActivePowerPv;
        this.currentActivePowerPvFeedIn = other.currentActivePowerPvFeedIn;
        this.currentActivePowerPvAutoConsumption = other.currentActivePowerPvAutoConsumption;
        this.currentActivePowerBatteryCharging = other.currentActivePowerBatteryCharging;
        this.currentActivePowerBatteryDischarging = other.currentActivePowerBatteryDischarging;
        this.currentActivePowerBatteryAutoConsumption = other.currentActivePowerBatteryAutoConsumption;
        this.currentActivePowerBatteryFeedIn = other.currentActivePowerBatteryFeedIn;
        this.currentActivePowerExternal = other.currentActivePowerExternal;
        this.currentReactivePowerExternal = other.currentReactivePowerExternal;
        this.currentGasPowerExternal = other.currentGasPowerExternal;
    }

    public BuildingStateExchange(UUID sender, ZonedDateTime currentTime, ZonedDateTime timestamp, double currentActivePower,
                                 double currentActivePowerConsumption, double currentActivePowerChp, double currentActivePowerChpFeedIn,
                                 double currentActivePowerChpAutoConsumption, double currentActivePowerPv, double currentActivePowerPvFeedIn,
                                 double currentActivePowerPvAutoConsumption, double currentActivePowerBatteryCharging, double currentActivePowerBatteryDischarging,
                                 double currentActivePowerBatteryAutoConsumption, double currentActivePowerBatteryFeedIn,
                                 double currentActivePowerExternal, double currentReactivePowerExternal, double currentGasPowerExternal) {

        super(sender, timestamp);

        this.currentTime = currentTime;
        this.currentActivePower = currentActivePower;
        this.currentActivePowerConsumption = currentActivePowerConsumption;
        this.currentActivePowerChp = currentActivePowerChp;
        this.currentActivePowerChpFeedIn = currentActivePowerChpFeedIn;
        this.currentActivePowerChpAutoConsumption = currentActivePowerChpAutoConsumption;
        this.currentActivePowerPv = currentActivePowerPv;
        this.currentActivePowerPvFeedIn = currentActivePowerPvFeedIn;
        this.currentActivePowerPvAutoConsumption = currentActivePowerPvAutoConsumption;
        this.currentActivePowerBatteryCharging = currentActivePowerBatteryCharging;
        this.currentActivePowerBatteryDischarging = currentActivePowerBatteryDischarging;
        this.currentActivePowerBatteryAutoConsumption = currentActivePowerBatteryAutoConsumption;
        this.currentActivePowerBatteryFeedIn = currentActivePowerBatteryFeedIn;
        this.currentActivePowerExternal = currentActivePowerExternal;
        this.currentReactivePowerExternal = currentReactivePowerExternal;
        this.currentGasPowerExternal = currentGasPowerExternal;
        System.out.println("HHSE erzeugt");
    }

    public double getCurrentActivePowerConsumption() {
        return this.currentActivePowerConsumption;
    }

    public void setCurrentActivePowerConsumption(double currentActivePowerConsumption) {
        this.currentActivePowerConsumption = currentActivePowerConsumption;
    }

    public double getCurrentActivePowerChpAutoConsumption() {
        return this.currentActivePowerChpAutoConsumption;
    }

    public void setCurrentActivePowerChpAutoConsumption(double currentActivePowerChpAutoConsumption) {
        this.currentActivePowerChpAutoConsumption = currentActivePowerChpAutoConsumption;
    }

    public double getCurrentActivePowerPvAutoConsumption() {
        return this.currentActivePowerPvAutoConsumption;
    }

    public void setCurrentActivePowerPvAutoConsumption(double currentActivePowerPvAutoConsumption) {
        this.currentActivePowerPvAutoConsumption = currentActivePowerPvAutoConsumption;
    }

    public ZonedDateTime getCurrentTime() {
        return this.currentTime;
    }

    public void setCurrentTime(ZonedDateTime currentTime) {
        this.currentTime = currentTime;
    }

    public double getCurrentActivePower() {
        return this.currentActivePower;
    }

    public void setCurrentActivePower(double currentActivePower) {
        this.currentActivePower = currentActivePower;
    }

    public double getCurrentActivePowerChp() {
        return this.currentActivePowerChp;
    }

    public void setCurrentActivePowerChp(double currentActivePowerChp) {
        this.currentActivePowerChp = currentActivePowerChp;
    }

    public double getCurrentActivePowerChpFeedIn() {
        return this.currentActivePowerChpFeedIn;
    }

    public void setCurrentActivePowerChpFeedIn(double currentActivePowerChpFeedIn) {
        this.currentActivePowerChpFeedIn = currentActivePowerChpFeedIn;
    }

    public double getCurrentActivePowerPv() {
        return this.currentActivePowerPv;
    }

    public void setCurrentActivePowerPv(double currentActivePowerPv) {
        this.currentActivePowerPv = currentActivePowerPv;
    }

    public double getCurrentActivePowerPvFeedIn() {
        return this.currentActivePowerPvFeedIn;
    }

    public void setCurrentActivePowerPvFeedIn(double currentActivePowerPvFeedIn) {
        this.currentActivePowerPvFeedIn = currentActivePowerPvFeedIn;
    }

    public double getCurrentActivePowerBatteryCharging() {
        return this.currentActivePowerBatteryCharging;
    }

    public void setCurrentActivePowerBatteryCharging(double currentActivePowerBatteryCharging) {
        this.currentActivePowerBatteryCharging = currentActivePowerBatteryCharging;
    }

    public double getCurrentActivePowerBatteryDischarging() {
        return this.currentActivePowerBatteryDischarging;
    }

    public void setCurrentActivePowerBatteryDischarging(double currentActivePowerBatteryDischarging) {
        this.currentActivePowerBatteryDischarging = currentActivePowerBatteryDischarging;
    }

    public double getCurrentActivePowerBatteryAutoConsumption() {
        return this.currentActivePowerBatteryAutoConsumption;
    }

    public void setCurrentActivePowerBatteryAutoConsumption(double currentActivePowerBatteryAutoConsumption) {
        this.currentActivePowerBatteryAutoConsumption = currentActivePowerBatteryAutoConsumption;
    }

    public double getCurrentActivePowerBatteryFeedIn() {
        return this.currentActivePowerBatteryFeedIn;
    }

    public void setCurrentActivePowerBatteryFeedIn(double currentActivePowerBatteryFeedIn) {
        this.currentActivePowerBatteryFeedIn = currentActivePowerBatteryFeedIn;
    }

    public double getCurrentActivePowerExternal() {
        return this.currentActivePowerExternal;
    }

    public void setCurrentActivePowerExternal(double currentActivePowerExternal) {
        this.currentActivePowerExternal = currentActivePowerExternal;
    }

    public double getCurrentReactivePowerExternal() {
        return this.currentReactivePowerExternal;
    }

    public void setCurrentReactivePowerExternal(double currentReactivePowerExternal) {
        this.currentReactivePowerExternal = currentReactivePowerExternal;
    }

    public double getCurrentGasPowerExternal() {
        return this.currentGasPowerExternal;
    }

    public void setCurrentGasPowerExternal(double currentGasPowerExternal) {
        this.currentGasPowerExternal = currentGasPowerExternal;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + ": Sender " + this.getSender() + ", time: " + this.getTimestamp();
    }

    public BuildingStateExchange clone() {

        return new BuildingStateExchange(this);
    }

}
