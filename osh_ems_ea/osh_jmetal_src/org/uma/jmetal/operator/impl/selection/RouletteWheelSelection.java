package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.Comparator;
import java.util.List;

/**
 * Applies a roulette wheel selection to return the best solution chosen randomly between all solutions in the list
 * proportional to their fitness.
 *
 * @author Sebastian Kramer
 */
public class RouletteWheelSelection<S extends Solution<?>> implements SelectionOperator<List<S>, S> {

    private static final long serialVersionUID = -6295833917346199900L;
    private final JMetalRandom randomGenerator;
    private final Comparator<S> comparator;

    /**
     * Constructor
     * Creates a new RouletteWheelSelection operator using a DominanceComparator
     */
    public RouletteWheelSelection(Comparator<S> comparator) {
        this.comparator = comparator;
        this.randomGenerator = JMetalRandom.getInstance();
    }

    public RouletteWheelSelection() {
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
        S best = solutionList.get(0);
        S worst = solutionList.get(solutionList.size() - 1);

        double[] wheel = new double[solutionList.size()];
        double sumOfFitness = 0.0;

        //if minProblem just change to maxproblem by *(-1)
        if (best.getObjective(0) < worst.getObjective(0)) {
            double negCorrection = 0.0;

            if (worst.getObjective(0) > 0) {
                negCorrection = worst.getObjective(0) + 0.00001;
            }

            for (int i = 0; i < wheel.length; i++) {
                sumOfFitness += (-solutionList.get(i).getObjective(0) + negCorrection);
                wheel[i] = sumOfFitness;
            }
        } else {
            for (int i = 0; i < wheel.length; i++) {
                double negCorrection = 0.0;
                if (worst.getObjective(0) < 0) {
                    negCorrection = 0.0001 - worst.getObjective(0);
                }
                sumOfFitness += solutionList.get(i).getObjective(0) + negCorrection;
                wheel[i] = sumOfFitness;
            }
        }

        //if all have the same fitness just select randomly
        if (sumOfFitness == 0) {
            return SolutionListUtils.selectNRandomDifferentSolutions(1, solutionList).get(0);
        }

        for (int i = 0; i < wheel.length; i++) {
            wheel[i] /= sumOfFitness;
        }

        double selectedValue = this.randomGenerator.nextDouble();

        for (int i = 0; i < wheel.length; i++) {
            if (wheel[i] >= selectedValue) {
                return solutionList.get(i);
            }
        }

        //should never happen but return random if it does
        return SolutionListUtils.selectNRandomDifferentSolutions(1, solutionList).get(0);
    }
}
