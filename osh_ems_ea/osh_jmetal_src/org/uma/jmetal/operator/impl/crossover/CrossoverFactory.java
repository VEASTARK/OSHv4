package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.util.RepairDoubleSolution;
import org.uma.jmetal.util.JMetalException;
import osh.utils.string.ParameterConstants;

import java.util.Map;


/**
 * Class implementing a factory of crossover operators.
 *
 * @author Sebastian Kramer
 */
public class CrossoverFactory {


    /**
     * Constructs the crossover operator based on the given operator type and the given parameters.
     *
     * @param operator the operator type
     * @param parameters the parameters for the operator
     * @param <S> the type of the solution the operator needs to handle
     * @return the constructed operator
     *
     * @throws JMetalException if the specified operator cannot be constructed or parameters are missing
     */
    @SuppressWarnings("unchecked")
    public static <S extends Solution<?>> CrossoverOperator<S> getCrossoverOperator(CrossoverType operator,
                                                                                    Map<String, ?> parameters) throws JMetalException {

        switch(operator) {
            case BINARY_N_POINT:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.points)) {
                    return (CrossoverOperator<S>) new BinaryNPointCrossover(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Integer) parameters.get(ParameterConstants.EA_RECOMBINATION.points));
                } else {
                    throw new JMetalException("Parameter probability or points for BinaryNPointCrossover not given");
                }

            case BLX_ALPHA:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.alpha)
                        && parameters.containsKey(ParameterConstants.EA.solutionRepair)) {
                    return (CrossoverOperator<S>) new BLXAlphaCrossover(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.alpha),
                            (RepairDoubleSolution) parameters.get(ParameterConstants.EA.solutionRepair));
                } else if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.alpha)) {
                    return (CrossoverOperator<S>) new BLXAlphaCrossover(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.alpha));
                } else if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new BLXAlphaCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for BLXAlphaCrossover not given");
                }

            case DE:
                if (parameters.containsKey(ParameterConstants.EA_RECOMBINATION.cr)
                        && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.f)
                        && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.k)
                        && parameters.containsKey(ParameterConstants.EA_RECOMBINATION.variant)) {
                    if (parameters.containsKey(ParameterConstants.EA_RECOMBINATION.k)) {
                        return (CrossoverOperator<S>) new DifferentialEvolutionCrossover(
                                (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.cr),
                                (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.f),
                                (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.k),
                                (String) parameters.get(ParameterConstants.EA_RECOMBINATION.variant));
                    } else {
                        return (CrossoverOperator<S>) new DifferentialEvolutionCrossover(
                                (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.cr),
                                (Double) parameters.get(ParameterConstants.EA_RECOMBINATION.f),
                                (String) parameters.get(ParameterConstants.EA_RECOMBINATION.variant));
                    }
                } else {
                    return (CrossoverOperator<S>) new DifferentialEvolutionCrossover();
                }

            case HUX:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new HUXCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for HUXCrossover not given");
                }

            case INTEGER_SBX:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (CrossoverOperator<S>) new IntegerSBXCrossover(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Double) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for IntegerSBXCrossover not given");
                }

            case SBX:
                if (parameters.containsKey(ParameterConstants.EA.probability) && parameters.containsKey(ParameterConstants.EA.distributionIndex)) {
                    return (CrossoverOperator<S>) new SBXCrossover(
                            (Double) parameters.get(ParameterConstants.EA.probability),
                            (Integer) parameters.get(ParameterConstants.EA.distributionIndex));
                } else {
                    throw new JMetalException("Parameter probability or distributionIndex for SBXCrossover not given");
                }

            case NULL:
                return new NullCrossover<>();

            case PMX:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new PMXCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for PMXCrossover not given");
                }

            case BINARY_SHUFFLE:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new ShuffledBinaryCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for ShuffledBinaryCrossover not given");
                }

            case ONE_POINT:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new SinglePointCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for SinglePointCrossover not given");
                }

            case TWO_POINT:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new TwoPointCrossover<>((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for TwoPointCrossover not given");
                }

            case BINARY_UNIFORM:
                if (parameters.containsKey(ParameterConstants.EA.probability)) {
                    return (CrossoverOperator<S>) new UniformBinaryCrossover((Double) parameters.get(ParameterConstants.EA.probability));
                } else {
                    throw new JMetalException("Parameter probability for UniformBinaryCrossover not given");
                }
            default: throw new JMetalException("Crossover Operator: " + operator.getName() + " not implemented");
        }

    }
}
