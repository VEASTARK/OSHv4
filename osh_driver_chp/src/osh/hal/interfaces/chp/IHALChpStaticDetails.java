package osh.hal.interfaces.chp;

import osh.driver.chp.ChpOperationMode;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALChpStaticDetails {
	int getTypicalActivePower();
	int getTypicalReactivePower();
	int getTypicalGasPower();
	int getTypicalThermalPower();
	
	int getMinRuntime();
	ChpOperationMode getOperationMode();
}
