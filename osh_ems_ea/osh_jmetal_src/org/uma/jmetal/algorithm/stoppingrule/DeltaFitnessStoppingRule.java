package org.uma.jmetal.algorithm.stoppingrule;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import osh.utils.string.ParameterConstants;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Represents a stopping condition based upon minimum delta fitness improvement that has to be reached for the
 * condition to not trigger.
 *
 * @author Sebastian Kramer
 */
public class DeltaFitnessStoppingRule extends StoppingRule {

    int noObjectives;
    Comparator<Solution<?>>[] comparators;
    private final double minDeltaFitnessPercent;
    private final int maxGenerationsDeltaFitnessViolated;
    private Double[] lastGenerationBestFitness;
    private int[] generationsDeltaFitnessViolated;

    /**
     * Constructs this stopping rule with the given parameter collection.
     *
     * @param parameters the parameters for the stopping condition
     */
    public DeltaFitnessStoppingRule(Map<String, Object> parameters) {
        super(parameters);

        if (this._parameters.get(ParameterConstants.EA.minDeltaFitnessPercent) != null)
            this.minDeltaFitnessPercent = (double) parameters.get(ParameterConstants.EA.minDeltaFitnessPercent);
        else {
            throw new JMetalException("no minDeltaFitnessPercent in parameters");
        }

        if (this._parameters.get(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated) != null)
            this.maxGenerationsDeltaFitnessViolated = (int) parameters.get(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated);
        else {
            throw new JMetalException("no maxGenerationsDeltaFitnessViolated in parameters");
        }
    }

    /**
     * Constructs this stopping rule with the explicit paramters.
     *
     * @param minDeltaFitnessPercent the minimum fitness improvement to not be considered violating the rule
     * @param maxGenerationsDeltaFitnessViolated the amount of generations needed to violate the minimum improvement
     *                                           to trigger this stopping condition
     */
    public DeltaFitnessStoppingRule(double minDeltaFitnessPercent, int maxGenerationsDeltaFitnessViolated) {
        super(Collections.emptyMap());

        this.minDeltaFitnessPercent = minDeltaFitnessPercent;
        this.maxGenerationsDeltaFitnessViolated = maxGenerationsDeltaFitnessViolated;
    }

    @SuppressWarnings("unchecked")
    private void init() {
        this.lastGenerationBestFitness = new Double[this.noObjectives];
        Arrays.fill(this.lastGenerationBestFitness, null);

        this.comparators = (Comparator<Solution<?>>[]) Array.newInstance(ObjectiveComparator.class, this.noObjectives);

        for (int i = 0; i < this.noObjectives; i++) {
            this.comparators[i] = new ObjectiveComparator<>(i);
        }

        this.generationsDeltaFitnessViolated = new int[this.noObjectives];
    }

    /**
     * Checks and returns if the stopping condition is reached based upon the given algorithm state.
     *
     * <p>
     * Condition will be true, if: <br/>
     * <ul>
     * <li> delta fitness change between generations was smaller then required amount for the required generations
     * <li> for multi-objective-optimisation if the above condition is true for all objectives
     * </ul>
     * <p>
     * It is assumed that the optimisation cannot return worse fitness values for generation n+1 then for generation n
     *
     * @param problem the underlying problem of the algorithm
     * @param generation the number of generations that have passed since the start of the execution
     * @param evaluations the number of evaltuations that have been done since the start of the execution
     * @param currentSortedSolutions the current (sorted) set of solutions
     *
     * @return true if the required minimum improvement of fitness is not reached
     */
    @Override
    public <S extends Solution<?>> boolean checkIfStop(Problem<S> problem, int generation, int evaluations,
                                                       List<S> currentSortedSolutions) {

        if (this.lastGenerationBestFitness == null) {
            this.noObjectives = problem.getNumberOfObjectives();
            this.init();
        }

        if (currentSortedSolutions.isEmpty()) return false;

        boolean firstTime = false;
        double[] deltaFitness = new double[this.noObjectives];
        double[] thisGenerationBestFitness = new double[this.noObjectives];

        for (int i = 0; i < this.noObjectives; i++) {
            currentSortedSolutions.sort(this.comparators[i]);
            Solution<?> best = currentSortedSolutions.get(0);

            if (this.lastGenerationBestFitness[i] == null) {
                this.lastGenerationBestFitness[i] = best.getObjective(0);
                firstTime = true;
            }

            thisGenerationBestFitness[i] = best.getObjective(0);
            deltaFitness[i] = Math.abs((Math.abs(thisGenerationBestFitness[i])
                    - Math.abs(this.lastGenerationBestFitness[i])) / Math.abs(this.lastGenerationBestFitness[i]));
        }

        if (firstTime) {
            return false;
        }

        boolean stop = true;

        for (int i = 0; i < this.noObjectives; i++) {
            if (deltaFitness[i] >= this.minDeltaFitnessPercent) {
                this.generationsDeltaFitnessViolated[i] = 0;
                this.lastGenerationBestFitness[i] = thisGenerationBestFitness[i];
                stop = false;
            } else {
                this.generationsDeltaFitnessViolated[i]++;
                if (this.generationsDeltaFitnessViolated[i] < this.maxGenerationsDeltaFitnessViolated)
                    stop = false;
                else {
                    this._msg = "Optimisation stopped after violating minDeltaFitness for " + Arrays.toString(this.generationsDeltaFitnessViolated) + " generations.";
                }
            }
        }

        return stop;
    }
}
