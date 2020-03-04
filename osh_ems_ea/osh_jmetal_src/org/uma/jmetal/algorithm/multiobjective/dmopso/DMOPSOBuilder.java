package org.uma.jmetal.algorithm.multiobjective.dmopso;

import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.PseudoRandomGenerator;

/**
 * @author Jorge Rodriguez
 */
public class DMOPSOBuilder implements AlgorithmBuilder<DMOPSO> {
    private final DoubleProblem problem;
    private String name;
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
    private int maxAge;
    private String dataDirectory;
    private DMOPSO.FunctionType functionType;
    private SolutionListEvaluator<DoubleSolution> evaluator;
    private DMOPSOVariant variant;

    public DMOPSOBuilder(DoubleProblem problem) {
        this.name = "dMOPSO";
        this.problem = problem;

        this.swarmSize = 100;
        this.maxIterations = 250;
        this.maxAge = 2;

        this.functionType = DMOPSO.FunctionType.PBI;

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

        this.evaluator = new SequentialSolutionListEvaluator<>();

        this.variant = DMOPSOVariant.DMOPSO;

    }

    /* Getters */
    public int getSwarmSize() {
        return this.swarmSize;
    }

    /* Setters */
    public DMOPSOBuilder setSwarmSize(int swarmSize) {
        this.swarmSize = swarmSize;

        return this;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public DMOPSOBuilder setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;

        return this;
    }

    public double getR1Max() {
        return this.r1Max;
    }

    public DMOPSOBuilder setR1Max(double r1Max) {
        this.r1Max = r1Max;

        return this;
    }

    public double getR1Min() {
        return this.r1Min;
    }

    public DMOPSOBuilder setR1Min(double r1Min) {
        this.r1Min = r1Min;

        return this;
    }

    public double getR2Max() {
        return this.r2Max;
    }

    public DMOPSOBuilder setR2Max(double r2Max) {
        this.r2Max = r2Max;

        return this;
    }

    public double getR2Min() {
        return this.r2Min;
    }

    public DMOPSOBuilder setR2Min(double r2Min) {
        this.r2Min = r2Min;

        return this;
    }

    public double getC1Max() {
        return this.c1Max;
    }

    public DMOPSOBuilder setC1Max(double c1Max) {
        this.c1Max = c1Max;

        return this;
    }

    public double getC1Min() {
        return this.c1Min;
    }

    public DMOPSOBuilder setC1Min(double c1Min) {
        this.c1Min = c1Min;

        return this;
    }

    public double getC2Max() {
        return this.c2Max;
    }

    public DMOPSOBuilder setC2Max(double c2Max) {
        this.c2Max = c2Max;

        return this;
    }

    public double getC2Min() {
        return this.c2Min;
    }

    public DMOPSOBuilder setC2Min(double c2Min) {
        this.c2Min = c2Min;

        return this;
    }

    public double getWeightMax() {
        return this.weightMax;
    }

    public DMOPSOBuilder setWeightMax(double weightMax) {
        this.weightMax = weightMax;

        return this;
    }

    public double getWeightMin() {
        return this.weightMin;
    }

    public DMOPSOBuilder setWeightMin(double weightMin) {
        this.weightMin = weightMin;

        return this;
    }

    public double getChangeVelocity1() {
        return this.changeVelocity1;
    }

    public DMOPSOBuilder setChangeVelocity1(double changeVelocity1) {
        this.changeVelocity1 = changeVelocity1;

        return this;
    }

    public double getChangeVelocity2() {
        return this.changeVelocity2;
    }

    public DMOPSOBuilder setChangeVelocity2(double changeVelocity2) {
        this.changeVelocity2 = changeVelocity2;

        return this;
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public DMOPSOBuilder setMaxAge(int maxAge) {
        this.maxAge = maxAge;

        return this;
    }

    public String getDataDirectory() {
        return this.dataDirectory;
    }

    public DMOPSOBuilder setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        return this;
    }

    public DMOPSO.FunctionType getFunctionType() {
        return this.functionType;
    }

    public DMOPSOBuilder setFunctionType(DMOPSO.FunctionType functionType) {
        this.functionType = functionType;

        return this;
    }

    public String getName() {
        return this.name;
    }

    public DMOPSOBuilder setName(String name) {
        this.name = name;

        return this;
    }

    public DMOPSOBuilder setRandomGenerator(PseudoRandomGenerator randomGenerator) {
        JMetalRandom.getInstance().setRandomGenerator(randomGenerator);

        return this;
    }

    public DMOPSOBuilder setSolutionListEvaluator(SolutionListEvaluator<DoubleSolution> evaluator) {
        this.evaluator = evaluator;

        return this;
    }

    public DMOPSOBuilder setVariant(DMOPSOVariant variant) {
        this.variant = variant;

        return this;
    }

    public DMOPSO build() {
        DMOPSO algorithm = null;
        if (this.variant == DMOPSOVariant.DMOPSO) {
            algorithm = new DMOPSO(this.problem, this.swarmSize, this.maxIterations, this.r1Min, this.r1Max, this.r2Min, this.r2Max, this.c1Min, this.c1Max, this.c2Min,
                    this.c2Max, this.weightMin, this.weightMax, this.changeVelocity1, this.changeVelocity2, this.functionType, this.dataDirectory, this.maxAge,
                    this.name);
        } else if (this.variant == DMOPSOVariant.Measures) {
            algorithm = new DMOPSOMeasures(this.problem, this.swarmSize, this.maxIterations, this.r1Min, this.r1Max, this.r2Min, this.r2Max, this.c1Min, this.c1Max,
                    this.c2Min, this.c2Max, this.weightMin, this.weightMax, this.changeVelocity1, this.changeVelocity2, this.functionType, this.dataDirectory,
                    this.maxAge, this.name);
        }
        return algorithm;
    }

    public DoubleProblem getProblem() {
        return this.problem;
    }

    /*
     * Getters
     */

    public SolutionListEvaluator<DoubleSolution> getEvaluator() {
        return this.evaluator;
    }

    public enum DMOPSOVariant {
        DMOPSO, Measures
    }
}
