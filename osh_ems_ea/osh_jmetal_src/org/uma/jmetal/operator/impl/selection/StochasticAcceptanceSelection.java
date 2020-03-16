package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

/**
 * Applies a fitness proportionate selection to return the best solution chosen randomly between all solutions in the
 * list with the winning solution chosen by stochastic acceptance.
 *
 * @author Sebastian Kramer
 */
public class StochasticAcceptanceSelection<S extends Solution<?>> implements SelectionOperator<List<S>, S> {

    private static final long serialVersionUID = -6295833917346199900L;
    private final JMetalRandom randomGenerator;
    private final Comparator<S> comparator;

    /**
     * Constructor
     * Creates a new RouletteWheelSelection operator using a DominanceComparator
     */
    public StochasticAcceptanceSelection(Comparator<S> comparator) {
        this.comparator = comparator;
        this.randomGenerator = JMetalRandom.getInstance();
    }

    public StochasticAcceptanceSelection() {
        this(new DominanceComparator<>());
    }

    /**
     * Execute() method
     *
     * @param solutionList a list of solutions to select from
     * @return
     */
    @Override
    public S execute(List<S> solutionList) {
        if (null == solutionList) {
            throw new JMetalException("The solution list is null");
        } else if (solutionList.isEmpty()) {
            throw new JMetalException("The solution list is empty");
        } else if (solutionList.get(0).getNumberOfObjectives() != 1) {
            throw new JMetalException("This operator can only be applies to single-objective problems");
        }

        solutionList.sort(this.comparator);
        double bestFitness = solutionList.get(0).getObjective(0);
        double worstFitness = solutionList.get(solutionList.size() - 1).getObjective(0);
        //stochastic acceptance is based on fitness/bestFitness so we have to correct for minProblems and bestFitness < 0
        DoubleUnaryOperator correctionFunction = d -> d;

        //we need to shift and/or multiply the fitness values so we can calculate the relative fitness of candidate
        // solutions to the best fitness easier
        if (bestFitness < worstFitness) {
            if (worstFitness > 0) {
                correctionFunction = d -> -(d + + worstFitness + 0.00001);
            } else {
                correctionFunction = d -> -d;
            }
        } else {
            if (worstFitness < 0) {
                correctionFunction = d -> d + (0.0001 - worstFitness);
            }
        }
        bestFitness = correctionFunction.applyAsDouble(bestFitness);

        int counter = 0;

        do {
            S candidate = SolutionListUtils.selectNRandomDifferentSolutions(1, solutionList).get(0);
            //we accept the randomly selected solution with probability p = f_candidate/f_best
            if (this.randomGenerator.nextDouble() < (correctionFunction.applyAsDouble(candidate.getObjective(0))) / bestFitness) {
                return candidate;
            }
            counter++;

        } while (counter < solutionList.size());

        //shortcut so we do not enter into a very long loop
        return SolutionListUtils.selectNRandomDifferentSolutions(1, solutionList).get(0);
    }
}
