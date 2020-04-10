package osh.mgmt.globalcontroller.jmetal.solution;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import osh.configuration.oc.RankingType;
import osh.utils.string.ParameterConstants;
import osh.utils.string.StringConversions;

import java.util.Comparator;
import java.util.Map;

/**
 * Class implementing a factory of solution comparators.
 *
 * @author Sebastian Kramer
 */
public class SolutionComparatorFactory {

    /**
     * Constructs the solution comparator based on the given comparator type and the given parameters.
     *
     * @param comparator the comparators type
     * @param parameters the parameters for the comparator
     * @return the constructed solution comparators
     *
     * @throws JMetalException if the specified solution comparators cannot be constructed or parameters are missing
     */
    public static <S extends Solution<?>> Comparator<S> getComparator(RankingType comparator, Map<String, Object> parameters) {

        switch (comparator) {
            case DOMINANCE:
                return new DominanceComparator<>();

            case OBJECTIVE:
                if (parameters.containsKey(ParameterConstants.EA_MULTI_OBJECTIVE.objective)) {
                    if (!parameters.containsKey(ParameterConstants.EA_MULTI_OBJECTIVE.ordering)) {
                        return new ObjectiveComparator<>((int) parameters.get(ParameterConstants.EA_MULTI_OBJECTIVE.objective));
                    } else {
                        return new ObjectiveComparator<>((int) parameters.get(ParameterConstants.EA_MULTI_OBJECTIVE.objective),
                                ObjectiveComparator.Ordering.valueOf((String) parameters.get(ParameterConstants.EA_MULTI_OBJECTIVE.ordering)));
                    }
                } else {
                    throw new JMetalException("Parameter objective for ObjectiveComparator not given");
                }

            case WEIGHTED_OBJECTIVE:
                if (parameters.containsKey(ParameterConstants.EA_MULTI_OBJECTIVE.objectiveWeights)) {
                    return new WeightedObjectiveSumComparator<>(StringConversions.fromStringToPrimitiveDoubleArray(
                            (String) parameters.get(ParameterConstants.EA_MULTI_OBJECTIVE.objectiveWeights)));
                } else {
                    throw new JMetalException("Parameter objectiveWeights for WeightedObjectiveSumComparator not given");
                }

            case CHEBYSHEV:
                if (parameters.containsKey(ParameterConstants.EA_MULTI_OBJECTIVE.objectiveWeights)) {
                    return new ChebyshevComparator<>(StringConversions.fromStringToPrimitiveDoubleArray(
                            (String) parameters.get(ParameterConstants.EA_MULTI_OBJECTIVE.objectiveWeights)));
                } else {
                    throw new JMetalException("Parameter objectiveWeights for ChebyshevComparator not given");
                }

            case NASH_BARGAINING:
                return new NashBargainingComparator<>();

            default: throw new JMetalException("Comparator: " + comparator + " not implemented");
        }
    }
}
