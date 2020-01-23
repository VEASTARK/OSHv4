package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * Stop device now
 *
 * @author Kaibin Bao
 */
public class StopDeviceRequest extends CommandExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -465373952181618356L;

    public StopDeviceRequest(UUID sender, UUID receiver, ZonedDateTime timestamp) {
        super(sender, receiver, timestamp);
    }

}
