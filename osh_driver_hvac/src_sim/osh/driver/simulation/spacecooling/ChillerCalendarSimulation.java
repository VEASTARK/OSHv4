package osh.driver.simulation.spacecooling;

import osh.driver.datatypes.cooling.ChillerCalendarDate;

import java.util.ArrayList;

/**
 * @author Ingo Mauser
 */
public abstract class ChillerCalendarSimulation {

    public abstract ArrayList<ChillerCalendarDate> getDate(long timestamp);

}
