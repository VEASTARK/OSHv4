package osh;

import osh.core.OSHRandomGenerator;
import osh.core.interfaces.IOSH;
import osh.core.logging.IGlobalLogger;
import osh.eal.EALTimeDriver;

/**
 * Super class for all components managed by the OCManager or HALManager
 *
 * @author Florian Allerding, Ingo Mauser
 */
public abstract class OSHComponent {

    private final IOSH theOrganicSmartHome;


    /**
     * CONSTRUCTOR
     *
     * @param theOrganicSmartHome
     */
    public OSHComponent(IOSH theOrganicSmartHome) {
        super();
        this.theOrganicSmartHome = theOrganicSmartHome;
    }


    public IGlobalLogger getGlobalLogger() {
        return this.theOrganicSmartHome.getLogger();
    }

    public OSHRandomGenerator getRandomGenerator() {
        return this.theOrganicSmartHome.getRandomGenerator();
    }

    protected IOSH getOSH() {
        return this.theOrganicSmartHome;
    }

    public EALTimeDriver getTimeDriver() {
        return this.theOrganicSmartHome.getTimeDriver();
    }

}
