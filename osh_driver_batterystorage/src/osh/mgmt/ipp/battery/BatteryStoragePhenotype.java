package osh.mgmt.ipp.battery;

import osh.datatypes.ea.interfaces.ISolution;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Jan Mueller, Matthias Maerz
 *
 */
public class BatteryStoragePhenotype implements ISolution {

	private List<Integer> list;
	private Long referenceTime;
	
	public BatteryStoragePhenotype() {
		list = new ArrayList<>();
	}

	public List<Integer> getList() {
		return this.list;
	}
	public Long getReferenceTime() {
		return this.referenceTime;
	}
	public void setList(List<Integer> list) {
		this.list = list;
	}
	public void setReferenceTime(Long time) {
		this.referenceTime = time;
	}
	
}
