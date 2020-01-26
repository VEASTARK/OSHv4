package osh.esc;

import osh.simulation.energy.IEnergySubject;

/**
 * Marks that the entity (represented by its ProblemPart) consumes or produces energy
 * that is interdependent to other devices
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public interface IOCEnergySubject extends IEnergySubject {

    /**
     * Set initial values
     *
     * @param maxReferenceTime
     * @param stepSize
     * @param calculateLoadProfile indicates that devices should create load profiles throughout the interdependent calculation (currently only needed for the GUI)
     * @param keepPrediction       indicates that predicted values throughout the optimization (e.g. waterTemperatures) should be kept
     */
    void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean calculateLoadProfile,
            boolean keepPrediction
    );

    void prepareForDeepCopy();

    long getOptimizationHorizon();

    /**
     * Calculate next time step (similar to triggerSubject in SimulationEngine)
     */
    void calculateNextStep();

    boolean isNeedsAncillaryMeterState();

    boolean isReactsToInputStates();
}
