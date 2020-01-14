package osh.mgmt.ipp.watertank;

import osh.datatypes.ea.interfaces.IPrediction;

import java.util.TreeMap;

/**
 * @author Sebastian Kramer
 */
public class HotWaterTankPrediction implements IPrediction {

    TreeMap<Long, Double> temperatureStates;

    /**
     * @param temperatureStates
     */
    public HotWaterTankPrediction(TreeMap<Long, Double> temperatureStates) {
        super();
        this.temperatureStates = temperatureStates;
    }

    public TreeMap<Long, Double> getTemperatureStates() {
        return this.temperatureStates;
    }

    public void setTemperatureStates(TreeMap<Long, Double> temperatureStates) {
        this.temperatureStates = temperatureStates;
    }

    @Override
    public String toString() {
        return this.temperatureStates.toString();
    }
}
