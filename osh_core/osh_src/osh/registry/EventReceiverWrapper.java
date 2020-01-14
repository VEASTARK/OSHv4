package osh.registry;

import osh.core.interfaces.IPromiseToEnsureSynchronization;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.UUID;

/**
 * FOR INTERNAL USE ONLY
 */
public class EventReceiverWrapper implements IPromiseToEnsureSynchronization {
    private final EventReceiverType type;
    private final IEventTypeReceiver eventtypereceiver;

    public EventReceiverWrapper(IEventTypeReceiver eventtypereceiver) {
        super();
        this.type = EventReceiverType.IEVENTTYPERECEIVER;
        this.eventtypereceiver = eventtypereceiver;
    }

    public EventReceiverType getType() {
        return this.type;
    }

    public IEventTypeReceiver getEventTypeReceiver() {
        return this.eventtypereceiver;
    }

    public UUID getUUID() {
        if (this.type == EventReceiverType.IEVENTTYPERECEIVER) {
            return this.eventtypereceiver.getUUID();
        } else {
            throw new NullPointerException("type is null");//should never happen
        }
    }

    public Object getSyncObject() {
        if (this.type == EventReceiverType.IEVENTTYPERECEIVER) {
            return this.eventtypereceiver.getSyncObject();
        } else {
            throw new NullPointerException("type is null");//should never happen
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (this.type == EventReceiverType.IEVENTTYPERECEIVER) {
            result = prime
                    * result
                    + ((this.eventtypereceiver == null) ? 0 : this.eventtypereceiver
                    .hashCode());
        }
        result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        EventReceiverWrapper other = (EventReceiverWrapper) obj;
        if (this.type == EventReceiverType.IEVENTTYPERECEIVER) {
            if (this.eventtypereceiver == null) {
                if (other.eventtypereceiver != null)
                    return false;
            } else if (!this.eventtypereceiver.equals(other.eventtypereceiver))
                return false;
        }
        return this.type == other.type;
    }

}