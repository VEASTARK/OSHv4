package osh.core.interfaces;

import osh.registry.DataRegistry.DriverRegistry;

/**
 * @author Ingo Mauser
 */
public interface IOSHDriver extends IOSH {

    DriverRegistry getDriverRegistry();

}
