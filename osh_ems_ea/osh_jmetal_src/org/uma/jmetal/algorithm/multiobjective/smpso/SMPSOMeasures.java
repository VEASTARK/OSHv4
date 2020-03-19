package org.uma.jmetal.algorithm.multiobjective.smpso;

import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.BoundedArchive;
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
 * This class implements a version of SMPSO using measures
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMPSOMeasures extends SMPSO implements Measurable {
    protected CountingMeasure iterations;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;

    protected BasicMeasure<List<DoubleSolution>> solutionListMeasure;

    /**
     * Constructor
     *
     * @param problem
     * @param swarmSize
     * @param leaders
     * @param mutationOperator
     * @param maxIterations
     * @param r1Min
     * @param r1Max
     * @param r2Min
     * @param r2Max
     * @param c1Min
     * @param c1Max
     * @param c2Min
     * @param c2Max
     * @param weightMin
     * @param weightMax
     * @param changeVelocity1
     * @param changeVelocity2
     * @param evaluator
     */
    public SMPSOMeasures(DoubleProblem problem, int swarmSize, BoundedArchive<DoubleSolution> leaders,
                         MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min, double r1Max,
                         double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max, double weightMin,
                         double weightMax, double changeVelocity1, double changeVelocity2,
                         SolutionListEvaluator<DoubleSolution> evaluator, IEALogger eaLogger) {
        super(problem, swarmSize, leaders, mutationOperator, maxIterations, r1Min, r1Max, r2Min, r2Max, c1Min, c1Max,
                c2Min, c2Max, weightMin, weightMax, changeVelocity1, changeVelocity2, evaluator, eaLogger);

        this.initMeasures();
    }

    @Override
    public void run() {
        this.durationMeasure.reset();
        this.durationMeasure.start();
        super.run();
        this.durationMeasure.stop();
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations.get().intValue(), -1, this.getResult())) {
                this.getEALogger().logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void initProgress() {
        this.iterations.reset(1);
        this.updateLeadersDensityEstimator();
        this.logPopulation(this.iterations.get().intValue());
    }

    @Override
    protected void updateProgress() {
        this.iterations.increment(1);
        this.updateLeadersDensityEstimator();

        this.solutionListMeasure.push(super.getResult());
        this.logPopulation(this.iterations.get().intValue());
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }

    /* Measures code */
    private void initMeasures() {
        this.durationMeasure = new DurationMeasure();
        this.iterations = new CountingMeasure(0);
        this.solutionListMeasure = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPullMeasure("currentExecutionTime", this.durationMeasure);
        this.measureManager.setPullMeasure("currentIteration", this.iterations);

        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentIteration", this.iterations);
    }

    @Override
    public String getName() {
        return "SMPSOMeasures";
    }

    @Override
    public String getDescription() {
        return "SMPSO. Version using measures";
    }
}
