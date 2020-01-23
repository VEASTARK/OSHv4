package osh.driver.simulation.cooling;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.driver.model.BuildingThermalModel;
import osh.driver.model.FZIThermalModel;
import osh.driver.simulation.spacecooling.SimulationHollChillerCalendar;
import osh.eal.hal.exceptions.HALException;
import osh.hal.exchange.SpaceCoolingObserverExchange;

import java.util.UUID;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class SimulationHollSpaceCoolingSimulationDriver
        extends SpaceCoolingSimulationDriver {

    final BuildingThermalModel model = new FZIThermalModel();


    /**
     * CONSTRUCTOR
     */
    public SimulationHollSpaceCoolingSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);
    }

    @Override
    public void onNextTimeTick() {

        if (this.getTimeDriver().getUnixTime() % 86400 == 0) {
            //SIMULATE REAL CALENDER
            SimulationHollChillerCalendar calendar = new SimulationHollChillerCalendar(this.getRandomGenerator());
            this.dates = calendar.getDate(this.getTimeDriver().getUnixTime());
        }

        if (!this.dates.isEmpty()) {
            ChillerCalendarDate date = this.dates.get(0);
            if (date.getStartTimestamp() <= this.getTimeDriver().getUnixTime()
                    && date.getStartTimestamp() + date.getLength() >= this.getTimeDriver().getUnixTime()) {

//				getGlobalLogger().logDebug("start: " + date.startTimestamp);
//				getGlobalLogger().logDebug("length: " + date.length);
//				getGlobalLogger().logDebug("amountPersons: " + date.amountOfPerson);

                // calculate demand
                double currentOutdoorTemperature =
                        this.outdoorTemperature.getTemperature(this.getTimeDriver().getUnixTime());
                this.coldWaterPowerDemand = this.model.calculateCoolingDemand(currentOutdoorTemperature);

//				if (demand < 0) {
//					getGlobalLogger().logDebug("Demand:" + demand + "outdoor: " + currentOutdoorTemperature);
//				}
            } else if (date.getStartTimestamp() + date.getLength() < this.getTimeDriver().getUnixTime()) {
                this.dates.remove(0);
                this.coldWaterPowerDemand = 0;
            }
        } else {
//			getGlobalLogger().logDebug("There are no appointments today.");
        }

        this.setPower(Commodity.COLDWATERPOWER, (int) this.coldWaterPowerDemand);

        SpaceCoolingObserverExchange ox =
                new SpaceCoolingObserverExchange(
                        this.getUUID(),
                        this.getTimeDriver().getUnixTime(),
                        this.dates,
                        this.outdoorTemperature.getMap(),
                        (int) Math.round(this.coldWaterPowerDemand));
        this.notifyObserver(ox);
    }

}
