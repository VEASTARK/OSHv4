package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class RestStateDetail {
	
	protected UUID sender;
	private long timestamp;

	/** for JAXB */
	@SuppressWarnings("unused")
	@Deprecated
    protected RestStateDetail() {
		this(null, 0);
	}
	
	public RestStateDetail(UUID sender, long timestamp) {
		super();
		this.sender = sender;
		this.timestamp = timestamp;
	}

	@XmlTransient
	public UUID getSender() {
		return sender;
	}
	
	public void setSender(UUID sender) {
		this.sender = sender;
	}

	public long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RestStateDetail other = (RestStateDetail) obj;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
        return timestamp == other.timestamp;
    }
	
	
}