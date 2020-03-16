package org.uma.jmetal.operator.impl.selection;

import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import osh.utils.string.ParameterConstants;

import java.util.Comparator;
import java.util.Map;

/**
 * Class implementing a factory of selection operators
 *
 * @author Sebastian Kramer
 */
public class SelectionFactory {


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
    @SuppressWarnings({"unchecked"})
    public static <S extends Solution<?>> SelectionOperator<?, ?> getSelectionOperator(SelectionType operator,
                                                                                       Map<String, ?> parameters) throws JMetalException {
        switch(operator) {

            case BEST:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                    return new BestSolutionSelection<>((Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                } else {
                    throw new JMetalException("Parameter comparator for BestSolutionSelection not given");
                }

            case DE:
                return new DifferentialEvolutionSelection();

            case BINARY_TOURNAMENT:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                    return new BinaryTournamentSelection<>((Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                } else {
                    return new BinaryTournamentSelection<S>();
                }

            case TOURNAMENT:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator) && parameters.containsKey(ParameterConstants.EA_SELECTION.tournaments)) {
                    return new TournamentSelection<>((Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator),
                            (Integer) parameters.get(ParameterConstants.EA_SELECTION.tournaments));
                } else if (parameters.containsKey(ParameterConstants.EA_SELECTION.tournaments)) {
                    return new TournamentSelection<S>((Integer) parameters.get(ParameterConstants.EA_SELECTION.tournaments));
                } else {
                    throw new JMetalException("Parameter tournaments for TournamentSelection not given");
                }

            case  NARY_TOURNAMENT:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.toSelect) && parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                    return new NaryTournamentSelection<>((Integer) parameters.get(ParameterConstants.EA_SELECTION.toSelect),
                            (Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                } else {
                    return new NaryTournamentSelection<S>();
                }

            case NARY_RANDOM:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.toSelect)) {
                    return new NaryRandomSelection<S>((Integer) parameters.get(ParameterConstants.EA_SELECTION.toSelect));
                } else {
                    return new NaryRandomSelection<S>();
                }

            case RANDOM:
                return new RandomSelection<S>();

            case RANKING_CROWDING:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.toSelect)) {
                    return new RankingAndCrowdingSelection<S>((Integer) parameters.get(ParameterConstants.EA_SELECTION.toSelect));
                } else {
                    throw new JMetalException("Parameter solutionsToSelect for RankingAndCrowdingSelection not given");
                }

            case ROULETTE_WHEEL:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                    return new RouletteWheelSelection<>((Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                } else {
                    return new RouletteWheelSelection<S>();
                }

            case SUS:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.toSelect)) {
                    if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                        return new StochasticUniversalSampling<>((Integer) parameters.get(ParameterConstants.EA_SELECTION.toSelect),
                                (Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                    } else {
                        return new StochasticUniversalSampling<S>((Integer) parameters.get(ParameterConstants.EA_SELECTION.toSelect));
                    }
                } else {
                    throw new JMetalException("Parameter solutionsToSelect for TournamentSelection not given");
                }

            case STOCHASTIC_ACCEPTANCE:
                if (parameters.containsKey(ParameterConstants.EA_SELECTION.comparator)) {
                    return new StochasticAcceptanceSelection<>((Comparator<S>) parameters.get(ParameterConstants.EA_SELECTION.comparator));
                } else {
                    return new StochasticAcceptanceSelection<S>();
                }

            default: throw new JMetalException("Selection Operator: " + operator.getName() + " not implemented");
        }
    }
}
