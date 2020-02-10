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
     * Resets the internal state of the subject to the start of the optimization loop.
     *
     * @param interdependentStartingTime the start-time of the optimization loop
     * @param stepSize the step-size in which the optimization loop advances time
     * @param calculateLoadProfile flag that indicates that devices should create load profiles throughout the
     *                             interdependent calculation (currently only needed for the GUI)
     * @param keepPrediction       flag that indicates that predicted values throughout the optimization (e.g.
     *                             watertemperatures) should be kept
     */
    void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean calculateLoadProfile,
            boolean keepPrediction
    );

    /**
     * Returns the farthest point-of-time the subject indicates should be considered for the optimization loop.
     *
     * @return the farthest point-of-time the subject indicates should be considered for the optimization loop
     */
    long getOptimizationHorizon();

    /**
     * Calculate the next time step for the subject in the optimization loop.
     */
    void calculateNextStep();

    /**
     * Returns if the subject needs the virtual ancillary meter for it's calculation.
     *
     * @return true if the subject needs the virtual ancillary meter for it's calculation
     */
    boolean isNeedsAncillaryMeterState();

    /**
     * Returns if the subject reacts to any input information inside the optimization loop.
     *
     * @return true if the subject reacts to any input information
     */
    boolean isReactsToInputStates();
}
