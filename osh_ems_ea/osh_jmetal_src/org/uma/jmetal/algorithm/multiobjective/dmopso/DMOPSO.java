package org.uma.jmetal.algorithm.multiobjective.dmopso;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

@SuppressWarnings("serial")
public class DMOPSO implements Algorithm<List<DoubleSolution>> {

    protected final int swarmSize;
    protected final int maxIterations;
    protected final int maxAge;
    final double[] z;
    final double[][] lambda;
    final DoubleSolution[] indArray;
    final String dataDirectory;
    final FunctionType functionType;
    private final String name;
    private final DoubleProblem problem;
    private final double c1Max;
    private final double c1Min;
    private final double c2Max;
    private final double c2Min;
    private final double r1Max;
    private final double r1Min;
    private final double r2Max;
    private final double r2Min;
    private final double weightMax;
    private final double weightMin;
    private final double changeVelocity1;
    private final double changeVelocity2;
    private final DoubleSolution[] localBest;
    private final DoubleSolution[] globalBest;
    private final int[] shfGBest;
    private final double[][] speed;
    private final int[] age;
    private final double[] deltaMax;
    private final double[] deltaMin;
    private final JMetalRandom randomGenerator;
    private final SolutionListEvaluator<DoubleSolution> evaluator;
    protected int iterations;
    private List<DoubleSolution> swarm;
    public DMOPSO(DoubleProblem problem, int swarmSize,
                  int maxIterations, double r1Min, double r1Max,
                  double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
                  double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                  FunctionType functionType, String dataDirectory, int maxAge) {
        this(problem, swarmSize,
                maxIterations, r1Min, r1Max,
                r2Min, r2Max, c1Min, c1Max, c2Min, c2Max,
                weightMin, weightMax, changeVelocity1, changeVelocity2,
                functionType, dataDirectory, maxAge, "dMOPSO");
    }

    public DMOPSO(DoubleProblem problem, int swarmSize,
                  int maxIterations, double r1Min, double r1Max,
                  double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
                  double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                  FunctionType functionType, String dataDirectory, int maxAge, String name) {
        this.name = name;
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;

        this.r1Max = r1Max;
        this.r1Min = r1Min;
        this.r2Max = r2Max;
        this.r2Min = r2Min;
        this.c1Max = c1Max;
        this.c1Min = c1Min;
        this.c2Max = c2Max;
        this.c2Min = c2Min;
        this.weightMax = weightMax;
        this.weightMin = weightMin;
        this.changeVelocity1 = changeVelocity1;
        this.changeVelocity2 = changeVelocity2;
        this.functionType = functionType;
        this.maxAge = maxAge;

        this.dataDirectory = dataDirectory;

        this.evaluator = new SequentialSolutionListEvaluator<>();

        this.randomGenerator = JMetalRandom.getInstance();

        this.localBest = new DoubleSolution[swarmSize];
        this.globalBest = new DoubleSolution[swarmSize];
        this.shfGBest = new int[swarmSize];
        this.speed = new double[swarmSize][problem.getNumberOfVariables()];
        this.age = new int[swarmSize];

        this.indArray = new DoubleSolution[problem.getNumberOfObjectives()];
        this.z = new double[problem.getNumberOfObjectives()];
        this.lambda = new double[swarmSize][problem.getNumberOfObjectives()];

        this.deltaMax = new double[problem.getNumberOfVariables()];
        this.deltaMin = new double[problem.getNumberOfVariables()];
        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            this.deltaMax[i] = (problem.getUpperBound(i) -
                    problem.getLowerBound(i)) / 2.0;
            this.deltaMin[i] = -this.deltaMax[i];
        }
    }

    public List<DoubleSolution> getSwarm() {
        return this.swarm;
    }

    protected void initProgress() {
        this.iterations = 1;
    }

    protected void updateProgress() {
        this.iterations++;
    }

    protected boolean isStoppingConditionReached() {
        return this.iterations >= this.maxIterations;
    }

    protected List<DoubleSolution> createInitialSwarm() {
        List<DoubleSolution> swarm = new ArrayList<>(this.swarmSize);

        DoubleSolution newSolution;
        for (int i = 0; i < this.swarmSize; i++) {
            newSolution = this.problem.createSolution();
            swarm.add(newSolution);
        }

        return swarm;
    }

    protected List<DoubleSolution> evaluateSwarm(List<DoubleSolution> swarm) {
        swarm = this.evaluator.evaluate(swarm, this.problem);

        return swarm;
    }

    protected void initializeLeaders(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarm.size(); i++) {
            DoubleSolution particle = (DoubleSolution) this.swarm.get(i).copy();
            this.globalBest[i] = particle;
        }

        this.updateGlobalBest();
    }

    protected void initializeParticlesMemory(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarm.size(); i++) {
            DoubleSolution particle = (DoubleSolution) this.swarm.get(i).copy();
            this.localBest[i] = particle;
        }
    }

    protected void initializeVelocity(List<DoubleSolution> swarm) {
        // Initialize the speed and age of each particle to 0
        for (int i = 0; i < this.swarmSize; i++) {
            for (int j = 0; j < this.problem.getNumberOfVariables(); j++) {
                this.speed[i][j] = 0.0;
            }
            this.age[i] = 0;
        }
    }

    protected void updateVelocity(int i) {

        DoubleSolution particle = this.swarm.get(i);
        DoubleSolution bestParticle = this.localBest[i];
        DoubleSolution bestGlobal = this.globalBest[this.shfGBest[i]];

        double r1 = this.randomGenerator.nextDouble(this.r1Min, this.r1Max);
        double r2 = this.randomGenerator.nextDouble(this.r2Min, this.r2Max);
        double C1 = this.randomGenerator.nextDouble(this.c1Min, this.c1Max);
        double C2 = this.randomGenerator.nextDouble(this.c2Min, this.c2Max);

        for (int var = 0; var < particle.getNumberOfVariables(); var++) {
            //Computing the velocity of this particle
            this.speed[i][var] = this.velocityConstriction(this.constrictionCoefficient(C1, C2) *
                    (this.inertiaWeight(this.iterations, this.maxIterations, this.weightMax, this.weightMin) * this.speed[i][var] +
                            C1 * r1 * (bestParticle.getVariableValue(var) -
                                    particle.getVariableValue(var)) +
                            C2 * r2 * (bestGlobal.getVariableValue(var) -
                                    particle.getVariableValue(var))), this.deltaMax, this.deltaMin, var, i);

        }
    }

    private void computeNewPositions(int i) {
        DoubleSolution particle = this.swarm.get(i);
        for (int var = 0; var < particle.getNumberOfVariables(); var++) {
            particle.setVariableValue(var, particle.getVariableValue(var) + this.speed[i][var]);
        }
    }

    /**
     * initUniformWeight
     */
    private void initUniformWeight() {
        if ((this.problem.getNumberOfObjectives() == 2) && (this.swarmSize < 300)) {
            for (int n = 0; n < this.swarmSize; n++) {
                double a = 1.0 * n / (this.swarmSize - 1);
                this.lambda[n][0] = a;
                this.lambda[n][1] = 1 - a;
            }
        } else {
            String dataFileName;
            dataFileName = "W" + this.problem.getNumberOfObjectives() + "D_" +
                    this.swarmSize + ".dat";

            try {
                InputStream in = this.getClass().getResourceAsStream("/" + this.dataDirectory + "/" + dataFileName);
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = Double.parseDouble(st.nextToken());
                        this.lambda[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                throw new JMetalException("initUniformWeight: failed when reading for file: " + this.dataDirectory + "/" + dataFileName);
            }
        }
    }

    private void initIdealPoint() {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.z[i] = 1.0e+30;
            this.indArray[i] = this.problem.createSolution();
            this.problem.evaluate(this.indArray[i]);
        }

        for (int i = 0; i < this.swarmSize; i++) {
            this.updateReference(this.swarm.get(i));
        }
    }

    private void updateReference(DoubleSolution individual) {
        for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < this.z[n]) {
                this.z[n] = individual.getObjective(n);

                this.indArray[n] = (DoubleSolution) individual.copy();
            }
        }
    }

    private void updateGlobalBest() {

        double gBestFitness;

        for (int j = 0; j < this.lambda.length; j++) {
            gBestFitness = this.fitnessFunction(this.globalBest[j], this.lambda[j]);

            for (DoubleSolution doubleSolution : this.swarm) {
                double v1 = this.fitnessFunction(doubleSolution, this.lambda[j]);
                double v2 = gBestFitness;
                if (v1 < v2) {
                    this.globalBest[j] = (DoubleSolution) doubleSolution.copy();
                    gBestFitness = v1;
                }
            }
        }
    }

    private void updateLocalBest(int part) {

        double f1, f2;
        DoubleSolution indiv = (DoubleSolution) this.swarm.get(part).copy();

        f1 = this.fitnessFunction(this.localBest[part], this.lambda[part]);
        f2 = this.fitnessFunction(indiv, this.lambda[part]);

        if (this.age[part] >= this.maxAge || f2 <= f1) {
            this.localBest[part] = indiv;
            this.age[part] = 0;
        } else {
            this.age[part]++;
        }
    }

    private double fitnessFunction(DoubleSolution sol, double[] lambda) {
        double fitness = 0.0;

        if (this.functionType == FunctionType.TCHE) {
            double maxFun = -1.0e+30;

            for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
                double diff = Math.abs(sol.getObjective(n) - this.z[n]);

                double feval;
                if (lambda[n] == 0) {
                    feval = 0.0001 * diff;
                } else {
                    feval = diff * lambda[n];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            }

            fitness = maxFun;

        } else if (this.functionType == FunctionType.AGG) {
            double sum = 0.0;
            for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
                sum += (lambda[n]) * sol.getObjective(n);
            }

            fitness = sum;

        } else if (this.functionType == FunctionType.PBI) {
            double d1, d2, nl;
            double theta = 5.0;

            d1 = d2 = nl = 0.0;

            for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
                d1 += (sol.getObjective(i) - this.z[i]) * lambda[i];
                nl += Math.pow(lambda[i], 2.0);
            }
            nl = Math.sqrt(nl);
            d1 = Math.abs(d1) / nl;

            for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
                d2 += Math.pow((sol.getObjective(i) - this.z[i]) - d1 * (lambda[i] / nl), 2.0);
            }
            d2 = Math.sqrt(d2);

            fitness = (d1 + theta * d2);

        } else {
            System.out.println("dMOPSO.fitnessFunction: unknown type " + this.functionType);
            System.exit(-1);
        }
        return fitness;
    }

    private void shuffleGlobalBest() {
        int[] aux = new int[this.swarmSize];
        int rnd;
        int tmp;

        for (int i = 0; i < this.swarmSize; i++) {
            aux[i] = i;
        }

        for (int i = 0; i < this.swarmSize; i++) {
            rnd = this.randomGenerator.nextInt(i, this.swarmSize - 1);
            tmp = aux[rnd];
            aux[rnd] = aux[i];
            this.shfGBest[i] = tmp;
        }
    }

    private void repairBounds(int part) {

        DoubleSolution particle = this.swarm.get(part);

        for (int var = 0; var < particle.getNumberOfVariables(); var++) {
            if (particle.getVariableValue(var) < this.problem.getLowerBound(var)) {
                particle.setVariableValue(var, this.problem.getLowerBound(var));
                this.speed[part][var] *= this.changeVelocity1;
            }
            if (particle.getVariableValue(var) > this.problem.getUpperBound(var)) {
                particle.setVariableValue(var, this.problem.getUpperBound(var));
                this.speed[part][var] *= this.changeVelocity2;
            }
        }
    }

    private void resetParticle(int i) {
        DoubleSolution particle = this.swarm.get(i);
        double mean, sigma, N;

        for (int var = 0; var < particle.getNumberOfVariables(); var++) {
            DoubleSolution gB, pB;
            gB = this.globalBest[this.shfGBest[i]];
            pB = this.localBest[i];

            mean = (gB.getVariableValue(var) - pB.getVariableValue(var)) / 2;

            sigma = Math.abs(gB.getVariableValue(var) - pB.getVariableValue(var));

            java.util.Random rnd = new java.util.Random();

            N = rnd.nextGaussian() * sigma + mean;

            particle.setVariableValue(var, N);
            this.speed[i][var] = 0.0;
        }
    }

    private double velocityConstriction(double v, double[] deltaMax, double[] deltaMin,
                                        int variableIndex, int particleIndex) {

        double result;

        double dmax = deltaMax[variableIndex];
        double dmin = deltaMin[variableIndex];

        result = Math.min(v, dmax);

        if (v < dmin) {
            result = dmin;
        }

        return result;
    }

    private double constrictionCoefficient(double c1, double c2) {
        double rho = c1 + c2;
        if (rho <= 4) {
            return 1.0;
        } else {
            return 2 / (2 - rho - Math.sqrt(Math.pow(rho, 2.0) - 4.0 * rho));
        }
    }

    private double inertiaWeight(int iter, int miter, double wma, double wmin) {
        return wma;
    }

    @Override
    public void run() {
        this.swarm = this.createInitialSwarm();
        this.evaluateSwarm(this.swarm);
        this.initializeVelocity(this.swarm);

        this.initUniformWeight();
        this.initIdealPoint();

        this.initializeLeaders(this.swarm);
        this.initializeParticlesMemory(this.swarm);

        this.updateGlobalBest();

        this.initProgress();
        while (!this.isStoppingConditionReached()) {
            this.shuffleGlobalBest();

            for (int i = 0; i < this.swarm.size(); i++) {
                if (this.age[i] < this.maxAge) {
                    this.updateVelocity(i);
                    this.computeNewPositions(i);
                } else {
                    this.resetParticle(i);
                }

                this.repairBounds(i);

                this.problem.evaluate(this.swarm.get(i));
                this.updateReference(this.swarm.get(i));
                this.updateLocalBest(i);
            }
            this.updateGlobalBest();
            this.updateProgress();
        }
    }

    @Override
    public List<DoubleSolution> getResult() {
        return Arrays.asList(this.globalBest);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return "MOPSO with decomposition";
    }

    public enum FunctionType {
        TCHE, PBI, AGG
    }
}