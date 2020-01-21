package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.details.common.TemperatureDetails;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.registry.interfaces.IDataRegistryListener;
import osh.registry.interfaces.IHasState;

import java.util.UUID;


/**
 * @author Ingo Mauser, Jan Mueller
 */
public abstract class WaterTankDriver
        extends HALDeviceDriver
        implements IDataRegistryListener, IHasState {

//	private WaterTank waterTank;

    protected LoadProfileCompressionTypes compressionType;
    protected int compressionValue;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public WaterTankDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        try {
            this.compressionType = LoadProfileCompressionTypes.valueOf(this.getDriverConfig().getParameter("compressionType"));
        } catch (Exception e) {
            this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
            this.getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
        }

        try {
            this.compressionValue = Integer.parseInt(this.getDriverConfig().getParameter("compressionValue"));
        } catch (Exception e) {
            this.compressionValue = 100;
            this.getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
        }
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getDriverRegistry().subscribe(TemperatureDetails.class, this.getDeviceID(),this);

        StaticCompressionExchange observerExchange =
                new StaticCompressionExchange(this.getDeviceID(), this.getTimer().getUnixTime(), this.compressionType, this.compressionValue);
        this.notifyObserver(observerExchange);
    }
}



