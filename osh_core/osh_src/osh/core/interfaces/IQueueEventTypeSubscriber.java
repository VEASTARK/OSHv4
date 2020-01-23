package osh.core.interfaces;

import osh.datatypes.registry.EventExchange;

public interface IQueueEventTypeSubscriber {

    /**
     * This function gets called when there are new events in the event queue.
     * <p>
     * WARNING: asynchronous invocation, don't forget synchronization!
     *
     */
    <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event);

}
