package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;

/**
 * Builder class for algorithm MOEA/D and variants
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
public class MOEADBuilder implements AlgorithmBuilder<AbstractMOEAD<DoubleSolution>> {
    protected final Problem<DoubleSolution> problem;
    protected final Variant moeadVariant;
    /**
     * T in Zhang & Li paper
     */
    protected int neighborSize;
    /**
     * Delta in Zhang & Li paper
     */
    protected double neighborhoodSelectionProbability;
    /**
     * nr in Zhang & Li paper
     */
    protected int maximumNumberOfReplacedSolutions;

    protected MOEAD.FunctionType functionType;

    protected CrossoverOperator<DoubleSolution> crossover;
    protected MutationOperator<DoubleSolution> mutation;
    protected String dataDirectory;

    protected int populationSize;
    protected int resultPopulationSize;

    protected int maxEvaluations;

    protected int numberOfThreads;

    /**
     * Constructor
     */
    public MOEADBuilder(Problem<DoubleSolution> problem, Variant variant) {
        this.problem = problem;
        this.populationSize = 300;
        this.resultPopulationSize = 300;
        this.maxEvaluations = 150000;
        this.crossover = new DifferentialEvolutionCrossover();
        this.mutation = new PolynomialMutation(1.0 / problem.getNumberOfVariables(), 20.0);
        this.functionType = MOEAD.FunctionType.TCHE;
        this.neighborhoodSelectionProbability = 0.1;
        this.maximumNumberOfReplacedSolutions = 2;
        this.dataDirectory = "";
        this.neighborSize = 20;
        this.numberOfThreads = 1;
        this.moeadVariant = variant;
    }

    /* Getters/Setters */
    public int getNeighborSize() {
        return this.neighborSize;
    }

    public MOEADBuilder setNeighborSize(int neighborSize) {
        this.neighborSize = neighborSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public MOEADBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public int getPopulationSize() {
        return this.populationSize;
    }

    public MOEADBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getResultPopulationSize() {
        return this.resultPopulationSize;
    }

    public MOEADBuilder setResultPopulationSize(int resultPopulationSize) {
        this.resultPopulationSize = resultPopulationSize;

        return this;
    }

    public String getDataDirectory() {
        return this.dataDirectory;
    }

    public MOEADBuilder setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;

        return this;
    }

    public MutationOperator<DoubleSolution> getMutation() {
        return this.mutation;
    }

    public MOEADBuilder setMutation(MutationOperator<DoubleSolution> mutation) {
        this.mutation = mutation;

        return this;
    }

    public CrossoverOperator<DoubleSolution> getCrossover() {
        return this.crossover;
    }

    public MOEADBuilder setCrossover(CrossoverOperator<DoubleSolution> crossover) {
        this.crossover = crossover;

        return this;
    }

    public MOEAD.FunctionType getFunctionType() {
        return this.functionType;
    }

    public MOEADBuilder setFunctionType(MOEAD.FunctionType functionType) {
        this.functionType = functionType;

        return this;
    }

    public int getMaximumNumberOfReplacedSolutions() {
        return this.maximumNumberOfReplacedSolutions;
    }

    public MOEADBuilder setMaximumNumberOfReplacedSolutions(int maximumNumberOfReplacedSolutions) {
        this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions;

        return this;
    }

    public double getNeighborhoodSelectionProbability() {
        return this.neighborhoodSelectionProbability;
    }

    public MOEADBuilder setNeighborhoodSelectionProbability(double neighborhoodSelectionProbability) {
        this.neighborhoodSelectionProbability = neighborhoodSelectionProbability;

        return this;
    }

    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    public MOEADBuilder setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;

        return this;
    }

    public AbstractMOEAD<DoubleSolution> build() {
        AbstractMOEAD<DoubleSolution> algorithm = null;
        if (this.moeadVariant == Variant.MOEAD) {
            algorithm = new MOEAD(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations, this.mutation,
                    this.crossover, this.functionType, this.dataDirectory, this.neighborhoodSelectionProbability,
                    this.maximumNumberOfReplacedSolutions, this.neighborSize);
        } else if (this.moeadVariant == Variant.ConstraintMOEAD) {
            algorithm = new ConstraintMOEAD(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations, this.mutation,
                    this.crossover, this.functionType, this.dataDirectory, this.neighborhoodSelectionProbability,
                    this.maximumNumberOfReplacedSolutions, this.neighborSize);
        } else if (this.moeadVariant == Variant.MOEADDRA) {
            algorithm = new MOEADDRA(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations, this.mutation,
                    this.crossover, this.functionType, this.dataDirectory, this.neighborhoodSelectionProbability,
                    this.maximumNumberOfReplacedSolutions, this.neighborSize);
        } else if (this.moeadVariant == Variant.MOEADSTM) {
            algorithm = new MOEADSTM(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations, this.mutation,
                    this.crossover, this.functionType, this.dataDirectory, this.neighborhoodSelectionProbability,
                    this.maximumNumberOfReplacedSolutions, this.neighborSize);
        } else if (this.moeadVariant == Variant.MOEADD) {
            algorithm = new MOEADD<>(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations, this.crossover, this.mutation,
                    this.functionType, this.dataDirectory, this.neighborhoodSelectionProbability,
                    this.maximumNumberOfReplacedSolutions, this.neighborSize);
        }
        return algorithm;
    }

    public enum Variant {MOEAD, ConstraintMOEAD, MOEADDRA, MOEADSTM, MOEADD}
}
