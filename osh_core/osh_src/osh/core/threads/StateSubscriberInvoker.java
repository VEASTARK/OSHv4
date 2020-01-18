package osh.core.threads;

import osh.core.exceptions.OSHException;
import osh.datatypes.registry.StateChangedExchange;
import osh.registry.EventReceiverType;
import osh.registry.EventReceiverWrapper;
import osh.registry.StateChangedEventSet;
import osh.registry.interfaces.IEventTypeReceiver;

/**
 * Invokes a {@link IEventTypeReceiver} when new events are available.
 * <p>
 * A concrete strategy in the strategy pattern.
 *
 * @author Kaibin Bao
 */
public class StateSubscriberInvoker extends InvokerEntry<EventReceiverWrapper> {

    private final StateChangedEventSet eventSet;

    /* CONSTRUCTOR */
    public StateSubscriberInvoker(EventReceiverWrapper eventQueueSubscriber,
                                  StateChangedEventSet eventSet) {
        super(eventQueueSubscriber);

        this.eventSet = eventSet;
    }

    @Override
    public boolean shouldInvoke() {
        return !this.eventSet.isEmpty();
    }

    @Override
    public void invoke() throws OSHException {
        StateChangedExchange ex;
        while ((ex = this.eventSet.getNext()) != null) {
            EventReceiverWrapper receiver = this.getSubscriber();
            synchronized (receiver.getSyncObject()) {
                if (receiver.getType() == EventReceiverType.IEVENTTYPERECEIVER) {
                    receiver.getEventTypeReceiver().onQueueEventTypeReceived(StateChangedExchange.class, ex);
                } else {
                    throw new IllegalStateException("type is not known");
                }
            }
        }
    }

    @Override
    public Object getSyncObject() {
        return this.eventSet;
    }

    @Override
    public String getName() {
        return "StateSubscriberInvoker for " + this.getSubscriber().getClass().getName();
    }

    /* Delegate to eventQueueSubscriber for HashMap */

    @Override
    public int hashCode() {
        return this.getSubscriber().hashCode() ^ 0x50000000;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof StateSubscriberInvoker)
            return this.getSubscriber().equals(((StateSubscriberInvoker) obj).getSubscriber());
        else
            return false;
    }
}
