package osh.mgmt.globalcontroller.jmetal.builder;

import org.uma.jmetal.solution.Solution;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.AncillaryCommodityLoadProfile;

import java.util.List;
import java.util.TreeMap;

/**
 * Represents a collection object of all ea-results inclusive the logging and/or debugging information.
 *
 * @author Sebastian Kramer
 */
public class EAScheduleResult {

    private final TreeMap<Long, Double> predictedHotWaterTankTemperature;
    private final TreeMap<Long, Double> predictedHotWaterDemand;
    private final TreeMap<Long, Double> predictedHotWaterSupply;
    private final List<Schedule> schedules;
    private final AncillaryCommodityLoadProfile ancillaryMeter;
    private final Solution<?> solution;
    private final boolean hasGUIObjects;

    /**
     * Constructs this data collection with the given values.
     *
     * @param predictedHotWaterTankTemperature the map of the predicted hot-water tank temperature
     * @param predictedHotWaterDemand the map of the predicted hot-water demand
     * @param predictedHotWaterSupply the map of the predicted hot-water supply
     * @param schedules the schedules of all involved devices
     * @param ancillaryMeter the resulting ancillary meter of the solution
     * @param solution the solution
     */
    public EAScheduleResult(TreeMap<Long, Double> predictedHotWaterTankTemperature,
                            TreeMap<Long, Double> predictedHotWaterDemand,
                            TreeMap<Long, Double> predictedHotWaterSupply, List<Schedule> schedules,
                            AncillaryCommodityLoadProfile ancillaryMeter, Solution<?> solution) {
        this.predictedHotWaterTankTemperature = predictedHotWaterTankTemperature;
        this.predictedHotWaterDemand = predictedHotWaterDemand;
        this.predictedHotWaterSupply = predictedHotWaterSupply;
        this.schedules = schedules;
        this.ancillaryMeter = ancillaryMeter;
        this.solution = solution;
        this.hasGUIObjects =
                !predictedHotWaterTankTemperature.isEmpty() || !predictedHotWaterDemand.isEmpty()
                        || !predictedHotWaterSupply.isEmpty();
    }

    /**
     * Returns the map of the predicted hot-water tank temperature.
     *
     * @return the map of the predicted hot-water tank temperature
     */
    public TreeMap<Long, Double> getPredictedHotWaterTankTemperature() {
        return this.predictedHotWaterTankTemperature;
    }

    /**
     * Returns the map of the predicted hot-water demand.
     *
     * @return the map of the predicted hot-water demand
     */
    public TreeMap<Long, Double> getPredictedHotWaterDemand() {
        return this.predictedHotWaterDemand;
    }

    /**
     * Returns the map of the predicted hot-water supply.
     *
     * @return the map of the predicted hot-water supply
     */
    public TreeMap<Long, Double> getPredictedHotWaterSupply() {
        return this.predictedHotWaterSupply;
    }

    /**
     * Returns the schedules of all involved devices.
     *
     * @return the schedules of all involved devices
     */
    public List<Schedule> getSchedules() {
        return this.schedules;
    }

    /**
     * Returns the resulting ancillary meter of the solution.
     *
     * @return the resulting ancillary meter of the solution
     */
    public AncillaryCommodityLoadProfile getAncillaryMeter() {
        return this.ancillaryMeter;
    }

    /**
     * Returns the solution.
     *
     * @return the solution
     */
    public Solution<?> getSolution() {
        return this.solution;
    }

    /**
     * Returns the flag signalling that gui objects are contained in this result.
     *
     * @return true flag signalling that gui objects are contained in this result
     */
    public boolean isHasGUIObjects() {
        return this.hasGUIObjects;
    }
}