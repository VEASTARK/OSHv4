package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author Kaibin Bao
 */
@XmlType(name = "scheduleDetails")
public class RestScheduleDetails extends RestStateDetail {

    protected long scheduledStartTime;

    /**
     * for JAXB
     */
    @Deprecated
    public RestScheduleDetails() {
        this(null, 0);
    }

    public RestScheduleDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public long getScheduledStartTime() {
        return this.scheduledStartTime;
    }

    public void setScheduledStartTime(long scheduledStartTime) {
        this.scheduledStartTime = scheduledStartTime;
    }
}
