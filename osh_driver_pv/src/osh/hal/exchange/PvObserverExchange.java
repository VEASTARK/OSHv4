package osh.hal.exchange;

import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.exchange.HALObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricCurrentDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricVoltageDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;

import java.util.UUID;

/**
 * @author Ingo Mauser, verpfuscht von sko & mmae
 */
public class PvObserverExchange
        extends HALObserverExchange
        implements IHALElectricCurrentDetails,
        IHALElectricalPowerDetails,
        IHALElectricVoltageDetails {

    // ### IHALElectricCurrentDetails ###
    private double current;

    // ### IHALElectricPowerDetails ###
    private int activePower;
    private int reactivePower;

    // ### IHALElectricVoltageDetails ###
    private double voltage;
    // ### for optimization
    private int rescheduleAfter;
    private long newIppAfter;
    private int triggerIppIfDeltaPBigger;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR 1
     */
    public PvObserverExchange(UUID deviceID, Long timestamp) {
        super(deviceID, timestamp);
    }

    /**
     * CONSTRUCTOR 2
     */
    public PvObserverExchange(
            UUID deviceID,
            Long timestamp,
            int activePower,
            int reactivePower,
            double voltage,

            int rescheduleAfter,
            long newIppAfter,
            int triggerIppIfDeltaPBigger,
            LoadProfileCompressionTypes compressionType,
            int compressionValue
    ) {
        super(deviceID, timestamp);

        this.activePower = activePower;
        this.reactivePower = reactivePower;

        this.rescheduleAfter = rescheduleAfter;
        this.newIppAfter = newIppAfter;
        this.triggerIppIfDeltaPBigger = triggerIppIfDeltaPBigger;

        this.compressionType = compressionType;
        this.compressionValue = compressionValue;

    }

    public int getRescheduleAfter() {
        return this.rescheduleAfter;
    }

    public void setRescheduleAfter(int rescheduleAfter) {
        this.rescheduleAfter = rescheduleAfter;
    }

    public long getNewIppAfter() {
        return this.newIppAfter;
    }

    public void setNewIppAfter(long newIppAfter) {
        this.newIppAfter = newIppAfter;
    }

    public int getTriggerIppIfDeltaPBigger() {
        return this.triggerIppIfDeltaPBigger;
    }

    public void setTriggerIppIfDeltaPBigger(int triggerIppIfDeltaPBigger) {
        this.triggerIppIfDeltaPBigger = triggerIppIfDeltaPBigger;
    }

    public LoadProfileCompressionTypes getCompressionType() {
        return this.compressionType;
    }

    public void setCompressionType(LoadProfileCompressionTypes compressionType) {
        this.compressionType = compressionType;
    }

    public int getCompressionValue() {
        return this.compressionValue;
    }

    public void setCompressionValue(int compressionValue) {
        this.compressionValue = compressionValue;
    }

    public double getVoltage() {
        return this.voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return this.current;
    }

    public void setCurrent(double current) {
        this.current = current;
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

}
