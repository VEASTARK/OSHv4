package osh.driver.simulation.cooling;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.driver.simulation.spacecooling.July2013HollChillerCalendar;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.SpaceCoolingObserverExchange;

import java.util.UUID;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class July2013HollSpaceCoolingSimulationDriver
        extends SpaceCoolingSimulationDriver {


    /**
     * CONSTRUCTOR
     */
    public July2013HollSpaceCoolingSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);
    }


    @Override
    public void onSystemIsUp() {
        super.onSystemIsUp();

        July2013HollChillerCalendar calendar = new July2013HollChillerCalendar();
        this.dates = calendar.getDate();
    }

    @Override
    public void onNextTimeTick() {
        if (!this.dates.isEmpty()) {
            ChillerCalendarDate date = this.dates.get(0);
            if (date.getStartTimestamp() <= this.getTimer().getUnixTime()
                    && date.getStartTimestamp() + date.getLength() >= this.getTimer().getUnixTime()) {

//				getGlobalLogger().logDebug("start: " + date.startTimestamp);
//				getGlobalLogger().logDebug("length: " + date.length);
//				getGlobalLogger().logDebug("amountPersons: " + date.amountOfPerson);

                // get real demand from file
                this.coldWaterPowerDemand = date.getKnownPower();

//				if (demand < 0) {
//					getGlobalLogger().logDebug("Demand:" + demand + "outdoor: " + currentOutdoorTemperature);
//				}
            } else if (date.getStartTimestamp() + date.getLength() < this.getTimer().getUnixTime()) {
                this.dates.remove(0);
                this.coldWaterPowerDemand = 0;
            }
        } else {
//			getGlobalLogger().logDebug("There are no appointments today.");
        }

        this.setPower(Commodity.COLDWATERPOWER, (int) this.coldWaterPowerDemand);

        SpaceCoolingObserverExchange ox =
                new SpaceCoolingObserverExchange(
                        this.getDeviceID(),
                        this.getTimer().getUnixTime(),
                        this.dates,
                        this.outdoorTemperature.getMap(),
                        (int) Math.round(this.coldWaterPowerDemand));
        this.notifyObserver(ox);
    }

}
