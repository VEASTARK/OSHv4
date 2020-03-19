package org.uma.jmetal.algorithm.multiobjective.wasfga;

import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
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
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

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
                          CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator,
                          SolutionListEvaluator<S> evaluator,
                          double epsilon,
                          List<Double> referencePoint,
                          String weightVectorsFileName,
                          IEALogger eaLogger) {

        super(problem,
                populationSize,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                evaluator,
                epsilon,
                referencePoint,
                weightVectorsFileName,
                eaLogger);
        this.initMeasures();
    }

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public WASFGAMeasures(Problem<S> problem,
                          int populationSize,
                          CrossoverOperator<S> crossoverOperator,
                          MutationOperator<S> mutationOperator,
                          SelectionOperator<List<S>, S> selectionOperator,
                          SolutionListEvaluator<S> evaluator,
                          double epsilon,
                          List<Double> referencePoint,
                          IEALogger eaLogger) {
        this(problem,
                populationSize,
                crossoverOperator,
                mutationOperator,
                selectionOperator,
                evaluator,
                epsilon,
                referencePoint,
                "",
                eaLogger);
    }

    @Override
    protected void initProgress() {
        this.iterations.reset();
        this.getEALogger().logPopulation(this.population, this.iterations.get().intValue());
    }

    @Override
    protected void updateProgress() {
        this.iterations.increment();
        this.getEALogger().logPopulation(this.population, this.iterations.get().intValue());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations.get().intValue(), -1, this.getPopulation())) {
                this.getEALogger().logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
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
