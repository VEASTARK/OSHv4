package org.uma.jmetal.operator.impl.mutation;

import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import osh.utils.string.ParameterConstants;
import osh.utils.string.ParameterConstants.EA;

import java.util.Map;


/**
 * Class implementing a factory of mutation operators
 *
 * @author Sebastian Kramer
 */
public class MutationFactory {

    /**
     * Constructs the mutation operator based on the given operator type and the given parameters.
     *
     * @param operator the operator type
     * @param parameters the parameters for the operator
     * @param <S> the type of the solution the operator needs to handle
     * @return the constructed operator
     *
     * @throws JMetalException if the specified operator cannot be constructed or parameters are missing
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <S extends Solution<?>> MutationOperator<S> getMutationOperator(MutationType operator,
                                                                                  Map<String, ?> parameters) throws JMetalException {
        switch(operator) {

            case BIT_FLIP_AUTO:
                if (parameters.containsKey(ParameterConstants.EA_MUTATION.autoProbMutationFactor)) {
                    return (MutationOperator<S>) new BitFlipProbabilityAdjustingMutation((Double) parameters.get(ParameterConstants.EA_MUTATION.autoProbMutationFactor));
                } else {
                    throw new JMetalException("Parameter autoProbabilityFactor for BitFlipAutoProbMutation not given");
                }

            case BIT_FLIP:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (MutationOperator<S>) new BitFlipMutation((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for BitFlipMutation not given");
                }

            case BIT_FLIP_APPROX:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (MutationOperator<S>) new ApproximateBitFlipMutation((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for ApproximateBitFlipMutation not given");
                }

            case BIT_FLIP_BLOCK:
                if (parameters.containsKey(EA.probability) && parameters.containsKey(ParameterConstants.EA.blockSize)) {
                    return (MutationOperator<S>) new BlockBitFlipMutation(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Integer) parameters.get(ParameterConstants.EA.blockSize));
                } else {
                    throw new JMetalException("Parameter probability or blockSize for BlockBitFlipMutation not given");
                }

            case CDG:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.ALPHABET.delta)) {
                    return (MutationOperator<S>) new CDGMutation(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.ALPHABET.delta));
                } else {
                    throw new JMetalException("Parameter probability or delta for CDGMutation not given");
                }

            case INTEGER_POLYNOMIAL:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (MutationOperator<S>) new IntegerPolynomialMutation((Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for IntegerPolynomialMutation not given");
                }

            case POLYNOMIAL:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (MutationOperator<S>) new PolynomialMutation((Double) parameters.get(ParameterConstants.EA.probability),
                            (Integer) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for PolynomialMutation not given");
                }

            case POLYNOMIAL_AUTO:
                if (parameters.containsKey(ParameterConstants.EA_MUTATION.autoProbMutationFactor) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (MutationOperator<S>) new PolynomialProbabilityAdjustingMutation((Double) parameters.get(ParameterConstants.EA_MUTATION.autoProbMutationFactor),
                            (Integer) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for PolynomialMutation not given");
                }

            case POLYNOMIAL_APPROX:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (MutationOperator<S>) new ApproximatePolynomialMutation((Double) parameters.get(ParameterConstants.EA.probability),
                            (Integer) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for PolynomialMutation not given");
                }

            case NON_UNIFORM:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA_MUTATION.perturbation)
                        && parameters.containsKey(ParameterConstants.EA_MUTATION.maxIterations)) {
                    return (MutationOperator<S>) new NonUniformMutation((Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA_MUTATION.perturbation),
                            (Integer) parameters.get(ParameterConstants.EA_MUTATION.maxIterations));
                } else {
                    throw new JMetalException("Parameter probability or perturbation or maxIterations for NonUniformMutation not given");
                }

            case NULL:
                return new NullMutation();

            case PERMUTATION_SWAP:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return new PermutationSwapMutation((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for PermutationSwapMutation not given");
                }

            case SIMPLE_RANDOM:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (MutationOperator<S>) new SimpleRandomMutation((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for SimpleRandomMutation not given");
                }

            case UNIFORM:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA_MUTATION.perturbation)) {
                    return (MutationOperator<S>) new UniformMutation((Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA_MUTATION.perturbation));
                } else {
                    throw new JMetalException("Parameter probability or perturbation for UniformMutation not given");
                }

            default: throw new JMetalException("Mutation Operator: " + operator.getName() + " not implemented");
        }
    }
}
