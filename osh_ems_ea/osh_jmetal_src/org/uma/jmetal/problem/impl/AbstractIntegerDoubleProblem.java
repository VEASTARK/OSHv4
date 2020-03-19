package org.uma.jmetal.problem.impl;

import org.uma.jmetal.problem.IntegerDoubleProblem;

import java.util.List;

@SuppressWarnings("serial")
public abstract class AbstractIntegerDoubleProblem<S>
        extends AbstractGenericProblem<S>
        implements IntegerDoubleProblem<S> {

    private int numberOfIntegerVariables;
    private int numberOfDoubleVariables;

    private List<Number> lowerLimit;
    private List<Number> upperLimit;

    /* Getters */
    public int getNumberOfDoubleVariables() {
        return this.numberOfDoubleVariables;
    }

    /* Setters */
    protected void setNumberOfDoubleVariables(int numberOfDoubleVariables) {
        this.numberOfDoubleVariables = numberOfDoubleVariables;
    }

    public int getNumberOfIntegerVariables() {
        return this.numberOfIntegerVariables;
    }

    protected void setNumberOfIntegerVariables(int numberOfIntegerVariables) {
        this.numberOfIntegerVariables = numberOfIntegerVariables;
    }

    @Override
    public Number getUpperBound(int index) {
        return this.upperLimit.get(index);
    }

    @Override
    public Number getLowerBound(int index) {
        return this.lowerLimit.get(index);
    }

    protected void setLowerLimit(List<Number> lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    protected void setUpperLimit(List<Number> upperLimit) {
        this.upperLimit = upperLimit;
    }
}
