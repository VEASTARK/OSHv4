package osh.mgmt.globalcontroller.jmetal.solution;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import osh.configuration.oc.AlgorithmType;

import java.util.*;

/**
 * Represents a collection of algorithms and their returned results.
 *
 * @author Sebastian Kramer
 */
public class AlgorithmSolutionCollection {

    private final List<SolutionTypeUnion<?>> solutionList = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a single solution and their executing algorithm to the results.
     *
     * @param type the algorithm type
     * @param solution the solution
     */
    public void addSolution(AlgorithmType type, Solution<?> solution) {
        this.solutionList.add(new SolutionTypeUnion<>(solution, type));
    }

    /**
     * Adds a collection of solutions and their executing algorithm to the results.
     *
     * @param type the algorithm type
     * @param solutions the collection of solution
     */
    public void addSolutionCollection(AlgorithmType type, Collection<Solution<?>> solutions) {
        solutions.forEach(s -> this.solutionList.add(new SolutionTypeUnion<>(s, type)));
    }

    /**
     * Returns all results.
     *
     * @return all results
     */
    public List<SolutionTypeUnion<?>> getSolutionList() {
        return this.solutionList;
    }

    /**
     * Returns a non-dominating collection of all results.
     *
     * @return a non-dominating collection of all results
     */
    public List<SolutionTypeUnion<?>> getNonDominatedSolutionList() {
        return SolutionListUtils.getNondominatedSolutions(this.solutionList);
    }

    /**
     * Represents a wrapper around a solution containing the additional information about the algorithm the resulted
     * from.
     *
     * @param <S> the type of solution
     */
    public static class SolutionTypeUnion<S> implements Solution<S> {

        private static final long serialVersionUID = -4785823831112460543L;

        private final Solution<S> solution;
        private final AlgorithmType algorithmType;

        /**
         * Concstructs this wrapper around the given solution and algorithm type.
         *
         * @param solution the solution
         * @param algorithmType the algorithm type
         */
        SolutionTypeUnion(Solution<S> solution, AlgorithmType algorithmType) {
            this.solution = solution;
            this.algorithmType = algorithmType;
        }

        @Override
        public void setObjective(int index, double value) {
            this.solution.setObjective(index, value);
        }

        @Override
        public double getObjective(int index) {
            return this.solution.getObjective(index);
        }

        @Override
        public double[] getObjectives() {
            return this.solution.getObjectives();
        }

        @Override
        public S getVariableValue(int index) {
            return this.solution.getVariableValue(index);
        }

        @Override
        public List<S> getVariables() {
            return this.solution.getVariables();
        }

        @Override
        public void setVariableValue(int index, S value) {
            this.solution.setVariableValue(index, value);
        }

        @Override
        public String getVariableValueString(int index) {
            return this.solution.getVariableValueString(index);
        }

        @Override
        public int getNumberOfVariables() {
            return this.solution.getNumberOfVariables();
        }

        @Override
        public int getNumberOfObjectives() {
            return this.solution.getNumberOfObjectives();
        }

        @Override
        public Solution<S> copy() {
            return new SolutionTypeUnion<>(this.solution.copy(), this.algorithmType);
        }

        @Override
        public void setAttribute(Object id, Object value) {
            this.solution.setAttribute(id, value);
        }

        @Override
        public Object getAttribute(Object id) {
            return this.solution.getAttribute(id);
        }

        @Override
        public Map<Object, Object> getAttributes() {
            return this.solution.getAttributes();
        }

        /**
         * Returns the solution this wrapper was built around.
         *
         * @return the solution this wrapper was built around
         */
        public Solution<S> getSolution() {
            return this.solution;
        }

        /**
         * Returns the algorithm type this wrapper contains.
         *
         * @return the algorithm type this wrapper contains
         */
        public AlgorithmType getAlgorithmType() {
            return this.algorithmType;
        }
    }
}
