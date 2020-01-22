package osh.eal.hal;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHDriver;
import osh.core.interfaces.IRealTimeSubscriber;
import osh.eal.EALDriver;
import osh.registry.Registry.DriverRegistry;
import osh.registry.interfaces.IProvidesIdentity;
import osh.utils.uuid.UUIDLists;

import java.util.ArrayList;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class HALDriver extends EALDriver implements IRealTimeSubscriber, ILifeCycleListener, IProvidesIdentity {

    private final UUID deviceID;
    private OSHParameterCollection driverConfig;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public HALDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh);

        this.deviceID = deviceID;
        this.driverConfig = driverConfig;
    }


    @Override
    protected IOSHDriver getOSH() {
        return (IOSHDriver) super.getOSH();
    }

    /**
     * The UUID of the device.
     *
     * @return Device-UUID
     */
    public UUID getUUID() {
        return this.deviceID;
    }

    protected DriverRegistry getDriverRegistry() {
        return this.getOSH().getDriverRegistry();
    }

    /**
     * @return the driverConfig
     */
    public OSHParameterCollection getDriverConfig() {
        return this.driverConfig;
    }

    /**
     * @param driverConfig the driverConfig to set
     */
    public void setDriverConfig(OSHParameterCollection driverConfig) {
        this.driverConfig = driverConfig;
    }

    @Override
    public HALDriver getSyncObject() {
        return this;
    }

    @Override
    public void onSystemRunning() {
        //...in case of use please override
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        //...in case of use please override
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        //...in case of use please override and implement things like:
//		getTimer().registerComponent(this, 1);
//		getDriverRegistry().registerStateChangeListener(ComDriverDetails.class, this);
    }

    @Override
    public void onSystemHalt() {
        //...in case of use please override
    }

    @Override
    public void onSystemResume() {
        //...in case of use please override
    }

    @Override
    public void onSystemError() {
        //...in case of use please override
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        //...in case of use please override
    }


    // HELPER METHODS

    protected ArrayList<UUID> parseUUIDArray(String parameter) throws OSHException {
        try {
            return UUIDLists.parseUUIDArray(parameter);
        } catch (IllegalArgumentException e) {
            throw new OSHException(e);
        }
    }

}
