package osh.datatypes.ea;

import osh.datatypes.ea.interfaces.IPrediction;

import java.util.TreeMap;

/**
 * Represents a prediciton about temperature states
 *
 * @author Sebastian Kramer
 */
public class TemperaturePrediction implements IPrediction {

    private final TreeMap<Long, Double> temperatureStates;

    /**
     * Constructs a temperature prediction with the given temperature states
     *
     * @param temperatureStates a map of time and temperature states
     */
    public TemperaturePrediction(TreeMap<Long, Double> temperatureStates) {
        super();
        this.temperatureStates = temperatureStates;
    }

    /**
     * Returns the predicted temperature states.
     *
     * @return a map of time and temperature states
     */
    public TreeMap<Long, Double> getTemperatureStates() {
        return this.temperatureStates;
    }

    @Override
    public String toString() {
        return this.temperatureStates.toString();
    }
}
