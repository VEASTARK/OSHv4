package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class ChpSimulationDriver extends DeviceSimulationDriver {

    private boolean running;


    /**
     * CONSTRUCTOR
     */
    public ChpSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);
    }


    protected boolean isRunning() {
        return this.running;
    }

    protected void setRunning(boolean running) {
        this.running = running;
    }
}
