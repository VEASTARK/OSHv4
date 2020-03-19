package org.uma.jmetal.algorithm.multiobjective.nsgaii;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.Comparator;
import java.util.List;

/**
 * This class shows a version of NSGA-II having a stopping condition depending on run-time
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class NSGAIIStoppingByTime<S extends Solution<?>> extends NSGAII<S> {
    private final long initComputingTime;
    private final long thresholdComputingTime;

    /**
     * Constructor
     */
    public NSGAIIStoppingByTime(Problem<S> problem, int populationSize,
                                long maxComputingTime, int matingPoolSize, int offspringPopulationSize,
                                CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutationOperator,
                                SelectionOperator<List<S>, S> selectionOperator, Comparator<S> dominanceComparator,
                                SolutionListEvaluator<S> evaluator, IEALogger eaLogger) {
        super(problem, populationSize, matingPoolSize, offspringPopulationSize,
                crossoverOperator, mutationOperator,
                selectionOperator, dominanceComparator, evaluator, eaLogger);

        this.initComputingTime = System.currentTimeMillis();
        this.thresholdComputingTime = maxComputingTime;
    }

    @Override
    protected boolean isStoppingConditionReached() {
        long currentComputingTime = System.currentTimeMillis() - this.initComputingTime;
        return currentComputingTime > this.thresholdComputingTime;
    }
}
