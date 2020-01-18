package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.EventExchange;
import osh.eal.hal.HALDeviceDriver;
import osh.registry.interfaces.IEventTypeReceiver;
import osh.registry.interfaces.IHasState;

import java.util.Collections;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class ApplianceDriver
        extends HALDeviceDriver
        implements IEventTypeReceiver, IHasState {

    // TEMPORAL DEGREE OF FREEDOM

    /**
     * Max 1stTemporalDoF in ticks for generation of DoF
     */
    private int deviceMax1stTDof;

    /**
     * Max 2ndTemporalDoF in ticks for generation of DoF
     */
    private int deviceMax2ndTDof;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public ApplianceDriver(IOSH osh,
                           UUID deviceID, OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        // IMPORTANT:
        // if (getDeviceType() == DeviceTypes.WASHINGMACHINE) <-- does NOT work!
    }


    /**
     * Is called when all drivers, OC-units are up
     *
     * @throws OSHException
     */
    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // set meters
        this.setDataSourcesUsed(this.getMeterUuids());
        this.setDataSourcesConfigured(Collections.singleton(this.getDeviceID()));
    }


    /**
     * Is called to update the power values according to values metered by some other device (e.g. smart plug)
     */
    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) {
        // our device? then: build observer exchange

    }


    // ### GETTER ###
    // used only within this class
    protected int getDeviceMax1stDof() {
        return this.deviceMax1stTDof;
    }

    // used by FutureAppliance
    protected int getDeviceMax2ndDof() {
        return this.deviceMax2ndTDof;
    }


    // ### UNIMPORTANT METHODS ###

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

}
