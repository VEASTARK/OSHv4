package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.driver.thermal.FactorisedBasicWaterTank;
import osh.eal.hal.exceptions.HALException;
import osh.esc.LimitedCommodityStateMap;
import osh.simulation.DeviceSimulationDriver;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public abstract class WaterTankSimulationDriver
        extends DeviceSimulationDriver {

    protected FactorisedBasicWaterTank waterTank;

    /**
     * CONSTRUCTOR
     */
    public WaterTankSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);
    }

    @Override
    public LimitedCommodityStateMap getCommodityOutputStates() {
        LimitedCommodityStateMap map =
                new LimitedCommodityStateMap(this.usedCommodities);
        for (Commodity c : this.usedCommodities) {
            map.setTemperature(c, this.waterTank != null ? this.waterTank.getCurrentWaterTemperature() : 0.0);
        }
        return map;
    }
}
