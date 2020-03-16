package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 * This class implements a non-uniform mutation operator.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class NonUniformMutation implements MutationOperator<DoubleSolution> {
    private final RandomGenerator<Double> randomGenenerator;
    private double perturbation;
    private int maxIterations;
    private double mutationProbability;
    private int currentIteration;

    /**
     * Constructor
     */
    public NonUniformMutation(double mutationProbability, double perturbation, int maxIterations) {
        this(mutationProbability, perturbation, maxIterations, () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor
     */
    public NonUniformMutation(double mutationProbability, double perturbation, int maxIterations, RandomGenerator<Double> randomGenenerator) {
        this.perturbation = perturbation;
        this.mutationProbability = mutationProbability;
        this.maxIterations = maxIterations;

        this.randomGenenerator = randomGenenerator;
    }

    /* Getters */
    public double getPerturbation() {
        return this.perturbation;
    }

    public void setPerturbation(double perturbation) {
        this.perturbation = perturbation;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public double getMutationProbability() {
        return this.mutationProbability;
    }

    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    public int getCurrentIteration() {
        return this.currentIteration;
    }

    /* Setters */
    public void setCurrentIteration(int currentIteration) {
        if (currentIteration < 0) {
            throw new JMetalException("Iteration number cannot be a negative value: " + currentIteration);
        }

        this.currentIteration = currentIteration;
    }

    /**
     * Execute() method
     */
    @Override
    public DoubleSolution execute(DoubleSolution solution) {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        this.doMutation(this.mutationProbability, solution);

        return solution;
    }

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation setProbability
     * @param solution    The solution to mutate
     */
    public void doMutation(double probability, DoubleSolution solution) {
        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            if (this.randomGenenerator.getRandomValue() < probability) {
                double rand = this.randomGenenerator.getRandomValue();
                double tmp;

                if (rand <= 0.5) {
                    tmp = this.delta(solution.getUnboxedUpperBound(i) - solution.getUnboxedVariableValue(i),
                            this.perturbation);
                } else {
                    tmp = this.delta(solution.getUnboxedLowerBound(i) - solution.getUnboxedVariableValue(i),
                            this.perturbation);
                }
                tmp += solution.getUnboxedVariableValue(i);

                if (tmp < solution.getUnboxedLowerBound(i)) {
                    tmp = solution.getUnboxedLowerBound(i);
                } else if (tmp > solution.getUnboxedUpperBound(i)) {
                    tmp = solution.getUnboxedUpperBound(i);
                }
                solution.setUnboxedVariableValue(i, tmp);
            }
        }
    }


    /**
     * Calculates the delta value used in NonUniform mutation operator
     */
    private double delta(double y, double bMutationParameter) {
        double rand = this.randomGenenerator.getRandomValue();
        int it, maxIt;
        it = this.currentIteration;
        maxIt = this.maxIterations;

        return (y * (1.0 -
                Math.pow(rand,
                        Math.pow((1.0 - it / (double) maxIt), bMutationParameter)
                )));
    }
}
