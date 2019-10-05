package osh.mgmt.ipp.pv;

import osh.datatypes.ea.interfaces.ISolution;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Matthias Maerz
 *
 */
public class SmaInverterPhenotype implements ISolution {

	private List<Integer> list;
	private Long referenceTime;
	
	public SmaInverterPhenotype() {
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
