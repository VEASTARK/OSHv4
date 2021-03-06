package osh.datatypes.registry;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * This class provides the skeletal structure of an exchange object to be used in the registry of the OSH.
 *
 * An extending class needs only to implement the data objects to be exchanged.
 *
 * @author Sebastian Kramer
 */
public abstract class AbstractExchange implements Cloneable {


    private UUID sender;
    private final ZonedDateTime timestamp;

    /**
     * Creates an exchange object for the given sender at the given timestamp.
     *
     * @param sender identity of the sender of this exchange
     * @param timestamp timestamp at which this exchange object was created
     */
    public AbstractExchange(UUID sender, ZonedDateTime timestamp) {
        Objects.requireNonNull(sender);
        this.sender = sender;
        this.timestamp = timestamp;
    }

    /**
     *  Returns the identity of the sender of this exchange.
      * @return the sender of this exchange
     */
    public UUID getSender() {
        return this.sender;
    }

    /**
     * Sets the identity of the sender of this exchange
     * @param sender thw new sender
     */
    public void setSender(UUID sender) {
        this.sender = sender;
    }

    /**
     * Returns the timestamp marking this exchange
     * @return
     */
    public ZonedDateTime getTimestamp() {
        return this.timestamp;
    }

    @Override
    public AbstractExchange clone() {
        try {
            return (AbstractExchange) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("subclass doesn't provide proper cloning functionality", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        AbstractExchange abstractExchange = (AbstractExchange) o;
        return this.timestamp == abstractExchange.timestamp &&
                this.sender.equals(abstractExchange.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.sender, this.timestamp);
    }
}
