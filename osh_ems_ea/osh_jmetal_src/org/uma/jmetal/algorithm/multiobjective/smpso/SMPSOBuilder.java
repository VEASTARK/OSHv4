package org.uma.jmetal.algorithm.multiobjective.smpso;

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class SMPSOBuilder implements AlgorithmBuilder<SMPSO> {
    protected final BoundedArchive<DoubleSolution> leaders;
    private final DoubleProblem problem;
    protected int archiveSize;
    protected MutationOperator<DoubleSolution> mutationOperator;
    protected SolutionListEvaluator<DoubleSolution> evaluator;
    protected SMPSOVariant variant;
    private double c1Max;
    private double c1Min;
    private double c2Max;
    private double c2Min;
    private double r1Max;
    private double r1Min;
    private double r2Max;
    private double r2Min;
    private double weightMax;
    private double weightMin;
    private double changeVelocity1;
    private double changeVelocity2;
    private int swarmSize;
    private int maxIterations;

    public SMPSOBuilder(DoubleProblem problem, BoundedArchive<DoubleSolution> leaders) {
        this.problem = problem;
        this.leaders = leaders;

        this.swarmSize = 100;
        this.maxIterations = 250;

        this.r1Max = 1.0;
        this.r1Min = 0.0;
        this.r2Max = 1.0;
        this.r2Min = 0.0;
        this.c1Max = 2.5;
        this.c1Min = 1.5;
        this.c2Max = 2.5;
        this.c2Min = 1.5;
        this.weightMax = 0.1;
        this.weightMin = 0.1;
        this.changeVelocity1 = -1;
        this.changeVelocity2 = -1;

        this.mutationOperator = new PolynomialMutation(1.0 / problem.getNumberOfVariables(), 20.0);
        this.evaluator = new SequentialSolutionListEvaluator<>();

        this.variant = SMPSOVariant.SMPSO;

    }

    /* Getters */
    public int getSwarmSize() {
        return this.swarmSize;
    }

    /* Setters */
    public SMPSOBuilder setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;

        return this;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public SMPSOBuilder setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public double getR1Max() {
        return this.r1Max;
    }

    public SMPSOBuilder setR1Max(double r1Max) {
        this.r1Max = r1Max;

        return this;
    }

    public double getR1Min() {
        return this.r1Min;
    }

    public SMPSOBuilder setR1Min(double r1Min) {
        this.r1Min = r1Min;

        return this;
    }

    public double getR2Max() {
        return this.r2Max;
    }

    public SMPSOBuilder setR2Max(double r2Max) {
        this.r2Max = r2Max;

        return this;
    }

    public double getR2Min() {
        return this.r2Min;
    }

    public SMPSOBuilder setR2Min(double r2Min) {
        this.r2Min = r2Min;

        return this;
    }

    public double getC1Max() {
        return this.c1Max;
    }

    public SMPSOBuilder setC1Max(double c1Max) {
        this.c1Max = c1Max;

        return this;
    }

    public double getC1Min() {
        return this.c1Min;
    }

    public SMPSOBuilder setC1Min(double c1Min) {
        this.c1Min = c1Min;

        return this;
    }

    public double getC2Max() {
        return this.c2Max;
    }

    public SMPSOBuilder setC2Max(double c2Max) {
        this.c2Max = c2Max;

        return this;
    }

    public double getC2Min() {
        return this.c2Min;
    }

    public SMPSOBuilder setC2Min(double c2Min) {
        this.c2Min = c2Min;

        return this;
    }

    public MutationOperator<DoubleSolution> getMutation() {
        return this.mutationOperator;
    }

    public SMPSOBuilder setMutation(MutationOperator<DoubleSolution> mutation) {
        this.mutationOperator = mutation;

        return this;
    }

    public double getWeightMax() {
        return this.weightMax;
    }

    public SMPSOBuilder setWeightMax(double weightMax) {
        this.weightMax = weightMax;

        return this;
    }

    public double getWeightMin() {
        return this.weightMin;
    }

    public SMPSOBuilder setWeightMin(double weightMin) {
        this.weightMin = weightMin;

        return this;
    }

    public double getChangeVelocity1() {
        return this.changeVelocity1;
    }

    public SMPSOBuilder setChangeVelocity1(double changeVelocity1) {
        this.changeVelocity1 = changeVelocity1;

        return this;
    }

    public double getChangeVelocity2() {
        return this.changeVelocity2;
    }

    public SMPSOBuilder setChangeVelocity2(double changeVelocity2) {
        this.changeVelocity2 = changeVelocity2;

        return this;
    }

    public SMPSOBuilder setRandomGenerator(PseudoRandomGenerator randomGenerator) {
        JMetalRandom.getInstance().setRandomGenerator(randomGenerator);

        return this;
    }

    public SMPSOBuilder setSolutionListEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public SMPSOBuilder setVariant(SMPSOVariant variant) {
        this.variant = variant;

        return this;
    }

    public SMPSO build() {
        SMPSO algorithm;
        if (this.variant == SMPSOVariant.SMPSO) {
            algorithm = new SMPSO(this.problem, this.swarmSize, this.leaders, this.mutationOperator,
                this.maxIterations, this.r1Min, this.r1Max,
                    this.r2Min, this.r2Max, this.c1Min, this.c1Max, this.c2Min, this.c2Max, this.weightMin, this.weightMax, this.changeVelocity1,
                    this.changeVelocity2, this.evaluator);
        } else {
            algorithm = new SMPSOMeasures(this.problem, this.swarmSize, this.leaders, this.mutationOperator,
                this.maxIterations, this.r1Min, this.r1Max,
                    this.r2Min, this.r2Max, this.c1Min, this.c1Max, this.c2Min, this.c2Max, this.weightMin, this.weightMax, this.changeVelocity1,
                    this.changeVelocity2, this.evaluator);
        }
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.swarmSize, this.swarmSize * this.maxIterations));
        return algorithm;
    }

    /*
     * Getters
     */
    public DoubleProblem getProblem() {
        return this.problem;
    }

    public int getArchiveSize() {
        return this.archiveSize;
    }

    public MutationOperator<DoubleSolution> getMutationOperator() {
        return this.mutationOperator;
    }

    public BoundedArchive<DoubleSolution> getLeaders() {
        return this.leaders;
    }

    public SolutionListEvaluator<DoubleSolution> getEvaluator() {
        return this.evaluator;
    }

    public enum SMPSOVariant {SMPSO, Measures}
}



