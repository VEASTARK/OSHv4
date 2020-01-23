package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.datatypes.registry.driver.details.chp.ChpDriverDetails;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.dachs.GLTDachsInfoRequestThread;
import osh.driver.dachs.GLTDachsPowerRequestThread;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class GLTDachsChpDriver
        extends DachsChpDriver {

    protected String loginName;
    protected String loginPwd;
    private long lastRequest = Long.MIN_VALUE;
    private GLTDachsInfoRequestThread reqRunnable;
    private Thread reqThread;


    /**
     * CONSTRUCTOR
     */
    public GLTDachsChpDriver(IOSH osh, UUID deviceID, OSHParameterCollection driverConfig)
            throws OSHException {
        super(osh, deviceID, driverConfig);

        this.loginName = driverConfig.getParameter("dachsloginname");
        this.loginPwd = driverConfig.getParameter("dachsloginpwd");
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getTimeDriver().registerComponent(this, 300);

        // init request thread
        this.reqRunnable = new GLTDachsInfoRequestThread(
                this.getGlobalLogger(),
                this,
                this.getDachsURL(),
                this.loginName,
                this.loginPwd);
        this.reqThread = new Thread(this.reqRunnable, "DachsInformationRequestThread");
        this.reqThread.start();
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // re-init request thread if dead
        if (this.reqThread == null || !this.reqThread.isAlive()) {
            this.reqRunnable = new GLTDachsInfoRequestThread(
                    this.getGlobalLogger(),
                    this, this.getDachsURL(),
                    this.loginName,
                    this.loginPwd);
            this.reqThread = new Thread(this.reqRunnable, "DachsInformationRequestThread");
            this.reqThread.start();
        }
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();
        this.reqRunnable.shutdown();
    }

    @Override
    protected void sendPowerRequestToChp() {
        // Start new thread and send power request to CHP
        // (Has to be resend at least every 10 minutes)
        if (this.isOperationRequest() && this.getTimeDriver().getUnixTime() - this.lastRequest > 60) {
            GLTDachsPowerRequestThread powerRequestThread = new GLTDachsPowerRequestThread(
                    this.getGlobalLogger(),
                    this.isOperationRequest(),
                    this.getDachsURL(),
                    this.loginName,
                    this.loginPwd
            );
            new Thread(powerRequestThread, "DachsPowerRequestThread").start();
            this.lastRequest = this.getTimeDriver().getUnixTime();
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
        ChpDriverDetails chpDetails = new ChpDriverDetails(this.getUUID(), this.getTimeDriver().getUnixTime());

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
//			TemperatureDetails td = new TemperatureDetails(getDeviceID(), getTimer().getUnixTime());
            TemperatureDetails td = new TemperatureDetails(this.getHotWaterTankUuid(), this.getTimeDriver().getUnixTime());
            td.setTemperature(waterStorageTemperature);
//			getDriverRegistry().setState(TemperatureDetails.class, this, td);
//			getDriverRegistry().setState(TemperatureDetails.class, UUID.fromString("268ea9bd-572c-46dd-a383-960b4ed65337"), td);
            this.getDriverRegistry().publish(TemperatureDetails.class, td);
        }

        this.chpDriverDetails = chpDetails;
        this.getDriverRegistry().publish(ChpDriverDetails.class, this, this.chpDriverDetails);
        this.processChpDetailsAndNotify(this.chpDriverDetails);
    }

}
