package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.datatypes.registry.EventExchange;
import osh.registry.EventQueue;
import osh.registry.EventReceiverType;
import osh.registry.EventReceiverWrapper;
import osh.registry.ExchangeWrapper;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * Invokes a {@link IEventTypeReceiver} when new events are available.
 * <p>
 * A concrete strategy in the strategy pattern.
 *
 * @author Kaibin Bao
 */
public class EventQueueSubscriberInvoker extends InvokerEntry<EventReceiverWrapper> {

    private final EventQueue eventqueue;

    /* CONSTRUCTOR */
    public EventQueueSubscriberInvoker(EventReceiverWrapper eventQueueSubscriber,
                                       EventQueue eventqueue) {
        super(eventQueueSubscriber);

        this.eventqueue = eventqueue;
    }

    @Override
    public boolean shouldInvoke() {
        return this.eventqueue.isNotEmpty();
    }

    /**
     * This function only exists because the compiler cannot infer the type parameters in this situation
     */
    private <T extends EventExchange> void wildcardHelper(IEventTypeReceiver receiver, ExchangeWrapper<T> wrapper) throws OSHException {
        receiver.onQueueEventTypeReceived(wrapper.getType(), wrapper.getExchange());
    }


    @Override
    public void invoke() throws OSHException {
        ExchangeWrapper<? extends EventExchange> ex;
        while ((ex = this.eventqueue.getNext()) != null) {
            EventReceiverWrapper sub = this.getSubscriber();
            synchronized (sub.getSyncObject()) {
                if (sub.getType() == EventReceiverType.IEVENTTYPERECEIVER) {
                    this.wildcardHelper(sub.getEventTypeReceiver(), ex);
                } else {
                    throw new NullPointerException("type is null"); //cannot happen if you don't changed something
                }
            }
        }
    }

    @Override
    public Object getSyncObject() {
        return this.eventqueue;
    }

    @Override
    public String getName() {
        return "EventQueueSubscriberInvoker for " + this.getSubscriber().getClass().getName();
    }

    /* Delegate to eventQueueSubscriber for HashMap */

    @Override
    public int hashCode() {
        return this.getSubscriber().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof EventQueueSubscriberInvoker)
            return this.getSubscriber().equals(((EventQueueSubscriberInvoker) obj).getSubscriber());
        else
            return false;
    }
}
