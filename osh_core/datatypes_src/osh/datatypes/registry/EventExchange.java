package osh.datatypes.registry;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Till Schuberth
 */
public abstract class EventExchange extends AbstractExchange {

    public EventExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    @Override
    public EventExchange clone() {
        return (EventExchange) super.clone();
    }

    /**
     * tries to cast this object to the given event type.
     *
     * @param type the type of this event
     * @return This object casted to the event type or null if this is not possible.
     */
    public <T extends EventExchange> T castToType(Class<T> type) {
        if (type.isAssignableFrom(this.getClass())) {
            return type.cast(this);
        } else {
            return null;
        }
    }

}
