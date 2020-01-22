package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.driver.thermal.SimpleWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class WaterTankSimulationDriver
        extends DeviceSimulationDriver {

    protected SimpleWaterTank waterTank;

    /**
     * CONSTRUCTOR
     */
    public WaterTankSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);
    }
}
