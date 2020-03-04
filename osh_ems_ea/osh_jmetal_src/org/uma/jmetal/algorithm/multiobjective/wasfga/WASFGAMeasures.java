package org.uma.jmetal.algorithm.multiobjective.wasfga;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.DurationMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;

import java.util.List;

/**
 * Implementation of the preference based algorithm named WASF-GA on jMetal5.0
 *
 * @author Jorge Rodriguez
 */
@SuppressWarnings("serial")
public class WASFGAMeasures<S extends Solution<?>> extends WASFGA<S> implements Measurable {

    protected CountingMeasure iterations;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;
    protected BasicMeasure<List<S>> solutionListMeasure;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public WASFGAMeasures(Problem<S> problem,
                          int populationSize,
                          int maxIterations,
                          CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator,
                          SolutionListEvaluator<S> evaluator,
                          double epsilon,
                          List<Double> referencePoint,
                          String weightVectorsFileName) {

        super(problem,
                populationSize,
                maxIterations,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                evaluator,
                epsilon,
                referencePoint,
                weightVectorsFileName);
        this.initMeasures();
    }

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public WASFGAMeasures(Problem<S> problem,
                          int populationSize,
                          int maxIterations,
                          CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator,
                          SolutionListEvaluator<S> evaluator,
                          double epsilon,
                          List<Double> referencePoint) {
        this(problem,
                populationSize,
                maxIterations,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                evaluator,
                epsilon,
                referencePoint,
                "");
    }

    @Override
    protected void initProgress() {
        this.iterations.reset();
    }

    @Override
    protected void updateProgress() {
        this.iterations.increment();
        this.solutionListMeasure.push(this.getResult());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.iterations.get() >= this.maxIterations;
    }

    @Override
    public void run() {
        this.durationMeasure.reset();
        this.durationMeasure.start();
        super.run();
        this.durationMeasure.stop();
    }

    /* Measures code */
    private void initMeasures() {
        this.durationMeasure = new DurationMeasure();
        this.iterations = new CountingMeasure(0);
        this.solutionListMeasure = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPullMeasure("currentExecutionTime", this.durationMeasure);
        this.measureManager.setPullMeasure("currentEvaluation", this.iterations);

        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentEvaluation", this.iterations);
    }

    @Override
    public String getName() {
        return "WASFGA";
    }

    @Override
    public String getDescription() {
        return "Weighting Achievement Scalarizing Function Genetic Algorithm. Version using Measures";
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }
}