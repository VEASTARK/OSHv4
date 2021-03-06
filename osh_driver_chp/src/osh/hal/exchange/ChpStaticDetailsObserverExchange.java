package osh.hal.exchange;

import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.hal.interfaces.chp.IHALChpStaticDetails;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Sebastian Kramer
 */
public class ChpStaticDetailsObserverExchange extends HALDeviceObserverExchange
        implements IHALChpStaticDetails {

    // ### IHALChpStaticDetails ###
    private int minRuntime;
    private ChpOperationMode operationMode;
    private int typicalActivePower;
    private int typicalReactivePower;
    private int typicalGasPower;
    private int typicalThermalPower;
    private Duration rescheduleAfter;
    private Duration newIPPAfter;
    private int relativeHorizonIPP;
    private long timePerSlot;
    private int bitsPerSlot;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double forcedOnHysteresis;
    private UUID hotWaterTankUuid;

    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;

    public ChpStaticDetailsObserverExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    @Override
    public int getMinRuntime() {
        return this.minRuntime;
    }

    public void setMinRuntime(int minRuntime) {
        this.minRuntime = minRuntime;
    }

    public ChpOperationMode getOperationMode() {
        return this.operationMode;
    }

    public void setOperationMode(ChpOperationMode operationMode) {
        this.operationMode = operationMode;
    }

    @Override
    public int getTypicalActivePower() {
        return this.typicalActivePower;
    }

    public void setTypicalActivePower(int typicalActivePower) {
        this.typicalActivePower = typicalActivePower;
    }

    public int getTypicalReactivePower() {
        return this.typicalReactivePower;
    }

    public void setTypicalReactivePower(int typicalReactivePower) {
        this.typicalReactivePower = typicalReactivePower;
    }

    @Override
    public int getTypicalGasPower() {
        return this.typicalGasPower;
    }

    public void setTypicalGasPower(int typicalGasPower) {
        this.typicalGasPower = typicalGasPower;
    }

    @Override
    public int getTypicalThermalPower() {
        return this.typicalThermalPower;
    }

    public void setTypicalThermalPower(int typicalThermalPower) {
        this.typicalThermalPower = typicalThermalPower;
    }

    public Duration getRescheduleAfter() {
        return this.rescheduleAfter;
    }

    public void setRescheduleAfter(Duration rescheduleAfter) {
        this.rescheduleAfter = rescheduleAfter;
    }

    public Duration getNewIPPAfter() {
        return this.newIPPAfter;
    }

    public void setNewIPPAfter(Duration newIPPAfter) {
        this.newIPPAfter = newIPPAfter;
    }

    public int getRelativeHorizonIPP() {
        return this.relativeHorizonIPP;
    }

    public void setRelativeHorizonIPP(int relativeHorizonIPP) {
        this.relativeHorizonIPP = relativeHorizonIPP;
    }

    public long getTimePerSlot() {
        return this.timePerSlot;
    }

    public void setTimePerSlot(long timePerSlot) {
        this.timePerSlot = timePerSlot;
    }

    public int getBitsPerSlot() {
        return this.bitsPerSlot;
    }

    public void setBitsPerSlot(int bitsPerSlot) {
        this.bitsPerSlot = bitsPerSlot;
    }

    public double getCurrentHotWaterStorageMinTemp() {
        return this.currentHotWaterStorageMinTemp;
    }

    public void setCurrentHotWaterStorageMinTemp(double currentHotWaterStorageMinTemp) {
        this.currentHotWaterStorageMinTemp = currentHotWaterStorageMinTemp;
    }

    public double getCurrentHotWaterStorageMaxTemp() {
        return this.currentHotWaterStorageMaxTemp;
    }

    public void setCurrentHotWaterStorageMaxTemp(double currentHotWaterStorageMaxTemp) {
        this.currentHotWaterStorageMaxTemp = currentHotWaterStorageMaxTemp;
    }

    public double getForcedOnHysteresis() {
        return this.forcedOnHysteresis;
    }

    public void setForcedOnHysteresis(double forcedOnHysteresis) {
        this.forcedOnHysteresis = forcedOnHysteresis;
    }

    public UUID getHotWaterTankUuid() {
        return this.hotWaterTankUuid;
    }

    public void setHotWaterTankUuid(UUID hotWaterTankUuid) {
        this.hotWaterTankUuid = hotWaterTankUuid;
    }

    public double getFixedCostPerStart() {
        return this.fixedCostPerStart;
    }

    public void setFixedCostPerStart(double fixedCostPerStart) {
        this.fixedCostPerStart = fixedCostPerStart;
    }

    public double getForcedOnOffStepMultiplier() {
        return this.forcedOnOffStepMultiplier;
    }

    public void setForcedOnOffStepMultiplier(double forcedOnOffStepMultiplier) {
        this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
    }

    public int getForcedOffAdditionalCost() {
        return this.forcedOffAdditionalCost;
    }

    public void setForcedOffAdditionalCost(int forcedOffAdditionalCost) {
        this.forcedOffAdditionalCost = forcedOffAdditionalCost;
    }

    public double getChpOnCervisiaStepSizeMultiplier() {
        return this.chpOnCervisiaStepSizeMultiplier;
    }

    public void setChpOnCervisiaStepSizeMultiplier(double chpOnCervisiaStepSizeMultiplier) {
        this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;
    }
}
