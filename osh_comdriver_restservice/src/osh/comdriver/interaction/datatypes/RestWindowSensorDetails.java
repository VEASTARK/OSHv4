package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * 
 * @author Ingo Mauser
 *
 */
@XmlType(name="windowSensorDetails")
public class RestWindowSensorDetails extends RestStateDetail {

	protected boolean state;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
    protected RestWindowSensorDetails() {
		this(null, 0);
	}
	
	public RestWindowSensorDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	
	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

}
