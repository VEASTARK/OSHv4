package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.solution.util.RepairDoubleSolutionAtBounds;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashSet;
import java.util.Set;

/**
 * This class implements a polynomial mutation operator that approximates the mutation points like
 * {@link ApproximateBitFlipMutation}.
 * <p>
 * The implementation is based on the NSGA-II code available in
 * http://www.iitk.ac.in/kangal/codes.shtml
 * <p>
 * If the lower and upper bounds of a variable are the same, no mutation is carried out and the
 * bound value is returned.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Juan J. Durillo
 */
@SuppressWarnings("serial")
public class ApproximatePolynomialMutation implements MutationOperator<DoubleSolution> {
    private static final double DEFAULT_PROBABILITY = 0.01;
    private static final double DEFAULT_DISTRIBUTION_INDEX = 20.0;
    private final RepairDoubleSolution solutionRepair;
    private double distributionIndex;
    private double mutationProbability;
    private final JMetalRandom randomGenerator;

    /**
     * Constructor
     */
    public ApproximatePolynomialMutation() {
        this(DEFAULT_PROBABILITY, DEFAULT_DISTRIBUTION_INDEX);
    }

    /**
     * Constructor
     */
    public ApproximatePolynomialMutation(DoubleProblem problem, double distributionIndex) {
        this(1.0 / problem.getNumberOfVariables(), distributionIndex);
    }

    /**
     * Constructor
     */
    public ApproximatePolynomialMutation(double mutationProbability,
                                         double distributionIndex) {
        this(mutationProbability, distributionIndex, new RepairDoubleSolutionAtBounds());
    }

    /**
     * Constructor
     */
    public ApproximatePolynomialMutation(double mutationProbability, double distributionIndex,
                                         RepairDoubleSolution solutionRepair) {
        if (mutationProbability < 0) {
            throw new JMetalException("Mutation probability is negative: " + mutationProbability);
        } else if (distributionIndex < 0) {
            throw new JMetalException("Distribution index is negative: " + distributionIndex);
        }
        this.mutationProbability = mutationProbability;
        this.distributionIndex = distributionIndex;
        this.solutionRepair = solutionRepair;

        this.randomGenerator = JMetalRandom.getInstance();
    }

    /* Getters */
    public double getMutationProbability() {
        return this.mutationProbability;
    }

    /* Setters */
    public void setMutationProbability(double probability) {
        this.mutationProbability = probability;
    }

    public double getDistributionIndex() {
        return this.distributionIndex;
    }

    public void setDistributionIndex(double distributionIndex) {
        this.distributionIndex = distributionIndex;
    }

    /**
     * Execute() method
     */
    @Override
    public DoubleSolution execute(DoubleSolution solution) throws JMetalException {
        if (null == solution) {
            throw new JMetalException("Null parameter");
        }

        this.doMutation(this.mutationProbability, solution);
        return solution;
    }

    /**
     * Perform the mutation operation
     */
    void doMutation(double probability, DoubleSolution solution) {
        double rnd, delta1, delta2, mutPow, deltaq;
        double y, yl, yu, val, xy;

        int noVariables = solution.getNumberOfVariables();
        double d_noVariables = solution.getNumberOfVariables();

        /* number of bits flipped is B(n, p) distributed (n = number of bits, p = mutationProbability),
         * this can be approximated with N(np, sqrt(np * (1-p)))
         */
        int x = (int) Math.round(Math.sqrt(probability * (1 - probability) * d_noVariables)
                * this.randomGenerator.nextGaussian() + d_noVariables * probability);

        //very low probability in some special cases that x is bigger then the bitcount
        x = Math.min(x, noVariables);

        Set<Integer> indices = new HashSet<>();
        while (indices.size() < x) {
            indices.add(this.randomGenerator.nextInt(0, noVariables - 1));
        }

        for (Integer i : indices) {
            y = solution.getUnboxedVariableValue(i);
            yl = solution.getUnboxedLowerBound(i);
            yu = solution.getUnboxedUpperBound(i);
            if (yl == yu) {
                y = yl;
            } else {
                delta1 = (y - yl) / (yu - yl);
                delta2 = (yu - y) / (yu - yl);
                rnd = this.randomGenerator.nextDouble();
                mutPow = 1.0 / (this.distributionIndex + 1.0);
                if (rnd <= 0.5) {
                    xy = 1.0 - delta1;
                    val = 2.0 * rnd + (1.0 - 2.0 * rnd) * (Math.pow(xy, this.distributionIndex + 1.0));
                    deltaq = Math.pow(val, mutPow) - 1.0;
                } else {
                    xy = 1.0 - delta2;
                    val = 2.0 * (1.0 - rnd) + 2.0 * (rnd - 0.5) * (Math.pow(xy, this.distributionIndex + 1.0));
                    deltaq = 1.0 - Math.pow(val, mutPow);
                }
                y += deltaq * (yu - yl);
                y = this.solutionRepair.repairSolutionVariableValue(y, yl, yu);
            }
            solution.setUnboxedVariableValue(i, y);
        }
    }
}
