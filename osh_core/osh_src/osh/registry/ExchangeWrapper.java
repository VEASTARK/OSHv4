package osh.registry;

import osh.datatypes.registry.EventExchange;


/**
 * FOR INTERNAL USE ONLY
 */
public class ExchangeWrapper<T extends EventExchange> {

    private final Class<T> type;
    private final T ex;

    public <U extends T> ExchangeWrapper(Class<T> type, U ex) {
        super();
        this.type = type;
        this.ex = ex;
    }

    public Class<T> getType() {
        return this.type;
    }

    public T getExchange() {
        return this.ex;
    }
}
