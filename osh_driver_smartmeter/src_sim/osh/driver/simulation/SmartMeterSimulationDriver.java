package osh.driver.simulation;


import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.screenplay.SubjectAction;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class SmartMeterSimulationDriver
        extends DeviceSimulationDriver {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public SmartMeterSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // NOTHING
    }


    @Override
    public void onNextTimeTick() {

        // get voltage
        //TODO

        // communicate state to logger
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
