package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;

/**
 * This class implements a bitflip mutation operator that adjusts it's mutation probability depending on the number
 * of bits in the solution.
 * <p>
 * This will result in the same numbers of bits flipped on average no matter how many bits the solution contains.
 *
 * @author Sebastian Kramer
 */
public class BitFlipProbabilityAdjustingMutation extends ApproximateBitFlipMutation {

    private static final long serialVersionUID = 2185962434106149347L;
    private final double autoProbabilityFactor;

    /**
     * Constructor
     */
    public BitFlipProbabilityAdjustingMutation(double autoProbabilityFactor) {
        super(0.0);
        if (autoProbabilityFactor < 0) {
            throw new JMetalException("AutoProbabilityFactor probability is negative: " + autoProbabilityFactor);
        }
        this.autoProbabilityFactor = autoProbabilityFactor;
    }

    /* Getter */
    public double getAutoProbabilityFactor() {
        return this.autoProbabilityFactor;
    }

    /**
     * Execute() method
     */
    @Override
    public BinarySolution execute(BinarySolution solution) {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        double probability = this.autoProbabilityFactor / solution.getTotalNumberOfBits();
        this.doMutation(probability, solution);

        return solution;
    }
}
