package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.IntegerDoubleProblem;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.utils.DeepCopy;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines an implementation of a class for solutions having integers and doubles
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultIntegerDoubleSolution
        extends AbstractGenericSolution<Number, IntegerDoubleProblem<?>>
        implements IntegerDoubleSolution {

    private final int numberOfIntegerVariables;
    private final int numberOfDoubleVariables;

    /**
     * Constructor
     */
    public DefaultIntegerDoubleSolution(IntegerDoubleProblem<?> problem) {
        super(problem);

        this.numberOfIntegerVariables = problem.getNumberOfIntegerVariables();
        this.numberOfDoubleVariables = problem.getNumberOfDoubleVariables();

        this.initializeIntegerDoubleVariables(JMetalRandom.getInstance());
        this.initializeObjectiveValues();
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unchecked")
    public DefaultIntegerDoubleSolution(DefaultIntegerDoubleSolution solution) {
        super(solution.problem);

        this.numberOfIntegerVariables = solution.problem.getNumberOfIntegerVariables();
        this.numberOfDoubleVariables = solution.problem.getNumberOfDoubleVariables();

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        for (int i = 0; i < this.numberOfIntegerVariables; i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }

        for (int i = this.numberOfIntegerVariables; i < (this.numberOfIntegerVariables + this.numberOfDoubleVariables); i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }

        this.attributes = (HashMap<Object, Object>) DeepCopy.copy(solution.attributes);
    }

    @Override
    public Number getUpperBound(int index) {
        return this.problem.getUpperBound(index);
    }

    @Override
    public int getNumberOfIntegerVariables() {
        return this.numberOfIntegerVariables;
    }

    @Override
    public int getNumberOfDoubleVariables() {
        return this.numberOfDoubleVariables;
    }

    @Override
    public Number getLowerBound(int index) {
        return this.problem.getLowerBound(index);
    }

    @Override
    public DefaultIntegerDoubleSolution copy() {
        return new DefaultIntegerDoubleSolution(this);
    }

    @Override
    public String getVariableValueString(int index) {
        return this.getVariableValue(index).toString();
    }

    private void initializeIntegerDoubleVariables(JMetalRandom randomGenerator) {
        for (int i = 0; i < this.numberOfIntegerVariables; i++) {
            Integer value = randomGenerator.nextInt((Integer) this.getLowerBound(i), (Integer) this.getUpperBound(i));
            this.setVariableValue(i, value);
        }

        for (int i = this.numberOfIntegerVariables; i < this.getNumberOfVariables(); i++) {
            Double value = randomGenerator.nextDouble((Double) this.getLowerBound(i), (Double) this.getUpperBound(i));
            this.setVariableValue(i, value);
        }
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
