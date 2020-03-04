package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines an implementation of a double solution
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultDoubleSolution
        extends AbstractGenericSolution<Double, DoubleProblem>
        implements DoubleSolution {

    /**
     * Constructor
     */
    public DefaultDoubleSolution(DoubleProblem problem) {
        super(problem);

        this.initializeDoubleVariables(JMetalRandom.getInstance());
        this.initializeObjectiveValues();
    }

    /**
     * Copy constructor
     */
    public DefaultDoubleSolution(DefaultDoubleSolution solution) {
        super(solution.problem);

        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        this.attributes = new HashMap<>(solution.attributes);
    }

    @Override
    public Double getUpperBound(int index) {
        return this.problem.getUpperBound(index);
    }

    @Override
    public Double getLowerBound(int index) {
        return this.problem.getLowerBound(index);
    }

    @Override
    public DefaultDoubleSolution copy() {
        return new DefaultDoubleSolution(this);
    }

    @Override
    public String getVariableValueString(int index) {
        return this.getVariableValue(index).toString();
    }

    private void initializeDoubleVariables(JMetalRandom randomGenerator) {
        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            Double value = randomGenerator.nextDouble(this.getLowerBound(i), this.getUpperBound(i));
            this.setVariableValue(i, value);
        }
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
