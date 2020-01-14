package osh.core.interfaces;

import osh.core.exceptions.OSHException;
import osh.datatypes.registry.EventExchange;

public interface IQueueEventTypeSubscriber {

    /**
     * This function gets called when there are new events in the event queue.
     * <p>
     * WARNING: asynchronous invocation, don't forget synchronization!
     *
     * @throws OSHException
     */
    <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException;

}
