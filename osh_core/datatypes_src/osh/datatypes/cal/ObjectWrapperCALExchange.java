package osh.datatypes.cal;

import osh.cal.CALComExchange;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Represents a generic wrapper around an object to send between CAL-Drivers/-Managers.
 *
 * @author Sebastian Kramer
 * @param <D> the object to wrap around
 */
public class ObjectWrapperCALExchange<D> extends CALComExchange {

    private final D data;

    /**
     * Constructs this wrapper around the given object with information about the sender and the time it was sent.
     *
     * @param uuid the unique identifier of the sender
     * @param timestamp the timestamp when it was sent
     * @param data the data object
     */
    public ObjectWrapperCALExchange(UUID uuid, ZonedDateTime timestamp, D data) {
        super(uuid, timestamp);
        this.data = data;
    }

    public D getData() {
        return this.data;
    }
}
