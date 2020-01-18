package osh.hal.exchange;

import osh.datatypes.registry.StateExchange;
import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class HttpRestInteractionComManagerExchange
        extends HALControllerExchange {

    private StateExchange stateExchange;

    /**
     * @param deviceID
     * @param timestamp
     */
    public HttpRestInteractionComManagerExchange(UUID deviceID, Long timestamp, StateExchange stateExchange) {
        super(deviceID, timestamp);
        this.stateExchange = stateExchange;
    }

    public StateExchange getStateExchange() {
        return this.stateExchange;
    }

    public void setStateExchange(StateExchange stateExchange) {
        this.stateExchange = stateExchange;
    }
}
