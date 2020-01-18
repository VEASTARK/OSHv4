package osh.datatypes.registry;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class StateChangedExchange extends EventExchange {

    /**
     *
     */
    private static final long serialVersionUID = -145050460542819652L;
    private final Class<? extends StateExchange> type;
    private final UUID statefulEntity;


    /**
     * CONSTRUCTOR
     *
     * @param timestamp
     * @param type
     * @param statefulEntity
     */
    public StateChangedExchange(long timestamp,
                                Class<? extends StateExchange> type, UUID statefulEntity) {
        super(null, timestamp);

        this.type = type;
        this.statefulEntity = statefulEntity;
    }


    public Class<? extends StateExchange> getType() {
        return this.type;
    }

    public UUID getStatefulEntity() {
        return this.statefulEntity;
    }

    // needed for StateChangedEventSet
    @Override
    public int hashCode() {
        if (this.statefulEntity == null) {
            System.out.println("statefulEntity NULL");
        }
        if (this.type == null) {
            System.out.println("type NULL");
        }
        return this.statefulEntity.hashCode() ^ this.type.hashCode();
    }

    // needed for StateChangedEventSet
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof StateChangedExchange))
            return false;

        StateChangedExchange other = (StateChangedExchange) obj;

        return this.statefulEntity.equals(other.statefulEntity)
                && this.type.equals(other.type);
    }
}
