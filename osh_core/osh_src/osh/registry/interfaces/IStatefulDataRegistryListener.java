package osh.registry.interfaces;

/**
 * Conveniance interface combining the interfaces for having an identity and having a listening interface for the
 * registry. Using the registry to subscribe to exchanges requires both of these so using this interface is simpler.
 *
 * @author Sebastian Kramer
 */
public interface IStatefulDataRegistryListener extends IDataRegistryListener, IHasState {
}
