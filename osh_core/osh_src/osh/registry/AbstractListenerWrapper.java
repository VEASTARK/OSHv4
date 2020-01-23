package osh.registry;

import osh.datatypes.registry.AbstractExchange;

import java.util.Objects;

/**
 * Generic wrapper for listener methods for use in the registry. Class exists to provide unified call methods for
 * subscribed exchanges in the registry and to ensure synchonization of the underlying callbacks
 *
 * @author Sebastian Kramer
 * @param <T> the listener interface
 */
public abstract class AbstractListenerWrapper<T> {

    private final T listener;

    /**
     * Generates a new wrapper around the given listener callbck.
     *
     * @param listener the listener callback
     */
    public AbstractListenerWrapper(T listener) {
        this.listener = listener;
    }

    /**
     * Calls the wrapped listener method with the given exchange object.
     *
     * @param exchange the exchange object
     */
    public abstract void onListen(AbstractExchange exchange);

    /**
     * Returns the listener callback underlying this wrapper     *
     * @return the listener callback
     */
    public T getListener() {
        return this.listener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        AbstractListenerWrapper<?> that = (AbstractListenerWrapper<?>) o;

        return Objects.equals(this.listener, that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.listener);
    }
}
