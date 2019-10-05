package osh.hal.interfaces.appliance;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.PowerProfileTick;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * 
 * @author Ingo Mauser
 *
 */
public interface IHALMieleApplianceProgramDetails {
	String getProgramName();
	String getPhaseName();
	EnumMap<Commodity, ArrayList<PowerProfileTick>> getExpectedLoadProfiles();
}
