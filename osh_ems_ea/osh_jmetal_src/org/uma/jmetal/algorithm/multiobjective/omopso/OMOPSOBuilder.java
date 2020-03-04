package org.uma.jmetal.algorithm.multiobjective.omopso;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.NonUniformMutation;
import org.uma.jmetal.operator.impl.mutation.UniformMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

/**
 * Class implementing the OMOPSO algorithm
 */
public class OMOPSOBuilder implements AlgorithmBuilder<OMOPSO> {
    protected final DoubleProblem problem;
    protected final SolutionListEvaluator<DoubleSolution> evaluator;

    private int swarmSize = 100;
    private int archiveSize = 100;
    private int maxIterations = 25000;

    private UniformMutation uniformMutation;
    private NonUniformMutation nonUniformMutation;

    public OMOPSOBuilder(DoubleProblem problem, SolutionListEvaluator<DoubleSolution> evaluator) {
        this.evaluator = evaluator;
        this.problem = problem;
    }

    /* Getters */
    public int getArchiveSize() {
        return this.archiveSize;
    }

    public OMOPSOBuilder setArchiveSize(int archiveSize) {
        this.archiveSize = archiveSize;

        return this;
    }

    public int getSwarmSize() {
        return this.swarmSize;
    }

    public OMOPSOBuilder setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;

        return this;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public OMOPSOBuilder setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public UniformMutation getUniformMutation() {
        return this.uniformMutation;
    }

    public OMOPSOBuilder setUniformMutation(MutationOperator<DoubleSolution> uniformMutation) {
        this.uniformMutation = (UniformMutation) uniformMutation;

        return this;
    }

    public NonUniformMutation getNonUniformMutation() {
        return this.nonUniformMutation;
    }

    public OMOPSOBuilder setNonUniformMutation(MutationOperator<DoubleSolution> nonUniformMutation) {
        this.nonUniformMutation = (NonUniformMutation) nonUniformMutation;

        return this;
    }

    public OMOPSO build() {
        return new OMOPSO(this.problem, this.evaluator, this.swarmSize, this.maxIterations, this.archiveSize, this.uniformMutation,
                this.nonUniformMutation);
    }
}
