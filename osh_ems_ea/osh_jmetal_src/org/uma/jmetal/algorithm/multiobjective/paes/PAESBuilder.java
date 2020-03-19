package org.uma.jmetal.algorithm.multiobjective.paes;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.AlgorithmBuilder;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class PAESBuilder<S extends Solution<?>> implements AlgorithmBuilder<PAES<S>> {
    private final Problem<S> problem;
    private final IEALogger eaLogger;

    private int archiveSize;
    private int maxEvaluations;
    private int biSections;

    private MutationOperator<S> mutationOperator;

    public PAESBuilder(Problem<S> problem, IEALogger eaLogger) {
        this.problem = problem;
        this.eaLogger = eaLogger;
    }

    public PAES<S> build() {
        PAES<S> algorithm = new PAES<>(this.problem, this.archiveSize, this.biSections, this.mutationOperator, this.eaLogger);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.archiveSize, this.maxEvaluations));
        return algorithm;
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
