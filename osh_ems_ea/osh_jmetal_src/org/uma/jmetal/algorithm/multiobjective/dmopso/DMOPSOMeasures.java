package org.uma.jmetal.algorithm.multiobjective.dmopso;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.DurationMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;

import java.util.List;

@SuppressWarnings("serial")
public class DMOPSOMeasures extends DMOPSO implements Measurable {

    protected CountingMeasure iterations;
    protected DurationMeasure durationMeasure;
    protected SimpleMeasureManager measureManager;
    protected BasicMeasure<List<DoubleSolution>> solutionListMeasure;
    protected BasicMeasure<Double> hypervolumeValue;
    protected BasicMeasure<Double> epsilonValue;
    protected Front referenceFront;

    public DMOPSOMeasures(DoubleProblem problem, int swarmSize, int maxIterations,
                          double r1Min, double r1Max, double r2Min, double r2Max,
                          double c1Min, double c1Max, double c2Min, double c2Max,
                          double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                          FunctionType functionType, String dataDirectory, int maxAge) {
        this(problem, swarmSize, maxIterations,
                r1Min, r1Max, r2Min, r2Max,
                c1Min, c1Max, c2Min, c2Max,
                weightMin, weightMax, changeVelocity1, changeVelocity2,
                functionType, dataDirectory, maxAge, "dMOPSO");
    }

    public DMOPSOMeasures(DoubleProblem problem, int swarmSize, int maxIterations,
                          double r1Min, double r1Max, double r2Min, double r2Max,
                          double c1Min, double c1Max, double c2Min, double c2Max,
                          double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                          FunctionType functionType, String dataDirectory, int maxAge, String name) {
        super(problem, swarmSize, maxIterations,
                r1Min, r1Max, r2Min, r2Max,
                c1Min, c1Max, c2Min, c2Max,
                weightMin, weightMax, changeVelocity1, changeVelocity2,
                functionType, dataDirectory, maxAge, name);
        this.referenceFront = new ArrayFront();
        this.initMeasures();
    }

    @Override
    protected void initProgress() {
        this.iterations.reset();
    }

    @Override
    protected void updateProgress() {
        this.iterations.increment();
        this.hypervolumeValue.push(new PISAHypervolume<DoubleSolution>(this.referenceFront).evaluate(this.getResult()));
        this.epsilonValue.push(new Epsilon<DoubleSolution>(this.referenceFront).evaluate(this.getResult()));
        this.solutionListMeasure.push(this.getResult());
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
        this.hypervolumeValue = new BasicMeasure<>();
        this.epsilonValue = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPullMeasure("currentExecutionTime", this.durationMeasure);
        this.measureManager.setPullMeasure("currentEvaluation", this.iterations);
        this.measureManager.setPullMeasure("hypervolume", this.hypervolumeValue);
        this.measureManager.setPullMeasure("epsilon", this.epsilonValue);

        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentEvaluation", this.iterations);
        this.measureManager.setPushMeasure("hypervolume", this.hypervolumeValue);
        this.measureManager.setPushMeasure("epsilon", this.epsilonValue);
    }

    @Override
    public String getDescription() {
        return "MOPSO with decomposition. Version using measures";
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }

    public void setReferenceFront(Front referenceFront) {
        this.referenceFront = referenceFront;
    }
}
