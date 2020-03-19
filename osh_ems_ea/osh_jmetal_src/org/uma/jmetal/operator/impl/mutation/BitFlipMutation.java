package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @version 1.0
 * <p>
 * This class implements a bit flip mutation operator.
 */
@SuppressWarnings("serial")
public class BitFlipMutation implements MutationOperator<BinarySolution> {
    private final RandomGenerator<Double> randomGenerator;
    private double mutationProbability;

    /**
     * Constructor
     */
    public BitFlipMutation(double mutationProbability) {
        this(mutationProbability, () -> JMetalRandom.getInstance().nextDouble());
    }

    /**
     * Constructor
     */
    public BitFlipMutation(double mutationProbability, RandomGenerator<Double> randomGenerator) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        }
        this.mutationProbability = mutationProbability;
        this.randomGenerator = randomGenerator;
    }

    /* Getter */
    public double getMutationProbability() {
        return this.mutationProbability;
    }

    /* Setters */
    public void setMutationProbability(double mutationProbability) {
        this.mutationProbability = mutationProbability;
    }

    /**
     * Execute() method
     */
    @Override
    public BinarySolution execute(BinarySolution solution) {
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
    public void doMutation(double probability, BinarySolution solution) {
        for (int i = 0; i < solution.getNumberOfVariables(); i++) {
            for (int j = 0; j < solution.getVariableValue(i).getBinarySetLength(); j++) {
                if (this.randomGenerator.getRandomValue() <= probability) {
                    solution.getVariableValue(i).flip(j);
                }
            }
        }
    }
}
