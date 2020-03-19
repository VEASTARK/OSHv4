package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements an approximation of a normal bitflip mutation operator that will operate much faster as it
 * does not rely on nedding a generated random number per bit.
 * <p>
 * For small bitstrings this approximation will be very imprecise (number of bits < 50) as we use a guasian
 * distribution to approximate a binomial one.
 *
 *
 * @author Sebastian Kramer
 * @version 1.0
 */
public class ApproximateBitFlipMutation implements MutationOperator<BinarySolution> {

    private static final long serialVersionUID = -1962570721423968221L;

    private double mutationProbability;
    private final JMetalRandom randomGenerator;

    /**
     * Constructor
     */
    public ApproximateBitFlipMutation(double mutationProbability) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        }
        this.mutationProbability = mutationProbability;
        this.randomGenerator = JMetalRandom.getInstance();
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

            int bitCount = solution.getNumberOfBits(i);
            BitSet variable = solution.getVariableValue(i);
            BitSet set = new BitSet(bitCount);

            /* number of bits flipped is B(n, p) distributed (n = number of bits, p = mutationProbability),
             * this can be approximated with N(np, sqrt(np * (1-p)))
             */
            int x = (int) Math.round(Math.sqrt(probability * (1 - probability) * bitCount)
                    * this.randomGenerator.nextGaussian() + bitCount * probability);

            //very low probability in some special cases that x is bigger then the bitcount
            x = Math.min(x, bitCount);

            Set<Integer> indices = new HashSet<>();
            while (indices.size() < x) {
                indices.add(this.randomGenerator.nextInt(0, bitCount - 1));
            }
            for (Integer j : indices)
                set.set(j);
            variable.xor(set);
        }
    }
}
