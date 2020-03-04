package org.uma.jmetal.algorithm.multiobjective.paes;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class PAESBuilder<S extends Solution<?>> implements AlgorithmBuilder<PAES<S>> {
    private final Problem<S> problem;

    private int archiveSize;
    private int maxEvaluations;
    private int biSections;

    private MutationOperator<S> mutationOperator;

    public PAESBuilder(Problem<S> problem) {
        this.problem = problem;
    }

    public PAES<S> build() {
        return new PAES<>(this.problem, this.archiveSize, this.maxEvaluations, this.biSections, this.mutationOperator);
    }

    /*
     * Getters
     */
    public Problem<S> getProblem() {
        return this.problem;
    }

    public int getArchiveSize() {
        return this.archiveSize;
    }

    public PAESBuilder<S> setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public PAESBuilder<S> setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public int getBiSections() {
        return this.biSections;
    }

    public PAESBuilder<S> setBiSections(int biSections) {
        this.biSections = biSections;

        return this;
    }

    public MutationOperator<S> getMutationOperator() {
        return this.mutationOperator;
    }

    public PAESBuilder<S> setMutationOperator(MutationOperator<S> mutation) {
        this.mutationOperator = mutation;

        return this;
    }
}
