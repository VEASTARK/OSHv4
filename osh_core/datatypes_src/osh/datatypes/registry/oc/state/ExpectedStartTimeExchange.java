package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZonedDateTime;
import java.util.UUID;


@XmlRootElement
public class ExpectedStartTimeExchange extends StateExchange {
    /**
     *
     */
    private static final long serialVersionUID = -5083812677185394953L;
    private long expectedStartTime;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    private ExpectedStartTimeExchange() {
        this(null, null);
    }

    public ExpectedStartTimeExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    public ExpectedStartTimeExchange(UUID sender, ZonedDateTime timestamp,
                                     long startTime) {
        super(sender, timestamp);
        this.expectedStartTime = startTime;
    }

    public long getExpectedStartTime() {
        return this.expectedStartTime;
    }

    public void setExpectedStartTime(long expectedStartTime) {
        this.expectedStartTime = expectedStartTime;
    }

    @Override
    public String toString() {
        return "ExpectedStartTimeExchange [expectedStartTime="
                + this.expectedStartTime + "]";
    }
}
