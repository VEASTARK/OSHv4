package org.uma.jmetal.algorithm.singleobjective.evolutionstrategy;

import org.uma.jmetal.algorithm.impl.AbstractEvolutionStrategy;
import org.uma.jmetal.algorithm.singleobjective.evolutionstrategy.util.CMAESUtils;
import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Class implementing the CMA-ES algorithm
 */
@SuppressWarnings("serial")
public final class CovarianceMatrixAdaptationEvolutionStrategy
        extends AbstractEvolutionStrategy<DoubleSolution, DoubleSolution> {
    private final Comparator<DoubleSolution> comparator;
    private final int lambda;
    private final double[] typicalX;
    private final JMetalRandom rand;
    private int evaluations;
    private boolean eigenValuesStoppingCondition;
    /**
     * CMA-ES state variables
     */

    // Distribution mean and current favorite solution to the optimization problem
    private double[] distributionMean;
    // coordinate wise standard deviation (step size)
    private double sigma;
    // Symmetric and positive definitive covariance matrix
    private double[][] c;
    // Evolution paths for c and sigma
    private double[] pathsC;

    /*
     * Strategy parameter setting: Selection
     */
    private double[] pathsSigma;
    // number of parents/points for recombination
    private int mu;
    private double[] weights;

    /*
     * Strategy parameter setting: Adaptation
     */
    private double muEff;
    // time constant for cumulation for c
    private double cumulationC;
    // t-const for cumulation for sigma control
    private double cumulationSigma;
    // learning rate for rank-one update of c
    private double c1;
    // learning rate for rank-mu update
    private double cmu;

    /*
     * Dynamic (internal) strategy parameters and constants
     */
    // damping for sigma
    private double dampingSigma;
    // coordinate system
    private double[][] b;
    // diagonal D defines the scaling
    private double[] diagD;
    // c^1/2
    private double[][] invSqrtC;
    // track update of b and c
    private int eigenEval;
    private double chiN;
    private DoubleSolution bestSolutionEver;

    /**
     * Constructor
     */
    public CovarianceMatrixAdaptationEvolutionStrategy(DoubleProblem problem, int lambda, double[] typicalX, double sigma,
                                                       IEALogger eaLogger) {
        super(problem, eaLogger);
        this.lambda = lambda;
        this.typicalX = typicalX;
        this.sigma = sigma;

        this.rand = JMetalRandom.getInstance();
        this.comparator = new ObjectiveComparator<>(0);

        this.initializeInternalParameters();
    }

    private CovarianceMatrixAdaptationEvolutionStrategy(Builder builder) {
        this(builder.problem, builder.lambda, builder.typicalX, builder.sigma, builder.eaLogger);
    }

    /* Getters */
    public int getLambda() {
        return this.lambda;
    }

    @Override
    protected void initProgress() {
        this.evaluations = this.lambda;
        this.getEALogger().logPopulation(this.population, this.evaluations / this.lambda);
    }

    @Override
    protected void updateProgress() {
        this.evaluations += this.lambda;
        this.updateInternalParameters();
        this.getEALogger().logPopulation(this.population, this.evaluations / this.lambda);
    }

    @Override
    protected boolean isStoppingConditionReached() {
        if (this.eigenValuesStoppingCondition) {
            this.getEALogger().logAdditional("Eigenvalues stopping condition reached");
            return true;
        } else {
            for (StoppingRule sr : this.getStoppingRules()) {
                if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                    this.getEALogger().logAdditional(sr.getMsg());
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected List<DoubleSolution> createInitialPopulation() {
        List<DoubleSolution> population = new ArrayList<>(this.lambda);
        for (int i = 0; i < this.lambda; i++) {
            DoubleSolution newIndividual = this.getProblem().createSolution();
            population.add(newIndividual);
        }
        return population;
    }

    @Override
    protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
        for (DoubleSolution solution : population) {
            this.getProblem().evaluate(solution);
        }
        return population;
    }

    @Override
    protected List<DoubleSolution> selection(List<DoubleSolution> population) {
        return population;
    }

    @Override
    protected List<DoubleSolution> reproduction(List<DoubleSolution> population) {

        List<DoubleSolution> offspringPopulation = new ArrayList<>(this.lambda);

        for (int iNk = 0; iNk < this.lambda; iNk++) {
            offspringPopulation.add(this.sampleSolution());
        }

        return offspringPopulation;
    }

    @Override
    protected List<DoubleSolution> replacement(List<DoubleSolution> population,
                                               List<DoubleSolution> offspringPopulation) {
        return offspringPopulation;
    }

    @Override
    public DoubleSolution getResult() {
        return this.bestSolutionEver;
    }

    private void initializeInternalParameters() {

        // number of objective variables/problem dimension
        int numberOfVariables = this.getProblem().getNumberOfVariables();

        // objective variables initial point
        // TODO: Initialize the mean in a better way

        if (this.typicalX != null) {
            this.distributionMean = this.typicalX;
        } else {
            this.distributionMean = new double[numberOfVariables];
            for (int i = 0; i < numberOfVariables; i++) {
                this.distributionMean[i] = this.rand.nextDouble();
            }
        }

        /* Strategy parameter setting: Selection */

        // number of parents/points for recombination
        this.mu = (int) Math.floor(this.lambda / 2);

        // muXone array for weighted recombination
        this.weights = new double[this.mu];
        double sum = 0;
        for (int i = 0; i < this.mu; i++) {
            this.weights[i] = (Math.log(this.mu + 1 / 2) - Math.log(i + 1));
            sum += this.weights[i];
        }
        // normalize recombination weights array
        for (int i = 0; i < this.mu; i++) {
            this.weights[i] /= sum;
        }

        // variance-effectiveness of sum w_i x_i
        double sum1 = 0;
        double sum2 = 0;
        for (int i = 0; i < this.mu; i++) {
            sum1 += this.weights[i];
            sum2 += this.weights[i] * this.weights[i];
        }
        this.muEff = sum1 * sum1 / sum2;

        /* Strategy parameter setting: Adaptation */

        // time constant for cumulation for C
        this.cumulationC =
                (4 + this.muEff / numberOfVariables) / (numberOfVariables + 4 + 2 * this.muEff / numberOfVariables);

        // t-const for cumulation for sigma control
        this.cumulationSigma = (this.muEff + 2) / (numberOfVariables + this.muEff + 5);

        // learning rate for rank-one update of C
        this.c1 = 2 / ((numberOfVariables + 1.3) * (numberOfVariables + 1.3) + this.muEff);

        // learning rate for rank-mu update
        this.cmu = Math.min(1 - this.c1,
                2 * (this.muEff - 2 + 1 / this.muEff) / ((numberOfVariables + 2) * (numberOfVariables + 2) + this.muEff));

        // damping for sigma, usually close to 1
        this.dampingSigma = 1 +
                2 * Math.max(0, Math.sqrt((this.muEff - 1) / (numberOfVariables + 1)) - 1) + this.cumulationSigma;

        /* Initialize dynamic (internal) strategy parameters and constants */

        // diagonal D defines the scaling
        this.diagD = new double[numberOfVariables];

        // evolution paths for C and sigma
        this.pathsC = new double[numberOfVariables];
        this.pathsSigma = new double[numberOfVariables];

        // b defines the coordinate system
        this.b = new double[numberOfVariables][numberOfVariables];
        // covariance matrix C
        this.c = new double[numberOfVariables][numberOfVariables];

        // C^-1/2
        this.invSqrtC = new double[numberOfVariables][numberOfVariables];

        for (int i = 0; i < numberOfVariables; i++) {
            this.pathsC[i] = 0;
            this.pathsSigma[i] = 0;
            this.diagD[i] = 1;
            for (int j = 0; j < numberOfVariables; j++) {
                this.b[i][j] = 0;
                this.invSqrtC[i][j] = 0;
            }
            for (int j = 0; j < i; j++) {
                this.c[i][j] = 0;
            }
            this.b[i][i] = 1;
            this.c[i][i] = this.diagD[i] * this.diagD[i];
            this.invSqrtC[i][i] = 1;
        }

        // track update of b and D
        this.eigenEval = 0;

        this.chiN = Math.sqrt(numberOfVariables) * (1 - 1 / (4 * numberOfVariables) + 1 / (21
                * numberOfVariables * numberOfVariables));

    }

    private void updateInternalParameters() {

        int numberOfVariables = this.getProblem().getNumberOfVariables();

        double[] oldDistributionMean = new double[numberOfVariables];
        System.arraycopy(this.distributionMean, 0, oldDistributionMean, 0, numberOfVariables);

        // Sort by fitness and compute weighted mean into distributionMean
        // minimization
        this.getPopulation().sort(this.comparator);
        this.storeBest();

        // calculate new distribution mean and BDz~N(0,C)
        this.updateDistributionMean();

        // Cumulation: Update evolution paths
        int hsig = this.updateEvolutionPaths(oldDistributionMean);

        // Adapt covariance matrix C
        this.adaptCovarianceMatrix(oldDistributionMean, hsig);

        // Adapt step size sigma
        double psxps = CMAESUtils.norm(this.pathsSigma);
        this.sigma *= Math.exp((this.cumulationSigma / this.dampingSigma) * (Math.sqrt(psxps) / this.chiN - 1));

        // Decomposition of C into b*diag(D.^2)*b' (diagonalization)
        this.decomposeCovarianceMatrix();

    }

    private void updateDistributionMean() {

        int numberOfVariables = this.getProblem().getNumberOfVariables();

        for (int i = 0; i < numberOfVariables; i++) {
            this.distributionMean[i] = 0.0;
            for (int iNk = 0; iNk < this.mu; iNk++) {
                double variableValue = this.getPopulation().get(iNk).getUnboxedVariableValue(i);
                this.distributionMean[i] += this.weights[iNk] * variableValue;
            }
        }

    }

    private int updateEvolutionPaths(double[] oldDistributionMean) {

        int numberOfVariables = this.getProblem().getNumberOfVariables();

        double[] artmp = new double[numberOfVariables];
        for (int i = 0; i < numberOfVariables; i++) {
            artmp[i] = 0;
            for (int j = 0; j < numberOfVariables; j++) {
                artmp[i] += this.invSqrtC[i][j] * (this.distributionMean[j] - oldDistributionMean[j]) / this.sigma;
            }
        }
        // cumulation for sigma (pathsSigma)
        for (int i = 0; i < numberOfVariables; i++) {
            this.pathsSigma[i] = (1.0 - this.cumulationSigma) * this.pathsSigma[i]
                    + Math.sqrt(this.cumulationSigma * (2.0 - this.cumulationSigma) * this.muEff) * artmp[i];
        }

        // calculate norm(pathsSigma)^2
        double psxps = CMAESUtils.norm(this.pathsSigma);

        // cumulation for covariance matrix (pathsC)
        int hsig = 0;
        if ((Math.sqrt(psxps) / Math
                .sqrt(1.0 - Math.pow(1.0 - this.cumulationSigma, 2.0 * this.evaluations / this.lambda)) / this.chiN) < (1.4
                + 2.0 / (numberOfVariables + 1.0))) {
            hsig = 1;
        }
        for (int i = 0; i < numberOfVariables; i++) {
            this.pathsC[i] = (1.0 - this.cumulationC) * this.pathsC[i]
                    + hsig * Math.sqrt(this.cumulationC * (2.0 - this.cumulationC) * this.muEff)
                    * (this.distributionMean[i] - oldDistributionMean[i])
                    / this.sigma;
        }

        return hsig;

    }

    private void adaptCovarianceMatrix(double[] oldDistributionMean, int hsig) {

        int numberOfVariables = this.getProblem().getNumberOfVariables();

        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = 0; j <= i; j++) {
                this.c[i][j] = (1 - this.c1 - this.cmu) * this.c[i][j]
                        + this.c1
                        * (this.pathsC[i] * this.pathsC[j] + (1 - hsig) * this.cumulationC
                        * (2.0 - this.cumulationC) * this.c[i][j]);
                for (int k = 0; k < this.mu; k++) {
                    /*
                     * additional rank mu
                     * update
                     */
                    double valueI = this.getPopulation().get(k).getUnboxedVariableValue(i);
                    double valueJ = this.getPopulation().get(k).getUnboxedVariableValue(j);
                    this.c[i][j] += this.cmu
                            * this.weights[k]
                            * (valueI - oldDistributionMean[i])
                            * (valueJ - oldDistributionMean[j]) / this.sigma
                            / this.sigma;
                }
            }
        }

    }

    private void decomposeCovarianceMatrix() {
        int numberOfVariables = this.getProblem().getNumberOfVariables();

        if (this.evaluations - this.eigenEval > this.lambda / (this.c1 + this.cmu) / numberOfVariables / 10) {

            this.eigenEval = this.evaluations;

            // enforce symmetry
            for (int i = 0; i < numberOfVariables; i++) {
                for (int j = 0; j <= i; j++) {
                    this.b[i][j] = this.b[j][i] = this.c[i][j];
                }
            }

            // eigen decomposition, b==normalized eigenvectors
            double[] offdiag = new double[numberOfVariables];
            CMAESUtils.tred2(numberOfVariables, this.b, this.diagD, offdiag);
            CMAESUtils.tql2(numberOfVariables, this.diagD, offdiag, this.b);

            this.checkEigenCorrectness();

            double[][] artmp2 = new double[numberOfVariables][numberOfVariables];
            for (int i = 0; i < numberOfVariables; i++) {
                if (this.diagD[i] > 0) {
                    this.diagD[i] = Math.sqrt(this.diagD[i]);
                }
                for (int j = 0; j < numberOfVariables; j++) {
                    artmp2[i][j] = this.b[i][j] * (1 / this.diagD[j]);
                }
            }
            for (int i = 0; i < numberOfVariables; i++) {
                for (int j = 0; j < numberOfVariables; j++) {
                    this.invSqrtC[i][j] = 0.0;
                    for (int k = 0; k < numberOfVariables; k++) {
                        this.invSqrtC[i][j] += artmp2[i][k] * this.b[j][k];
                    }
                }
            }

        }

    }

    private void checkEigenCorrectness() {
        int numberOfVariables = this.getProblem().getNumberOfVariables();

        if (CMAESUtils.checkEigenSystem(numberOfVariables, this.c, this.diagD, this.b) > 0) {
            this.eigenValuesStoppingCondition = true;
        }

        for (int i = 0; i < numberOfVariables; i++) {
            // Numerical problem?
            if (this.diagD[i] < 0) {
                JMetalLogger.logger.severe(
                        "CovarianceMatrixAdaptationEvolutionStrategy.updateDistribution:" +
                                " WARNING - an eigenvalue has become negative.");
                this.eigenValuesStoppingCondition = true;
            }
        }

    }

    private DoubleSolution sampleSolution() {

        DoubleSolution solution = this.getProblem().createSolution();

        int numberOfVariables = this.getProblem().getNumberOfVariables();
        double[] artmp = new double[numberOfVariables];
        double sum;

        for (int i = 0; i < numberOfVariables; i++) {
            //TODO: Check the correctness of this random (http://en.wikipedia.org/wiki/CMA-ES)
            artmp[i] = this.diagD[i] * this.rand.nextGaussian();
        }
        for (int i = 0; i < numberOfVariables; i++) {
            sum = 0.0;
            for (int j = 0; j < numberOfVariables; j++) {
                sum += this.b[i][j] * artmp[j];
            }

            double value = this.distributionMean[i] + this.sigma * sum;
            if (value > ((DoubleProblem) this.getProblem()).getUnboxedUpperBound(i)) {
                value = ((DoubleProblem) this.getProblem()).getUnboxedUpperBound(i);
            } else if (value < ((DoubleProblem) this.getProblem()).getUnboxedLowerBound(i)) {
                value = ((DoubleProblem) this.getProblem()).getUnboxedLowerBound(i);
            }

            solution.setUnboxedVariableValue(i, value);
        }

        return solution;
    }

    private void storeBest() {
        if ((this.bestSolutionEver == null) || (this.bestSolutionEver.getObjective(0) > this.getPopulation().get(0)
                .getObjective(0))) {
            this.bestSolutionEver = this.getPopulation().get(0);
        }
    }

    @Override
    public String getName() {
        return "CMAES";
    }

    @Override
    public String getDescription() {
        return "Covariance Matrix Adaptation Evolution Strategy";
    }

    /**
     * Buider class
     */
    public static class Builder {
        private static final int DEFAULT_LAMBDA = 10;
        private static final int DEFAULT_MAX_EVALUATIONS = 1000000;
        private static final double DEFAULT_SIGMA = 0.3;

        private final DoubleProblem problem;
        private final IEALogger eaLogger;
        private int lambda;
        private int maxEvaluations;
        private double[] typicalX;
        private double sigma;

        public Builder(DoubleProblem problem, IEALogger eaLogger) {
            this.problem = problem;
            this.eaLogger = eaLogger;
            this.lambda = DEFAULT_LAMBDA;
            this.maxEvaluations = DEFAULT_MAX_EVALUATIONS;
            this.sigma = DEFAULT_SIGMA;
        }

        public Builder setLambda(int lambda) {
            this.lambda = lambda;
            return this;
        }

        public Builder setMaxEvaluations(int maxEvaluations) {
            this.maxEvaluations = maxEvaluations;
            return this;
        }

        public Builder setTypicalX(double[] typicalX) {
            this.typicalX = typicalX;
            return this;
        }

        public Builder setSigma(double sigma) {
            this.sigma = sigma;
            return this;
        }

        public CovarianceMatrixAdaptationEvolutionStrategy build() {
            CovarianceMatrixAdaptationEvolutionStrategy algorithm = new CovarianceMatrixAdaptationEvolutionStrategy(this);
            algorithm.addStoppingRule(new EvaluationsStoppingRule(this.lambda, this.maxEvaluations));
            return algorithm;
        }
    }

}
