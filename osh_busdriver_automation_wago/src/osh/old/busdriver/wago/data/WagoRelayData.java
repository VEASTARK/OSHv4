package osh.old.busdriver.wago.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlType;

/**
 * wago xml interface
 * 
 * @author Kaibin Bao
 *
 */
@XmlType
public class WagoRelayData {
	@XmlPath("@id")
	private int id;
	
	@XmlPath("output/@state")
	private boolean state;

	public int getId() {
		return id;
	}

	public boolean getState() {
		return state;
	}	
}
