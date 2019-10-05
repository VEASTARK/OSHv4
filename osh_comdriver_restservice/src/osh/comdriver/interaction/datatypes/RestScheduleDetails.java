package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * 
 * @author Kaibin Bao
 *
 */
@XmlAccessorType( XmlAccessType.PUBLIC_MEMBER )
@XmlType(name="scheduleDetails")
public class RestScheduleDetails extends RestStateDetail {
	
	protected long scheduledStartTime;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
	private RestScheduleDetails() {
		this(null, 0);
	}
	
	public RestScheduleDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}
	
	public long getScheduledStartTime() {
		return scheduledStartTime;
	}
	
	public void setScheduledStartTime(long scheduledStartTime) {
		this.scheduledStartTime = scheduledStartTime;
	}
}
