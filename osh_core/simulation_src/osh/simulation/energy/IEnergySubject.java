package osh.simulation.energy;

import osh.datatypes.commodity.AncillaryMeterState;
import osh.esc.LimitedCommodityStateMap;
import osh.registry.interfaces.IProvidesIdentity;

/**
 * Marks that the entity consumes or produces energy.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public interface IEnergySubject extends IProvidesIdentity {

    /**
     * Is invoked by the EnergySimulationCore (which is handled by the SimulationEngine or the EnergyManagementProblem)
     * at every time tick to GET the energy commodity states of the subject
     * (i.e., obtain new states and energy exchange)
     * <p>
     * entity consumes energy: positive values <br />
     * entity produces energy: negative values
     */
    LimitedCommodityStateMap getCommodityOutputStates();

    /**
     * Is invoked by the EnergySimulationCore (which is handled by the SimulationEngine or the EnergyManagementProblem)
     * at every time tick to SET the energy commodity states of the subject
     * (i.e. provide new states and energy exchange in order to calculate the next state
     * in onNextTimeTick() called by triggerSubject() or calculateNextStep(), respectively)
     * <p>
     * entity consumes energy: positive values (from perspective of the receiving subject) <br />
     * entity produces energy: negative values (from perspective of the receiving subject)
     */
    void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
            AncillaryMeterState ancillaryMeterState);

}
