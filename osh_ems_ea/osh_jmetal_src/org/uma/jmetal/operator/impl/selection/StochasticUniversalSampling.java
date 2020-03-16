package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import javax.management.JMException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Applies a stochastic universal sampling selection to return the best solution chosen randomly between all solutions
 * in the list proportional to their fitness.
 *
 * @author Sebastian Kramer
 */
public class StochasticUniversalSampling<S extends Solution<?>> implements SelectionOperator<List<S>, List<S>> {

    private static final long serialVersionUID = 7113383280941053657L;
    private final JMetalRandom randomGenerator;
    private final Comparator<S> comparator;
    private final int numberToSelect;

    /**
     * Constructor
     * Creates a new StochasticUniversalSampling operator
     *
     * @throws JMException
     */
    public StochasticUniversalSampling(int numberToSelect, Comparator<S> comparator) {
        this.numberToSelect = numberToSelect;
        this.comparator = comparator;
        this.randomGenerator = JMetalRandom.getInstance();
    }

    public StochasticUniversalSampling(int numberToSelect) {
        this(numberToSelect, new DominanceComparator<>());
    }

    /**
     * Execute() method
     *
     * @param solutionList a list of solutions to select from
     * @return
     */
    @Override
    public List<S> execute(List<S> solutionList) {
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
            return SolutionListUtils.selectNRandomDifferentSolutions(this.numberToSelect, solutionList);
        }

        for (int i = 0; i < wheel.length; i++) {
            wheel[i] /= sumOfFitness;
        }

        double[] selectedValues = new double[this.numberToSelect];
        double valueDistance = 1.0d / this.numberToSelect;
        double firstValue = this.randomGenerator.nextDouble(0.0, valueDistance);
        selectedValues[0] = firstValue;

        for (int i = 1; i < this.numberToSelect; i++) {
            selectedValues[i] = firstValue + i * valueDistance;
        }

        int selectPointer = 0;
        int counter = 0;
        List<S> result = new ArrayList<>(this.numberToSelect);

        while (selectPointer < this.numberToSelect) {
            if (wheel[counter] >= selectedValues[selectPointer]) {
                result.add(solutionList.get(counter));
                selectPointer++;
            } else
                counter++;
        }

        return result;
    }
}