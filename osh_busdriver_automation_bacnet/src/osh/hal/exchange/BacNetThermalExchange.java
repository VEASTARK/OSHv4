package osh.hal.exchange;

import osh.datatypes.hal.interfaces.ITemperatureDetails;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.util.HashMap;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class BacNetThermalExchange
        extends HALDeviceObserverExchange
        implements ITemperatureDetails {

    private TemperatureDetails temperatureDetails;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID  unique identifier for this element
     * @param timestamp timestamp of this exchange
     */
    public BacNetThermalExchange(UUID deviceID, Long timestamp) {
        super(deviceID, timestamp);
        this.temperatureDetails = new TemperatureDetails(deviceID, timestamp);
    }

    public double getTemperature() {
        return this.temperatureDetails.getTemperature();
    }

    public void setTemperature(double temperature) {
        this.temperatureDetails.setTemperature(temperature);
    }


    public HashMap<String, Double> getAuxiliaryTemperatures() {
        return this.temperatureDetails.getAuxiliaryTemperatures();
    }

    public void setAuxiliaryTemperatures(HashMap<String, Double> auxiliaryTemperatures) {
        this.temperatureDetails.setAuxiliaryTemperatures(auxiliaryTemperatures);
    }

    @Override
    public String toString() {
        return this.temperatureDetails.toString();
    }

    public TemperatureDetails getTemperatureDetails() {
        return this.temperatureDetails;
    }

    public void setTemperatureDetails(TemperatureDetails temperatureDetails) {
        this.temperatureDetails = temperatureDetails;
    }
}
