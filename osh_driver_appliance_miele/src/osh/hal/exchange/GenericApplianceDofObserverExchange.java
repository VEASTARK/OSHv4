package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

public class GenericApplianceDofObserverExchange extends HALDeviceObserverExchange {

    private Duration device1stDegreeOfFreedom;
    private Duration device2ndDegreeOfFreedom;

    public GenericApplianceDofObserverExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);
    }

    public Duration getDevice1stDegreeOfFreedom() {
        return this.device1stDegreeOfFreedom;
    }

    public void setDevice1stDegreeOfFreedom(Duration device1stDegreeOfFreedom) {
        this.device1stDegreeOfFreedom = device1stDegreeOfFreedom;
    }

    public Duration getDevice2ndDegreeOfFreedom() {
        return this.device2ndDegreeOfFreedom;
    }

    public void setDevice2ndDegreeOfFreedom(Duration device2ndDegreeOfFreedom) {
        this.device2ndDegreeOfFreedom = device2ndDegreeOfFreedom;
    }

    @Override
    public GenericApplianceDofObserverExchange clone() {
        GenericApplianceDofObserverExchange cloned = new GenericApplianceDofObserverExchange(this.getDeviceID(), this.getTimestamp());

        cloned.device1stDegreeOfFreedom = this.device1stDegreeOfFreedom;
        cloned.device2ndDegreeOfFreedom = this.device2ndDegreeOfFreedom;
        return cloned;
    }

}
