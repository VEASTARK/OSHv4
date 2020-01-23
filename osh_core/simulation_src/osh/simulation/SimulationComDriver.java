package osh.simulation;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;

import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public abstract class SimulationComDriver extends CALComDriver {

    private ISimulationActionLogger simLogger;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public SimulationComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);
    }

    @Override
    public final UUID getUUID() {
        return super.getUUID();
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        //NOTHING
    }


    /**
     * Please use only for logging stuff
     */
    public PriceSignal getPriceSignal(AncillaryCommodity c) {
        return null;
    }

    /**
     * Please use only for logging stuff
     */
    public PowerLimitSignal getPowerLimitSignal(AncillaryCommodity c) {
        return null;
    }


    protected ISimulationActionLogger getSimLogger() {
        return this.simLogger;
    }

    public void setSimulationActionLogger(ISimulationActionLogger simulationLogger) {
        this.simLogger = simulationLogger;
    }

}
