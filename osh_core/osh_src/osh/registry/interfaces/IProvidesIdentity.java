package osh.registry.interfaces;

import java.util.UUID;

/**
 * Signals that the implementing class can provide a {@link UUID} as an unique identifier.
 *
 * @author Sebastian Kramer
 */
public interface IProvidesIdentity {

    /**
     * Returns the unique identifier of this object.
     *
     * @return the unique identifier of this object
     */
    UUID getUUID();
}
