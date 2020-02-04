package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALHotWaterPowerDetails;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class SmartHeaterOX
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails, IHALHotWaterPowerDetails {

    private final int temperatureSetting;
    private int waterTemperature;

    private final int currentState;

    private final int hotWaterPower;
    private final int activePower;
    private final long[] timestampOfLastChangePerSubElement;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public SmartHeaterOX(
            UUID deviceID,
            ZonedDateTime timestamp,

            int temperatureSetting,
            int waterTemperature,

            int currentState,

            int activePower,
            int hotWaterPower,
            long[] timestampOfLastChangePerSubElement) {

        super(deviceID, timestamp);

        this.temperatureSetting = temperatureSetting;
        this.currentState = currentState;

        this.hotWaterPower = hotWaterPower;
        this.activePower = activePower;
        this.timestampOfLastChangePerSubElement = timestampOfLastChangePerSubElement;
    }

    public int getTemperatureSetting() {
        return this.temperatureSetting;
    }

    public int getCurrentState() {
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
        return 0;
    }

    public long[] getTimestampOfLastChangePerSubElement() {
        return Arrays.copyOf(this.timestampOfLastChangePerSubElement, this.timestampOfLastChangePerSubElement.length);
    }

    @Override
    public double getHotWaterTemperature() {
        return this.waterTemperature;
    }

}
