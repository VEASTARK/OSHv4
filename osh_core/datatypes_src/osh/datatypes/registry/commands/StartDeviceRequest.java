package osh.datatypes.registry.commands;

import osh.datatypes.registry.CommandExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.util.UUID;


/**
 * Start device now
 *
 * @author Kaibin Bao
 */
public class StartDeviceRequest extends CommandExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -4843814099005430530L;

    public StartDeviceRequest(UUID sender, UUID receiver, long timestamp) {
        super(sender, receiver, timestamp);
    }

}
