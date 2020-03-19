package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;

/**
 * This class implements a polynomial mutation operator
 * <p>
 * The implementation is based on the NSGA-II code available in
 * http://www.iitk.ac.in/kangal/codes.shtml
 * <p>
 * If the lower and upper bounds of a variable are the same, no mutation is carried out and the
 * bound value is returned.
 *
 * This class implements a polynomial mutation operator that like {@link BitFlipProbabilityAdjustingMutation} adjusts it's
 * mutation probability depending on the number of bits in the solution.
 *
 * @author Sebastian Kramer
 */
@SuppressWarnings("serial")
public class PolynomialProbabilityAdjustingMutation extends ApproximatePolynomialMutation {
    private final double autoProbabilityFactor;

    /**
     * Constructor
     */
    public PolynomialProbabilityAdjustingMutation(double autoProbabilityFactor, double distributionIndex) {
        this(autoProbabilityFactor, distributionIndex, new RepairDoubleSolutionAtBounds());
    }

    /**
     * Constructor
     */
    public PolynomialProbabilityAdjustingMutation(double autoProbabilityFactor, double distributionIndex,
                                                  RepairDoubleSolution solutionRepair) {
        super(0.0, distributionIndex, solutionRepair);
        if (autoProbabilityFactor < 0) {
            throw new JMetalException("autoProbabilityFactor is negative: " + autoProbabilityFactor);
        }
        this.autoProbabilityFactor = autoProbabilityFactor;
    }

    /**
     * Execute() method
     */
    @Override
    public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        double probability = this.autoProbabilityFactor / solution.getNumberOfVariables();

        this.doMutation(probability, solution);
        return solution;
    }
}
