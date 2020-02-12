package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * Start device now
 *
 * @author Kaibin Bao
 */
public class StartDeviceRequest extends CommandExchange implements IPromiseToBeImmutable {

    public StartDeviceRequest(UUID sender, UUID receiver, ZonedDateTime timestamp) {
        super(sender, receiver, timestamp);
    }
}
