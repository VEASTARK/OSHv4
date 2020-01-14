package osh.hal.exchange;

import osh.eal.hal.exchange.HALDeviceObserverExchange;

import java.util.UUID;

public class GenericApplianceDofObserverExchange extends HALDeviceObserverExchange {

    private Integer device1stDegreeOfFreedom;
    private Integer device2ndDegreeOfFreedom;

    public GenericApplianceDofObserverExchange(UUID deviceID, Long timestamp) {
        super(deviceID, timestamp);
    }

    public Integer getDevice1stDegreeOfFreedom() {
        return this.device1stDegreeOfFreedom;
    }

    public void setDevice1stDegreeOfFreedom(Integer device1stDegreeOfFreedom) {
        this.device1stDegreeOfFreedom = device1stDegreeOfFreedom;
    }

    public Integer getDevice2ndDegreeOfFreedom() {
        return this.device2ndDegreeOfFreedom;
    }

    public void setDevice2ndDegreeOfFreedom(Integer device2ndDegreeOfFreedom) {
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
