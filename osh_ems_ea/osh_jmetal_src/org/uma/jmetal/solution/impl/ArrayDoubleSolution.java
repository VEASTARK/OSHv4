package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.utils.DeepCopy;

import java.util.*;

/**
 * Implementation of {@link DoubleSolution} using arrays.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class ArrayDoubleSolution implements DoubleSolution {
    protected final DoubleProblem problem;
    protected final JMetalRandom randomGenerator;
    private final double[] objectives;
    private final double[] variables;
    protected Map<Object, Object> attributes;

    /**
     * Constructor
     */
    public ArrayDoubleSolution(DoubleProblem problem) {
        this.problem = problem;
        this.attributes = new HashMap<>();
        this.randomGenerator = JMetalRandom.getInstance();

        this.objectives = new double[problem.getNumberOfObjectives()];
        this.variables = new double[problem.getNumberOfVariables()];
        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            this.variables[i] = this.randomGenerator.nextDouble(this.getUnboxedLowerBound(i), this.getUnboxedUpperBound(i));
        }
    }

    /**
     * Copy constructor
     *
     * @param solution to copy
     */
    @SuppressWarnings("unchecked")
    public ArrayDoubleSolution(ArrayDoubleSolution solution) {
        this.problem = solution.problem;
        this.randomGenerator = JMetalRandom.getInstance();

        this.objectives = new double[this.problem.getNumberOfObjectives()];
        this.variables = new double[this.problem.getNumberOfVariables()];

        System.arraycopy(solution.objectives, 0, this.objectives, 0, this.objectives.length);
        System.arraycopy(solution.variables, 0, this.variables, 0, this.variables.length);

        this.attributes = (HashMap<Object, Object>) DeepCopy.copy(solution.attributes);
    }

    @Override
    public void setObjective(int index, double value) {
        this.objectives[index] = value;
    }

    @Override
    public double getObjective(int index) {
        return this.objectives[index];
    }

    @Override
    public List<Double> getVariables() {
        List<Double> vars = new ArrayList<>(this.getNumberOfVariables());
        for (int i = 0; i < this.getNumberOfVariables(); i++) {
            vars.add(this.variables[i]);
        }
        return vars;
    }

    @Override
    public double[] getObjectives() {
        return this.objectives;
    }

    @Override
    @Deprecated
    public Double getVariableValue(int index) {
        return this.variables[index];
    }

    @Override
    @Deprecated
    public void setVariableValue(int index, Double value) {
        this.variables[index] = value;
    }

    @Override
    public String getVariableValueString(int index) {
        return String.valueOf(this.getUnboxedVariableValue(index));
    }

    @Override
    public int getNumberOfVariables() {
        return this.problem.getNumberOfVariables();
    }

    @Override
    public int getNumberOfObjectives() {
        return this.problem.getNumberOfObjectives();
    }

    @Override
    public Double getUpperBound(int index) {
        return this.problem.getUpperBound(index);
    }

    @Override
    public double getUnboxedVariableValue(int index) {
        return this.variables[index];
    }

    @Override
    public void setUnboxedVariableValue(int index, double value) {
        this.variables[index] = value;
    }

    @Override
    public Double getLowerBound(int index) {
        return this.problem.getLowerBound(index);
    }

    @Override
    public double getUnboxedLowerBound(int index) {
        return this.problem.getUnboxedLowerBound(index);
    }

    @Override
    public double getUnboxedUpperBound(int index) {
        return this.problem.getUnboxedUpperBound(index);
    }

    @Override
    public Solution<Double> copy() {
        return new ArrayDoubleSolution(this);
    }

    @Override
    public void setAttribute(Object id, Object value) {
        this.attributes.put(id, value);
    }

    @Override
    public Object getAttribute(Object id) {
        return this.attributes.get(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ArrayDoubleSolution that = (ArrayDoubleSolution) o;

        if (!Arrays.equals(this.objectives, that.objectives)) return false;
        if (!Arrays.equals(this.variables, that.variables)) return false;
        return Objects.equals(this.problem, that.problem);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.objectives);
        result = 31 * result + Arrays.hashCode(this.variables);
        result = 31 * result + (this.problem != null ? this.problem.hashCode() : 0);
        return result;
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
