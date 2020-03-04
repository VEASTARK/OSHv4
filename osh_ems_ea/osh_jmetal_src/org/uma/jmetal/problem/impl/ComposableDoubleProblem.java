package org.uma.jmetal.problem.impl;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.impl.DefaultDoubleSolution;
import org.uma.jmetal.util.solutionattribute.impl.NumberOfViolatedConstraints;
import org.uma.jmetal.util.solutionattribute.impl.OverallConstraintViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This class allows to define a continuous (double) problem dynamically, by adding the ranges of
 * every decision variable, the objective functions and the constraints.  For example, the Schaffer
 * unconstrained problem (1 decision variable, two objectives) can be defined as follows:
 * <p>
 * ComposableDoubleProblem problem = new ComposableDoubleProblem()
 * .setName("Schaffer")
 * .addVariable(-10, 10)
 * .addVariable(-10, 10)
 * .addFunction((x) -> x[0] * x[0])
 * .addFunction((x) -> (x[0] - 2.0) * (x[0] - 2.0));
 * <p>
 * The Srinivas constrained problem can be defined in this way:
 * <p>
 * ComposableDoubleProblem problem;
 * problem = new ComposableDoubleProblem()
 * .setName("Srinivas")
 * .addVariable(-20.0, 20.0)
 * .addVariable(-20.0, 20.0)
 * .addFunction((x) ->  2.0 + (x[0] - 2.0) * (x[0] - 2.0) + (x[1] - 1.0) * (x[1] - 1.0))
 * .addFunction((x) ->  9.0 * x[0] - (x[1] - 1.0) * (x[1] - 1.0))
 * .addConstraint((x) -> 1.0 - (x[0] * x[0] + x[1] * x[1]) / 225.0)
 * .addConstraint((x) -> (3.0 * x[1] - x[0]) / 10.0 - 1.0) ;
 * <p>
 * Note that this class does not inherits from {@link AbstractDoubleProblem}.
 * <p>
 * As defined, this class would make possible to add more variables, objectives and constraints
 * to an existing problem on the fly.
 * <p>
 * This class does not intend to be a replacement of the existing of {@link AbstractDoubleProblem};
 * it is merely an alternative way of defining a problem.
 */
@SuppressWarnings("serial")
public class ComposableDoubleProblem implements DoubleProblem {

    private final List<Function<Double[], Double>> objectiveFunction;
    private final List<Function<Double[], Double>> constraints;
    private final List<Double> lowerBounds;
    private final List<Double> upperBounds;
    private final OverallConstraintViolation<DoubleSolution> overallConstraintViolationDegree;
    private final NumberOfViolatedConstraints<DoubleSolution> numberOfViolatedConstraints;
    private String name;

    public ComposableDoubleProblem() {
        this.objectiveFunction = new ArrayList<>();
        this.constraints = new ArrayList<>();
        this.lowerBounds = new ArrayList<>();
        this.upperBounds = new ArrayList<>();

        this.overallConstraintViolationDegree = new OverallConstraintViolation<>();
        this.numberOfViolatedConstraints = new NumberOfViolatedConstraints<>();
        this.name = "";
    }

    public ComposableDoubleProblem addFunction(Function<Double[], Double> objective) {
        this.objectiveFunction.add(objective);
        return this;
    }

    public ComposableDoubleProblem addConstraint(Function<Double[], Double> constraint) {
        this.constraints.add(constraint);
        return this;
    }

    public ComposableDoubleProblem addVariable(double lowerBound, double upperBound) {
        this.lowerBounds.add(lowerBound);
        this.upperBounds.add(upperBound);
        return this;
    }

    @Override
    public int getNumberOfVariables() {
        return this.lowerBounds.size();
    }

    @Override
    public int getNumberOfObjectives() {
        return this.objectiveFunction.size();
    }

    @Override
    public int getNumberOfConstraints() {
        return this.constraints.size();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public ComposableDoubleProblem setName(String name) {
        this.name = name;

        return this;
    }

    public Double getLowerBound(int index) {
        return this.lowerBounds.get(index);
    }

    public Double getUpperBound(int index) {
        return this.upperBounds.get(index);
    }

    @Override
    public DoubleSolution createSolution() {
        return new DefaultDoubleSolution(this);
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        Double[] vars = solution.getVariables().toArray(new Double[this.getNumberOfVariables()]);

        IntStream.range(0, this.getNumberOfObjectives())
                .forEach(i -> solution.setObjective(i, this.objectiveFunction.get(i).apply(vars)));

        if (this.getNumberOfConstraints() > 0) {
            double overallConstraintViolation = 0.0;
            int violatedConstraints = 0;
            for (int i = 0; i < this.getNumberOfConstraints(); i++) {
                double violationDegree = this.constraints.get(i).apply(vars);
                if (violationDegree < 0) {
                    overallConstraintViolation += violationDegree;
                    violatedConstraints++;
                }
            }
            this.overallConstraintViolationDegree.setAttribute(solution, overallConstraintViolation);
            this.numberOfViolatedConstraints.setAttribute(solution, violatedConstraints);
        }
    }
}
