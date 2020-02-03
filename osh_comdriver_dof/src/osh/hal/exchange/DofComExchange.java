package osh.hal.exchange;

import osh.cal.CALComExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class DofComExchange extends CALComExchange {

    private HashMap<UUID, Duration> device1stDegreeOfFreedom;
    private HashMap<UUID, Duration> device2ndDegreeOfFreedom;

    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public DofComExchange(UUID deviceID, ZonedDateTime timestamp) {
        super(deviceID, timestamp);

    }


    public HashMap<UUID, Duration> getDevice1stDegreeOfFreedom() {
        return this.device1stDegreeOfFreedom;
    }

    public void setDevice1stDegreeOfFreedom(
            HashMap<UUID, Duration> device1stDegreeOfFreedom) {
        this.device1stDegreeOfFreedom = new HashMap<>();

        for (Entry<UUID, Duration> e : device1stDegreeOfFreedom.entrySet()) {
            this.device1stDegreeOfFreedom.put(e.getKey(), e.getValue());
        }
    }

    public HashMap<UUID, Duration> getDevice2ndDegreeOfFreedom() {
        return this.device2ndDegreeOfFreedom;
    }

    public void setDevice2ndDegreeOfFreedom(
            HashMap<UUID, Duration> device2ndDegreeOfFreedom) {
        this.device2ndDegreeOfFreedom = new HashMap<>();

        for (Entry<UUID, Duration> e : device2ndDegreeOfFreedom.entrySet()) {
            this.device2ndDegreeOfFreedom.put(e.getKey(), e.getValue());
        }
    }


}
