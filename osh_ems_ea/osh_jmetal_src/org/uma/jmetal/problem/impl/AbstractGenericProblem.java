package org.uma.jmetal.problem.impl;

import org.uma.jmetal.problem.Problem;

@SuppressWarnings("serial")
public abstract class AbstractGenericProblem<S> implements Problem<S> {
    private int numberOfVariables;
    private int numberOfObjectives;
    private int numberOfConstraints;
    private String name;

    /* Getters */
    @Override
    public int getNumberOfVariables() {
        return this.numberOfVariables;
    }

    /* Setters */
    protected void setNumberOfVariables(int numberOfVariables) {
        this.numberOfVariables = numberOfVariables;
    }

    @Override
    public int getNumberOfObjectives() {
        return this.numberOfObjectives;
    }

    protected void setNumberOfObjectives(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
    }

    @Override
    public int getNumberOfConstraints() {
        return this.numberOfConstraints;
    }

    protected void setNumberOfConstraints(int numberOfConstraints) {
        this.numberOfConstraints = numberOfConstraints;
    }

    @Override
    public String getName() {
        return this.name;
    }

    protected void setName(String name) {
        this.name = name;
    }
}
