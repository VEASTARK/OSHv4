package org.uma.jmetal.operator.impl.localsearch;

import org.uma.jmetal.operator.LocalSearchOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import java.util.Comparator;

/**
 * This class implements a basic local search operator based in the use of a mutation operator.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class BasicLocalSearch<S extends Solution<?>> implements LocalSearchOperator<S> {

    private final Problem<S> problem;
    private final int improvementRounds;
    private final Comparator<S> constraintComparator;
    private final Comparator<S> comparator;

    private final MutationOperator<S> mutationOperator;
    private final RandomGenerator<Double> randomGenerator;
    private int evaluations;
    private int numberOfImprovements;
    private int numberOfNonComparableSolutions;

    /**
     * Constructor. Creates a new local search object.
     *
     * @param improvementRounds number of iterations
     * @param mutationOperator  mutation operator
     * @param comparator        comparator to determine which solution is the best
     * @param problem           problem to resolve
     */
    public BasicLocalSearch(int improvementRounds, MutationOperator<S> mutationOperator,
                            Comparator<S> comparator, Problem<S> problem) {
        this(improvementRounds, mutationOperator, comparator, problem,
                () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor. Creates a new local search object.
     *
     * @param improvementRounds number of iterations
     * @param mutationOperator  mutation operator
     * @param comparator        comparator to determine which solution is the best
     * @param problem           problem to resolve
     * @param randomGenerator   the {@link RandomGenerator} to use when we must choose between
     *                          equivalent solutions
     */
    public BasicLocalSearch(int improvementRounds, MutationOperator<S> mutationOperator,
                            Comparator<S> comparator, Problem<S> problem, RandomGenerator<Double> randomGenerator) {
        this.problem = problem;
        this.mutationOperator = mutationOperator;
        this.improvementRounds = improvementRounds;
        this.comparator = comparator;
        this.constraintComparator = new OverallConstraintViolationComparator<>();

        this.randomGenerator = randomGenerator;
        this.numberOfImprovements = 0;
    }

    /**
     * Executes the local search.
     *
     * @param solution The solution to improve
     * @return An improved solution
     */
    @SuppressWarnings("unchecked")
    public S execute(S solution) {
        int best;
        this.evaluations = 0;
        this.numberOfNonComparableSolutions = 0;

        int i = 0;
        while (i < this.improvementRounds) {
            S mutatedSolution = this.mutationOperator.execute((S) solution.copy());

            this.problem.evaluate(mutatedSolution);
            this.evaluations++;
            if (this.problem.getNumberOfConstraints() > 0) {
                best = this.constraintComparator.compare(mutatedSolution, solution);
                if (best == 0) {
                    best = this.comparator.compare(mutatedSolution, solution);
                }
            } else {
                best = this.comparator.compare(mutatedSolution, solution);
            }

            if (best == -1) {
                solution = mutatedSolution;
                this.numberOfImprovements++;
            } else if (best == 1) {
                // Current solution is best
            } else {
                this.numberOfNonComparableSolutions++;

                if (this.randomGenerator.getRandomValue() < 0.5) {
                    solution = mutatedSolution;
                }
            }
            i++;
        }
        return (S) solution.copy();
    }

    /**
     * Returns the number of evaluations
     */
    public int getEvaluations() {
        return this.evaluations;
    }

    @Override
    public int getNumberOfImprovements() {
        return this.numberOfImprovements;
    }

    @Override
    public int getNumberOfNonComparableSolutions() {
        return this.numberOfNonComparableSolutions;
    }
}
