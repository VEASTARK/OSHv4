package osh.eal.hal.interfaces.common;

import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

/**
 * @author Ingo Mauser
 */
public interface IHALDeviceMetaDetails {

    String getName();

    String getLocation();

    DeviceTypes getDeviceType();

    DeviceClassification getDeviceClassification();

    boolean isConfigured();
}
