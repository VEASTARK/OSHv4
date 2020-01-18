package osh.datatypes.registry;

import java.io.Serializable;
import java.util.UUID;


/**
 * Abstract Data exchange object for Registry
 *
 * @author Till Schuberth, Kaibin Bao, Ingo Mauser
 */

public abstract class Exchange implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = -5082593042563570677L;


    protected UUID sender;


    private long timestamp;


    protected Exchange() {
        super();
    }

    public Exchange(UUID sender, long timestamp) {
        super();
        this.sender = sender;
        this.timestamp = timestamp;
    }


    public UUID getSender() {
        return this.sender;
    }

    /**
     * handle with care
     *
     * @param sender
     */
    public void setSender(UUID sender) {
        this.sender = sender;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.sender == null) ? 0 : this.sender.hashCode());
        result = prime * result + (int) (this.timestamp ^ (this.timestamp >>> 32));
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
        Exchange other = (Exchange) obj;
        if (this.sender == null) {
            if (other.sender != null)
                return false;
        } else if (!this.sender.equals(other.sender))
            return false;
        return this.timestamp == other.timestamp;
    }


}
