package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.*;

/**
 * Abstract class representing a generic solution
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractGenericSolution<T, P extends Problem<?>> implements Solution<T> {
    protected final P problem;
    /**
     * @deprecated Call {@link JMetalRandom#getInstance()} if you need one.
     */
    @Deprecated
    protected final JMetalRandom randomGenerator;
    private final double[] objectives;
    private final List<T> variables;
    protected Map<Object, Object> attributes;

    /**
     * Constructor
     */
    protected AbstractGenericSolution(P problem) {
        this.problem = problem;
        this.attributes = new HashMap<>();
        this.randomGenerator = JMetalRandom.getInstance();

        this.objectives = new double[problem.getNumberOfObjectives()];
        this.variables = new ArrayList<>(problem.getNumberOfVariables());
        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            this.variables.add(i, null);
        }
    }

    @Override
    public double[] getObjectives() {
        return this.objectives;
    }

    @Override
    public List<T> getVariables() {
        return this.variables;
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
    public void setObjective(int index, double value) {
        this.objectives[index] = value;
    }

    @Override
    public double getObjective(int index) {
        return this.objectives[index];
    }

    @Override
    public T getVariableValue(int index) {
        return this.variables.get(index);
    }

    @Override
    public void setVariableValue(int index, T value) {
        this.variables.set(index, value);
    }

    @Override
    public int getNumberOfVariables() {
        return this.variables.size();
    }

    @Override
    public int getNumberOfObjectives() {
        return this.objectives.length;
    }

    protected void initializeObjectiveValues() {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.objectives[i] = 0.0;
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("Variables: ");
        for (T var : this.variables) {
            result.append(var).append(" ");
        }
        result.append("Objectives: ");
        for (Double obj : this.objectives) {
            result.append(obj).append(" ");
        }
        result.append("\t");
        result.append("AlgorithmAttributes: ").append(this.attributes).append("\n");

        return result.toString();
    }

    private boolean equalsIgnoringAttributes(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        AbstractGenericSolution<?, ?> that = (AbstractGenericSolution<?, ?>) o;

        if (!Arrays.equals(this.objectives, that.objectives))
            return false;

        return this.variables.equals(that.variables);
    }

    @Override
    public boolean equals(Object o) {

        if (!this.equalsIgnoringAttributes(o)) {
            return false;
        }

        AbstractGenericSolution<?, ?> that = (AbstractGenericSolution<?, ?>) o;
        // avoid recursive infinite comparisons when solution as attribute

        // examples when problems would arise with a simple comparison attributes.equals(that.attributes):
        // if A contains itself as Attribute
        // If A contains B as attribute, B contains A as attribute
        //
        // the following implementation takes care of this by considering solutions as attributes as a special case

        if (this.attributes.size() != that.attributes.size()) {
            return false;
        }

        for (Map.Entry<Object, Object> entry : this.attributes.entrySet()) {
            Object value = entry.getValue();
            Object valueThat = that.attributes.get(entry.getKey());

            if (value != valueThat) { // it only makes sense comparing when having different references

                if (value == null) {
                    return false;
                } else if (valueThat == null) {
                    return false;
                } else { // both not null

                    boolean areAttributeValuesEqual;
                    if (value instanceof AbstractGenericSolution) {
                        areAttributeValuesEqual = ((AbstractGenericSolution<?, ?>) value).equalsIgnoringAttributes(valueThat);
                    } else {
                        areAttributeValuesEqual = !value.equals(valueThat);
                    }
                    if (!areAttributeValuesEqual) {
                        return false;
                    } // if equal the next attributeValue will be checked
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.objectives);
        result = 31 * result + this.variables.hashCode();
        result = 31 * result + this.attributes.hashCode();
        return result;
    }
}
