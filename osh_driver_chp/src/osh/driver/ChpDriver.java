package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.driver.details.chp.ChpDriverDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.hal.exchange.ChpObserverExchange;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

import java.util.UUID;


/**
 * @author Ingo Mauser, Jan M�ller
 */
public abstract class ChpDriver
        extends HALDeviceDriver
        implements IEventTypeReceiver, IHasState {

    protected int minimumRuntime;
    protected int runtime;
    protected ChpDriverDetails chpDriverDetails;
    private boolean electricityRequest;
    private boolean heatingRequest;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public ChpDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
        //NOTHING
    }


    protected abstract void sendPowerRequestToChp();

    public synchronized void processChpDetailsAndNotify(ChpDriverDetails chpDetails) {
        ChpObserverExchange _ox = new ChpObserverExchange(this.getDeviceID(), this.getTimer().getUnixTime());
        _ox.setActivePower((int) Math.round(chpDetails.getCurrentElectricalPower()));
        _ox.setThermalPower((int) Math.round(chpDetails.getCurrentThermalPower()));
        _ox.setElectricityRequest(this.electricityRequest);
        _ox.setHeatingRequest(this.heatingRequest);
        _ox.setMinRuntime(this.minimumRuntime);
        _ox.setMinRuntimeRemaining(this.getMinimumRuntimeRemaining());

        // Current state of CHP is sometimes not given to Observer directly
        if (_ox.getActivePower() != 0) {
            _ox.setRunning(true);
        }

        this.notifyObserver(_ox);
    }

    public int getMinimumRuntime() {
        return this.minimumRuntime;
    }

    protected void setMinimumRuntime(int minimumRuntime) {
        this.minimumRuntime = minimumRuntime;
    }

    public int getMinimumRuntimeRemaining() {
        int returnValue = this.minimumRuntime - this.runtime;
        if (returnValue < 0) returnValue = 0;
        return returnValue;
    }

    public int getRuntime() {
        return this.runtime;
    }

    public boolean isElectricityRequest() {
        return this.electricityRequest;
    }

    protected void setElectricityRequest(boolean electricityRequest) {
        if (this.chpDriverDetails != null) {
            this.chpDriverDetails.setPowerGenerationRequest(electricityRequest);
            this.getDriverRegistry().publish(ChpDriverDetails.class, this, this.chpDriverDetails);
        }

        this.electricityRequest = electricityRequest;
    }

    public boolean isHeatingRequest() {
        return this.heatingRequest;
    }

    protected void setHeatingRequest(boolean heatingRequest) {
        if (this.chpDriverDetails != null) {
            this.chpDriverDetails.setHeatingRequest(heatingRequest);
            this.getDriverRegistry().publish(ChpDriverDetails.class, this, this.chpDriverDetails);
        }
        this.heatingRequest = heatingRequest;
    }

    public boolean isOperationRequest() {
        return this.heatingRequest || this.electricityRequest;
    }

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

}
