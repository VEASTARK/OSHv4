package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * 
 * @author Kaibin Bao, Ingo Mauser
 *
 */
@XmlType
public class BusDeviceStatusDetails extends StateExchange {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8380215142279886946L;

	@XmlType
	public enum ConnectionStatus {
		@XmlEnumValue("ATTACHED")
		ATTACHED,
		@XmlEnumValue("DETACHED")
		DETACHED,
		@XmlEnumValue("ERROR")
		ERROR,
		@XmlEnumValue("UNDEFINED")
		UNDEFINED
	}
	

	protected ConnectionStatus state;
	
	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
    protected BusDeviceStatusDetails() {
		this(null, 0, ConnectionStatus.UNDEFINED);
	}

	public BusDeviceStatusDetails(UUID sender, long timestamp, ConnectionStatus state) {
		super(sender, timestamp);
		
		this.state = state;
	}

	public ConnectionStatus getState() {
		return state;
	}

	@Override
	public String toString() {
		return "BusDeviceStatus: " + state.name();
	}
}
