package osh.registry;

import osh.datatypes.registry.AbstractExchange;
import osh.registry.interfaces.IDataRegistryListener;

/**
 * Specific listener wrapper for the data-registry callbacks based on AbstractExchanges.
 *
 * @author Sebastian Kramer
 */
public class DataListenerWrapper extends AbstractListenerWrapper<IDataRegistryListener> {

    /**
     * Generates a new wrapper around the given listener callbck.
     *
     * @param listener the listener callback
     */
    public DataListenerWrapper(IDataRegistryListener listener) {
        super(listener);
    }

    @Override
    public synchronized void onListen(AbstractExchange exchange) {
        this.getListener().onExchange(exchange);
    }
}
