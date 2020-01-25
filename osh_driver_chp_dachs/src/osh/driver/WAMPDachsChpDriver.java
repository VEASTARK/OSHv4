package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.datatypes.registry.driver.details.chp.ChpDriverDetails;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.dachs.WAMPDachsDispatcher;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class WAMPDachsChpDriver
        extends DachsChpDriver
        implements Runnable {

    protected WAMPDachsDispatcher dachsInformationWAMPDispatcher;


    /**
     * CONSTRUCTOR
     */
    public WAMPDachsChpDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws OSHException {
        super(osh, deviceID, driverConfig);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);

        this.dachsInformationWAMPDispatcher = new WAMPDachsDispatcher(this.getGlobalLogger(), this);
        new Thread(this, "pull proxy of dachs driver to WAMP").start();
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();
    }

    @Override
    public void run() {
        while (true) {
            synchronized (this.dachsInformationWAMPDispatcher) {
                try { // wait for new data
                    this.dachsInformationWAMPDispatcher.wait();
                } catch (InterruptedException e) {
                    this.getGlobalLogger().logError("should not happen", e);
                    break;
                }

                // long timestamp = getTimer().getUnixTime();

                if (this.dachsInformationWAMPDispatcher.getDachsDetails().isEmpty()) {
                    // an error has occurred
                }
            }
        }
    }

    @Override
    protected void sendPowerRequestToChp() {
        // Start new thread and send power request to CHP
        // (Has to be resend at least every 10 minutes)
        if (!this.isOperationRequest()) {
            this.dachsInformationWAMPDispatcher.sendPowerRequest(this.isOperationRequest());
        }
    }

    // for callback of DachsInformationRequestThread
    public void processDachsDetails(DachsDriverDetails dachsDetails) {

        if (dachsDetails == null) {
            return;
        }

        // ### save DachsDetails into DB ###
        this.getDriverRegistry().publish(DachsDriverDetails.class, dachsDetails);

        // ### transform DachsDetails to ChpDetails ###
        HashMap<String, String> values = dachsDetails.getValues();

        // convert Dachs Details to general CHP details
        ChpDriverDetails chpDetails = new ChpDriverDetails(this.getUUID(), this.getTimeDriver().getCurrentEpochSecond());

        // Heating request or power request? Or both?
        chpDetails.setPowerGenerationRequest(this.isElectricityRequest());
        chpDetails.setHeatingRequest(this.isHeatingRequest());

        // current power
        Double currentElectricalPower = this.parseDoubleStatus(values.get("Hka_Mw1.sWirkleistung"));
        if (currentElectricalPower == null) {
            currentElectricalPower = -1.0;
        } else {
            currentElectricalPower *= -1000.0;
        }
        chpDetails.setCurrentElectricalPower(currentElectricalPower);

        double currentThermalPower = -1.0;
        if (Math.round(currentElectricalPower) < -1000) {
            currentThermalPower = -12500.0;
        }
        chpDetails.setCurrentThermalPower(currentThermalPower);

        // total energy
        Double generatedElectricalWork = this.parseDoubleStatus(values.get("Hka_Bd.ulArbeitElektr"));
        if (generatedElectricalWork == null) {
            generatedElectricalWork = -1.0;
        }
        chpDetails.setGeneratedElectricalWork(generatedElectricalWork);

        Double generatedThermalWork = this.parseDoubleStatus(values.get("Hka_Bd.ulArbeitThermHka"));
        if (generatedThermalWork != null) {
            generatedThermalWork = -1.0;
            chpDetails.setGeneratedThermalWork(generatedThermalWork);
        }

        // priorities
        Integer electricalPowerPrioritizedControl = this.parseIntegerStatus(values.get("Hka_Bd.UStromF_Frei.bFreigabe"));
        if (electricalPowerPrioritizedControl != null) {
            if (electricalPowerPrioritizedControl == 255) {
                chpDetails.setElectricalPowerPrioritizedControl(true);
            } else {
                chpDetails.setElectricalPowerPrioritizedControl(false);
            }
        }
        // always with thermal priority
        chpDetails.setThermalPowerPrioritizedControl(true);

        // temperature
        Integer temperatureIn = this.parseIntegerStatus(values.get("Hka_Mw1.Temp.sbGen"));
        if (temperatureIn == null) {
            temperatureIn = -1;
        }
        chpDetails.setTemperatureIn(temperatureIn);

        Integer temperatureOut = this.parseIntegerStatus(values.get("Hka_Mw1.Temp.sbMotor"));
        if (temperatureOut == null) {
            temperatureOut = -1;
        }
        chpDetails.setTemperatureOut(temperatureOut);

        // convert to TemperatureDetails
        Double waterStorageTemperature = this.parseDoubleStatus(values.get("Hka_Mw1.Temp.sbFuehler1"));
        if (waterStorageTemperature != null) {
            TemperatureDetails td = new TemperatureDetails(this.getHotWaterTankUuid(), this.getTimeDriver().getCurrentEpochSecond());
            td.setTemperature(waterStorageTemperature);
            this.getDriverRegistry().publish(TemperatureDetails.class, td);
        }

        this.chpDriverDetails = chpDetails;
        this.getDriverRegistry().publish(ChpDriverDetails.class, this, this.chpDriverDetails);
        this.processChpDetailsAndNotify(this.chpDriverDetails);
    }
}
