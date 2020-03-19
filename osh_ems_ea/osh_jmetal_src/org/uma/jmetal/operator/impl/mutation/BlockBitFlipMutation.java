package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

/**
 * @author Sebastian Kramer
 */
public class BlockBitFlipMutation implements MutationOperator<BinarySolution> {

    /**
     *
     */
    private static final long serialVersionUID = -7274222024314371526L;
    private final double mutationProbability;
    private final JMetalRandom randomGenerator;
    private final int blockSize;

    /**
     * Constructor
     * Creates a new instance of the Bit Flip mutation operator
     */
    public BlockBitFlipMutation(double mutationProbability, int blockSize) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        }
        if (blockSize < 1) {
            throw new JMetalException("blockSize probability is smaller then 1: " + blockSize);
        }

        this.mutationProbability = mutationProbability;
        this.blockSize = blockSize;
        this.randomGenerator = JMetalRandom.getInstance();
    }


    /**
     * Execute() method
     */
    @Override
    public BinarySolution execute(BinarySolution solution) {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        this.doMutation(this.mutationProbability, this.blockSize, solution);
        return solution;
    }

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation setProbability
     * @param solution    The solution to mutate
     */
    public void doMutation(double probability, int blockSize, BinarySolution solution) {

        for (int i = 0; i < solution.getNumberOfVariables(); i++) {

            int bitCount = solution.getNumberOfBits(i);

            for (int j = 0; j < bitCount; j += blockSize) {
                if (this.randomGenerator.nextDouble() < probability) {
                    int maxAdd = i + blockSize;
                    if (maxAdd > bitCount)
                        maxAdd = bitCount;
                    solution.getVariableValue(i).flip(i, maxAdd);
                }
            }
        }
    }
}
