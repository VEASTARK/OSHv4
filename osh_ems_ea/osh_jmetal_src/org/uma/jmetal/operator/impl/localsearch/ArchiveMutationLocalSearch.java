package org.uma.jmetal.operator.impl.localsearch;

import org.uma.jmetal.operator.LocalSearchOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.impl.OverallConstraintViolationComparator;

import java.util.Comparator;


/**
 * This class implements a local search operator based in the use of a mutation operator. An archive
 * is used to store the non-dominated solutions found during the search.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class ArchiveMutationLocalSearch<S extends Solution<?>> implements LocalSearchOperator<S> {

    private final Problem<S> problem;
    private final Archive<S> archive;
    private final int improvementRounds;
    private final Comparator<S> constraintComparator;
    private final Comparator<S> dominanceComparator;

    private final MutationOperator<S> mutationOperator;
    private int evaluations;

    private int numberOfImprovements;
    private int numberOfNonComparableSolutions;

    /**
     * Constructor. Creates a new local search object.
     *
     * @param improvementRounds number of iterations
     * @param mutationOperator  mutation operator
     * @param archive           archive to store non-dominated solution
     * @param problem           problem to resolve
     */
    public ArchiveMutationLocalSearch(int improvementRounds, MutationOperator<S> mutationOperator,
                                      Archive<S> archive, Problem<S> problem) {
        this.problem = problem;
        this.mutationOperator = mutationOperator;
        this.improvementRounds = improvementRounds;
        this.archive = archive;
        this.dominanceComparator = new DominanceComparator<>();
        this.constraintComparator = new OverallConstraintViolationComparator<>();

        this.numberOfImprovements = 0;
        this.numberOfNonComparableSolutions = 0;
    }

    /**
     * Executes the local search.
     *
     * @param solution The solution to improve
     * @return The improved solution
     */
    @SuppressWarnings("unchecked")
    public S execute(S solution) {
        int i = 0;
        int best;
        this.evaluations = 0;
        this.numberOfNonComparableSolutions = 0;

        while (i < this.improvementRounds) {
            S mutatedSolution = this.mutationOperator.execute((S) solution.copy());

            this.problem.evaluate(mutatedSolution);
            this.evaluations++;

            if (this.problem.getNumberOfConstraints() > 0) {
                best = this.constraintComparator.compare(mutatedSolution, solution);
                if (best == 0) {
                    best = this.dominanceComparator.compare(mutatedSolution, solution);
                }
            } else {
                best = this.dominanceComparator.compare(mutatedSolution, solution);
            }

            if (best == -1) {
                solution = mutatedSolution;
                this.numberOfImprovements++;
            } else if (best == 1) {
            } else {
                this.numberOfNonComparableSolutions++;
                this.archive.add(mutatedSolution);
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
