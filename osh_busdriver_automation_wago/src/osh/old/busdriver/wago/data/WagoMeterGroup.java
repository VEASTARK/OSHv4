package osh.old.busdriver.wago.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * wago xml interface
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class WagoMeterGroup {
	@XmlPath("@id")
	private int groupId;

	@XmlPath("input")
	private List<WagoPowerMeter> meters;

	public int getGroupId() {
		return groupId;
	}

	public List<WagoPowerMeter> getMeters() {
		return meters;
	}
}
