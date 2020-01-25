package osh.core.interfaces;

import osh.core.DataBroker;
import osh.core.LifeCycleStates;
import osh.core.OSHRandomGenerator;
import osh.core.logging.IGlobalLogger;
import osh.eal.EALTimeDriver;
import osh.registry.TimeRegistry;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public interface IOSH {

    boolean isSimulation();

    IGlobalLogger getLogger();

    IOSHStatus getOSHStatus();

    EALTimeDriver getTimeDriver();

    TimeRegistry getTimeRegistry();

    OSHRandomGenerator getRandomGenerator();

    LifeCycleStates getCurrentLifeCycleState();

    DataBroker getDataBroker();

}
