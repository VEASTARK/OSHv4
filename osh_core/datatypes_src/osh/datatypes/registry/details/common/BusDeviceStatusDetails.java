package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Kaibin Bao, Ingo Mauser
 */
public class BusDeviceStatusDetails extends StateExchange implements IPromiseToBeImmutable {

    protected final ConnectionStatus state;

    public BusDeviceStatusDetails(UUID sender, ZonedDateTime timestamp, ConnectionStatus state) {
        super(sender, timestamp);

        this.state = state;
    }

    public ConnectionStatus getState() {
        return this.state;
    }

    @Override
    public String toString() {
        return "BusDeviceStatus: " + this.state.name();
    }

    public enum ConnectionStatus {
        ATTACHED,
        DETACHED,
        ERROR,
        UNDEFINED
    }
}
