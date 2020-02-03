package osh.hal.exchange;

import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.thermal.IHALColdWaterPowerDetails;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class SpaceCoolingObserverExchange extends HALDeviceObserverExchange implements IHALColdWaterPowerDetails {

    private final ArrayList<ChillerCalendarDate> dates;
    private final Map<Long, Double> temperaturePrediction;
    private int coldWaterPower;


    public SpaceCoolingObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            ArrayList<ChillerCalendarDate> dates,
            Map<Long, Double> temperaturePrediction,
            int coldWaterPower) {
        super(deviceID, timestamp);

        this.dates = dates;
        this.temperaturePrediction = temperaturePrediction;
    }

    public ArrayList<ChillerCalendarDate> getDates() {
        return this.dates;
    }

    public Map<Long, Double> getTemperaturePrediction() {
        return this.temperaturePrediction;
    }

    @Override
    public int getColdWaterPower() {
        return this.coldWaterPower;
    }

    @Override
    public double getColdWaterTemperature() {
        return Double.NaN;
    }

}
