package osh.cal;

import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSH;
import osh.core.interfaces.IOSHCom;
import osh.eal.time.TimeExchange;
import osh.registry.Registry.ComRegistry;
import osh.registry.interfaces.IProvidesIdentity;
import osh.registry.interfaces.ITimeRegistryListener;
import osh.utils.uuid.UUIDLists;

import java.util.ArrayList;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser, Sebastian Kramer
 */
public class CALDriver extends OSHComponent implements ITimeRegistryListener, ILifeCycleListener, IProvidesIdentity {

    private final UUID deviceID;
    private OSHParameterCollection comConfig;

    private ComRegistry comRegistry;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param comConfig
     */
    public CALDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection comConfig) {
        super(osh);

        this.deviceID = deviceID;
        this.comConfig = comConfig;
    }


    @Override
    protected IOSHCom getOSH() {
        return (IOSHCom) super.getOSH();
    }

    @Override
    public UUID getUUID() {
        return this.deviceID;
    }

    protected ComRegistry getComRegistry() {
        return this.comRegistry;
    }

    /**
     * @return the driverConfig
     */
    public OSHParameterCollection getComConfig() {
        return this.comConfig;
    }

    /**
     * @param comConfig the comConfig to set
     */
    public void setComConfig(OSHParameterCollection comConfig) {
        this.comConfig = comConfig;
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
        this.comRegistry = this.getOSH().getComRegistry();

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
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
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
