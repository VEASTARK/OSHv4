package osh.datatypes.registry.oc.state;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Interface for prediction
 *
 * @author Florian Allerding, Till Schuberth
 */
public interface IAction {
    UUID getDeviceId();

    ZonedDateTime getTimestamp();

    boolean equals(IAction other);

//	public IAction createAction(long newTimestamp);

    @Override
    int hashCode();
}