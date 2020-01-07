package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


@XmlType(name="switchDetails")
public class RestSwitchDetails extends RestStateDetail {
	protected boolean on;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
    protected RestSwitchDetails() {
		this(null, 0);
	};
	
	public RestSwitchDetails(UUID sender, long timestamp) {
		super(sender, timestamp);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	};
}
