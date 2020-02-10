package osh.datatypes.registry.oc.state;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.ZonedDateTime;
import java.util.UUID;


@XmlRootElement
public class ExpectedStartTimeExchange extends StateExchange {

    private ZonedDateTime expectedStartTime;

    public ExpectedStartTimeExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    public ExpectedStartTimeExchange(UUID sender, ZonedDateTime timestamp,
                                     ZonedDateTime startTime) {
        super(sender, timestamp);
        this.expectedStartTime = startTime;
    }

    public ZonedDateTime getExpectedStartTime() {
        return this.expectedStartTime;
    }

    public void setExpectedStartTime(ZonedDateTime expectedStartTime) {
        this.expectedStartTime = expectedStartTime;
    }

    @Override
    public String toString() {
        return "ExpectedStartTimeExchange [expectedStartTime="
                + this.expectedStartTime.toEpochSecond() + "]";
    }
}
