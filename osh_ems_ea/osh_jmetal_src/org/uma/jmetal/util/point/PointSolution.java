package org.uma.jmetal.util.point;

import org.uma.jmetal.solution.Solution;

import java.util.*;

/**
 * Solution used to wrap a {@link Point} object. Only objectives are used.
 *
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class PointSolution implements Solution<Double> {
    private final int numberOfObjectives;
    private final double[] objectives;
    protected Map<Object, Object> attributes;

    /**
     * Constructor
     *
     * @param numberOfObjectives
     */
    public PointSolution(int numberOfObjectives) {
        this.numberOfObjectives = numberOfObjectives;
        this.objectives = new double[numberOfObjectives];
        this.attributes = new HashMap<>();
    }

    /**
     * Constructor
     *
     * @param point
     */
    public PointSolution(Point point) {
        this.numberOfObjectives = point.getDimension();
        this.objectives = new double[this.numberOfObjectives];

        for (int i = 0; i < this.numberOfObjectives; i++) {
            this.objectives[i] = point.getValue(i);
        }
    }

    /**
     * Constructor
     *
     * @param solution
     */
    public PointSolution(Solution<?> solution) {
        this.numberOfObjectives = solution.getNumberOfObjectives();
        this.objectives = new double[this.numberOfObjectives];

        for (int i = 0; i < this.numberOfObjectives; i++) {
            this.objectives[i] = solution.getObjective(i);
        }
    }

    /**
     * Copy constructor
     *
     * @param point
     */
    public PointSolution(PointSolution point) {
        this(point.numberOfObjectives);

        for (int i = 0; i < this.numberOfObjectives; i++) {
            this.objectives[i] = point.getObjective(i);
        }
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
    public double[] getObjectives() {
        return this.objectives;
    }

    @Override
    public List<Double> getVariables() {
        return Collections.emptyList();
    }

    @Override
    public Double getVariableValue(int index) {
        return null;
    }

    @Override
    public void setVariableValue(int index, Double value) {
        //This method is an intentionally-blank override.
    }

    @Override
    public String getVariableValueString(int index) {
        return null;
    }

    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return this.numberOfObjectives;
    }

    @Override
    public PointSolution copy() {
        return new PointSolution(this);
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
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        PointSolution that = (PointSolution) o;

        if (this.numberOfObjectives != that.numberOfObjectives)
            return false;
        return Arrays.equals(this.objectives, that.objectives);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.objectives);
    }

    @Override
    public String toString() {
        return Arrays.toString(this.objectives);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
