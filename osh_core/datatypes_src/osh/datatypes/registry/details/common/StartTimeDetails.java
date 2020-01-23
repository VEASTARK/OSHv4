package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Kaibin Bao
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "StartTimeDetails")
@XmlType

public class StartTimeDetails extends StateExchange {


    /**
     *
     */
    private static final long serialVersionUID = 8863557474207657667L;
    private long startTime;

    @SuppressWarnings("unused")
    @Deprecated
    protected StartTimeDetails() {
        super(null, null);
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public StartTimeDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);

    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

}