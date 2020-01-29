package osh.hal.exchange;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Jan Mueller
 */
public class BatteryStorageOX
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails {

    private final int activePower;
    private final int reactivePower;

    private final double batteryStateOfCharge;
    private double batteryStateOfHealth;
    private final int batteryStandingLoss;
    private final int batteryMinChargingState;
    private final int batteryMaxChargingState;
    private final int batteryMinChargePower;
    private final int batteryMinDischargePower;
    private final int batteryMaxChargePower;
    private final int inverterMinComplexPower;
    private final int inverterMaxComplexPower;
    private final int inverterMaxPower;
    private final int inverterMinPower;
    private final int batteryMaxDischargePower;

    private final int rescheduleAfter;
    private final long newIppAfter;
    private final int triggerIppIfDeltaSoCBigger;
    private final LoadProfileCompressionTypes compressionType;
    private final int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public BatteryStorageOX(
            UUID deviceID,
            ZonedDateTime timestamp,

            int activePower,
            int reactivePower,

            double batteryStateOfCharge,
            double batteryStateOfHealth,
            int batteryStandingLoss,
            int batteryMinChargingState,
            int batteryMaxChargingState,
            int batteryMinChargePower,
            int batteryMaxChargePower,
            int batteryMinDischargePower,
            int batteryMaxDischargePower,
            int inverterMinComplexPower,
            int inverterMaxComplexPower,
            int inverterMinPower,
            int inverterMaxPower,
            int rescheduleAfter,
            long newIppAfter,
            int triggerIppIfDeltaSoCBigger,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(deviceID, timestamp);


        this.activePower = activePower;
        this.reactivePower = reactivePower;

        this.batteryStateOfCharge = batteryStateOfCharge;

        this.batteryStandingLoss = batteryStandingLoss;

        this.batteryMinChargingState = batteryMinChargingState;
        this.batteryMaxChargingState = batteryMaxChargingState;

        this.batteryMinChargePower = batteryMinChargePower;
        this.batteryMaxChargePower = batteryMaxChargePower;

        this.batteryMinDischargePower = batteryMinDischargePower;
        this.batteryMaxDischargePower = batteryMaxDischargePower;

        this.inverterMinComplexPower = inverterMinComplexPower;
        this.inverterMaxComplexPower = inverterMaxComplexPower;

        this.inverterMaxPower = inverterMaxPower;
        this.inverterMinPower = inverterMinPower;

        this.rescheduleAfter = rescheduleAfter;
        this.newIppAfter = newIppAfter;
        this.triggerIppIfDeltaSoCBigger = triggerIppIfDeltaSoCBigger;

        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }

    @Override
    public int getActivePower() {
        return this.activePower;
    }

    @Override
    public int getReactivePower() {
        return this.reactivePower;
    }


    public double getBatteryStateOfCharge() {
        return this.batteryStateOfCharge;
    }

    public double getBatteryStateOfHealth() {
        return this.batteryStateOfHealth;
    }

    public int getBatteryStandingLoss() {
        return this.batteryStandingLoss;
    }

    public int getBatteryMinChargingState() {
        return this.batteryMinChargingState;
    }

    public int getBatteryMaxChargingState() {
        return this.batteryMaxChargingState;
    }

    public int getBatteryMinChargePower() {
        return this.batteryMinChargePower;
    }

    public int getBatteryMinDischargePower() {
        return this.batteryMinDischargePower;
    }

    public int getBatteryMaxChargePower() {
        return this.batteryMaxChargePower;
    }

    public int getInverterMinComplexPower() {
        return this.inverterMinComplexPower;
    }

    public int getInverterMaxComplexPower() {
        return this.inverterMaxComplexPower;
    }

    public int getInverterMaxPower() {
        return this.inverterMaxPower;
    }

    public int getInverterMinPower() {
        return this.inverterMinPower;
    }

    public int getBatteryMaxDischargePower() {
        return this.batteryMaxDischargePower;
    }

    public int getRescheduleAfter() {
        return this.rescheduleAfter;
    }

    public long getNewIppAfter() {
        return this.newIppAfter;
    }

    public int getTriggerIppIfDeltaSoCBigger() {
        return this.triggerIppIfDeltaSoCBigger;
    }

    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }

    public int getCompressionValue() {
        return this.compressionValue;
    }

}
