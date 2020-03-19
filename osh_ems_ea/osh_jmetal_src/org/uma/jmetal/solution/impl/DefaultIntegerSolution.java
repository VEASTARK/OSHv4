package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.solution.IntegerSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.utils.DeepCopy;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines an implementation of an integer solution
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultIntegerSolution
        extends AbstractGenericSolution<Integer, IntegerProblem>
        implements IntegerSolution {

    /**
     * Constructor
     */
    public DefaultIntegerSolution(IntegerProblem problem) {
        super(problem);

        this.initializeIntegerVariables(JMetalRandom.getInstance());
        this.initializeObjectiveValues();
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unchecked")
    public DefaultIntegerSolution(DefaultIntegerSolution solution) {
        super(solution.problem);

        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        this.attributes = (HashMap<Object, Object>) DeepCopy.copy(solution.attributes);
    }

    @Override
    public Integer getUpperBound(int index) {
        return this.problem.getUpperBound(index);
    }

    @Override
    public Integer getLowerBound(int index) {
        return this.problem.getLowerBound(index);
    }

    @Override
    public DefaultIntegerSolution copy() {
        return new DefaultIntegerSolution(this);
    }

    @Override
    public String getVariableValueString(int index) {
        return this.getVariableValue(index).toString();
    }

    private void initializeIntegerVariables(JMetalRandom randomGenerator) {
        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            Integer value = randomGenerator.nextInt(this.getLowerBound(i), this.getUpperBound(i));
            this.setVariableValue(i, value);
        }
    }


    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
