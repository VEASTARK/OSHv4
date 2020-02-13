package osh.mgmt.mox;

import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.driver.chp.ChpOperationMode;

import java.time.Duration;

/**
 * @author Ingo Mauser
 */
public class DachsChpMOX implements IModelOfObservationExchange {

    // current values
    private final double waterTemperature;
//	private INeededEnergy neededEnergy;

    private final boolean running;
    private final Duration remainingRunningTime;

    private final int activePower;
    private final int reactivePower;
    private final int thermalPower;
    private final int gasPower;

    // quasi static values
    private final ChpOperationMode operationMode;
    private final long timePerSlot;
    private final int bitsPerSlot;
    private final int typicalActivePower;
    private final int typicalReactivePower;
    private final int typicalGasPower;
    private final int typicalThermalPower;

    private final Duration rescheduleAfter;
    private final Duration newIPPAfter;
    private final int relativeHorizonIPP;
    private final double currentHotWaterStorageMinTemp;
    private final double currentHotWaterStorageMaxTemp;
    private final double forcedOnHysteresis;

    private final double fixedCostPerStart;
    private final double forcedOnOffStepMultiplier;
    private final int forcedOffAdditionalCost;
    private final double chpOnCervisiaStepSizeMultiplier;
    private int minRuntime;
    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public DachsChpMOX(double waterTemperature,
                       boolean running,
                       Duration remainingRunningTime,
                       int activePower,
                       int reactivePower,
                       int thermalPower,
                       int gasPower,
                       ChpOperationMode operationMode,
                       long timePerSlot,
                       int bitsPerSlot,
                       int typicalActivePower,
                       int typicalReactivePower,
                       int typicalGasPower,
                       int typicalThermalPower,
                       Duration rescheduleAfter,
                       Duration newIPPAfter,
                       int relativeHorizonIPP,
                       double currentHotWaterStorageMinTemp,
                       double currentHotWaterStorageMaxTemp,
                       double forcedOnHysteresis,
                       double fixedCostPerStart,
                       double forcedOnOffStepMultiplier,
                       int forcedOffAdditionalCost,
                       double chpOnCervisiaStepSizeMultiplier,
                       int minRunTime,
                       LoadProfileCompressionTypes compressionType,
                       int compressionValue) {
        super();

        this.waterTemperature = waterTemperature;
        this.running = running;
        this.remainingRunningTime = remainingRunningTime;
        this.activePower = activePower;
        this.reactivePower = reactivePower;
        this.thermalPower = thermalPower;
        this.gasPower = gasPower;

        this.operationMode = operationMode;
        this.timePerSlot = timePerSlot;
        this.bitsPerSlot = bitsPerSlot;
        this.typicalActivePower = typicalActivePower;
        this.typicalReactivePower = typicalReactivePower;
        this.typicalGasPower = typicalGasPower;
        this.typicalThermalPower = typicalThermalPower;

        this.rescheduleAfter = rescheduleAfter;
        this.newIPPAfter = newIPPAfter;
        this.relativeHorizonIPP = relativeHorizonIPP;
        this.currentHotWaterStorageMinTemp = currentHotWaterStorageMinTemp;
        this.currentHotWaterStorageMaxTemp = currentHotWaterStorageMaxTemp;
        this.forcedOnHysteresis = forcedOnHysteresis;

        this.fixedCostPerStart = fixedCostPerStart;
        this.forcedOnOffStepMultiplier = forcedOnOffStepMultiplier;
        this.forcedOffAdditionalCost = forcedOffAdditionalCost;
        this.chpOnCervisiaStepSizeMultiplier = chpOnCervisiaStepSizeMultiplier;
        this.minRuntime = minRunTime;

        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }


    public double getWaterTemperature() {
        return this.waterTemperature;
    }

    public boolean isRunning() {
        return this.running;
    }

    public Duration getRemainingRunningTime() {
        return this.remainingRunningTime;
    }

    public int getActivePower() {
        return this.activePower;
    }

    public int getReactivePower() {
        return this.reactivePower;
    }

    public int getThermalPower() {
        return this.thermalPower;
    }

    public int getGasPower() {
        return this.gasPower;
    }

    public int getTypicalActivePower() {
        return this.typicalActivePower;
    }

    public int getTypicalReactivePower() {
        return this.typicalReactivePower;
    }

    public int getTypicalGasPower() {
        return this.typicalGasPower;
    }

    public int getTypicalThermalPower() {
        return this.typicalThermalPower;
    }

    public ChpOperationMode getOperationMode() {
        return this.operationMode;
    }

    public long getTimePerSlot() {
        return this.timePerSlot;
    }

    public int getBitsPerSlot() {
        return this.bitsPerSlot;
    }

    public Duration getRescheduleAfter() {
        return this.rescheduleAfter;
    }


    public Duration getNewIPPAfter() {
        return this.newIPPAfter;
    }


    public int getRelativeHorizonIPP() {
        return this.relativeHorizonIPP;
    }

    public double getCurrentHotWaterStorageMinTemp() {
        return this.currentHotWaterStorageMinTemp;
    }


    public double getCurrentHotWaterStorageMaxTemp() {
        return this.currentHotWaterStorageMaxTemp;
    }


    public double getForcedOnHysteresis() {
        return this.forcedOnHysteresis;
    }


    public double getFixedCostPerStart() {
        return this.fixedCostPerStart;
    }


    public double getForcedOnOffStepMultiplier() {
        return this.forcedOnOffStepMultiplier;
    }


    public int getForcedOffAdditionalCost() {
        return this.forcedOffAdditionalCost;
    }


    public double getChpOnCervisiaStepSizeMultiplier() {
        return this.chpOnCervisiaStepSizeMultiplier;
    }


    public int getMinRuntime() {
        return this.minRuntime;
    }


    public void setMinRuntime(int minRuntime) {
        this.minRuntime = minRuntime;
    }


    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }


    public int getCompressionValue() {
        return this.compressionValue;
    }

//	public INeededEnergy getNeededEnergy() {
//		return neededEnergy;
//	}

}
